package com.sdl.lt.lc.json.streaming;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdl.lt.lc.json.streaming.element.JsonArrayElement;
import com.sdl.lt.lc.json.streaming.element.JsonObjectElement;
import com.sdl.lt.lc.json.streaming.matchers.PathMatcher;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author anegruti
 * @since 5/12/2022
 */
class JsonPathProcessor implements AutoCloseable {

    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    private final JsonParser parser;
    private final JsonWriter writer;
    private final JsonPathHolder path = new JsonPathHolder();

    @SneakyThrows
    private JsonPathProcessor(InputStream inputStream, OutputStream outputStream, ObjectMapper mapper) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        this.parser = JSON_FACTORY.createParser(reader);
        this.parser.setCodec(mapper);

        JsonGenerator generator = JSON_FACTORY.createGenerator(outputStream);
        generator.setCodec(mapper);

        this.writer = new JsonWriter(parser, generator);
        writeStart();
    }

    static JsonPathProcessor init(InputStream inputStream, OutputStream outputStream, ObjectMapper mapper) {
        return new JsonPathProcessor(inputStream, outputStream, mapper);
    }

    void skipUntilPathOrEnd(PathMatcher pathMatcher) {
        while (path.hasNext()) {
            this.next();

            if (pathMatcher.matches(path.getCurrentPath())) {
                break;
            }
        }
    }

    /**
     * Will replace at current position the object with the provided class
     *
     * @param replacer the class to use as a replacement
     *
     * @implNote Before attempting to replace the current object we ensure that the current cursor position is on a field name.
     * We then move one token further to be on a START_OBJECT/START_ARRAY and we skip the entire object or array
     * After skipping everything we then write the field name at which we were positioned and the provided replacer
     */
    @SneakyThrows
    <T> void replace(T replacer) {
        ensureCurrentTokenIsOfTypeFieldName();

        parser.nextToken();
        parser.skipChildren();

        writer.writeJsonElement(new JsonObjectElement<>(parser.getCurrentName(), replacer));
    }

    <T> void read(Class<T> clazz, Consumer<JsonObjectElement<T>> consumer) {
        JsonObjectElement<T> retrieved = this.retrieve(clazz);
        writer.writeJsonElement(retrieved);
        consumer.accept(retrieved);
    }

    <T> void consume(Class<T> clazz, Consumer<JsonObjectElement<T>> consumer) {
        JsonObjectElement<T> retrieved = this.retrieve(clazz);
        consumer.accept(retrieved);
    }

    <T> void readAll(Class<T> clazz, Consumer<JsonArrayElement<T>> consumer) {
        JsonArrayElement<T> retrieved = this.retrieveAll(clazz);
        writer.writeJsonElement(retrieved);
        consumer.accept(retrieved);
    }

    <T> void consumeAll(Class<T> clazz, Consumer<JsonArrayElement<T>> consumer) {
        JsonArrayElement<T> retrieved = this.retrieveAll(clazz);
        consumer.accept(retrieved);
    }

    /**
     * Will retrieve an object of type {@link T} at current location
     *
     * @param clazz the class of {@link T}
     * @return the {@link JsonObjectElement} which holds current field name and the retrieved object
     *
     * @implNote Before attempting to retrieve the object we ensure that the cursor is set on the START_OBJECT to
     * be able to retrieve an object
     */
    @SneakyThrows
    private <T> JsonObjectElement<T> retrieve(Class<T> clazz) {
        parser.nextToken();

        return new JsonObjectElement<>(
                parser.getCurrentName(),
                parser.readValueAs(clazz)
        );
    }

    /**
     * Will retrieve a list of objects of type {@link T} at current location
     *
     * @param clazz the class of {@link T}
     * @return the {@link JsonArrayElement} which holds current field name and the retrieved list
     *
     * @implNote Before attempting to retrieve the list we ensure that the cursor is set on the START_ARRAY to
     * be able to retrieve the array.
     * After that we move one token further on the first START_OBJECT or VALUE to start retrieving
     */
    @SneakyThrows
    private <T> JsonArrayElement<T> retrieveAll(Class<T> clazz) {
        parser.nextToken();
        ensureCurrentTokenIsOfTypeStartArray();

        List<T> elements = new LinkedList<>();
        if (JsonToken.END_ARRAY.equals(parser.nextToken())) {
            return new JsonArrayElement<>(parser.getCurrentName(), elements);
        }

        parser.readValuesAs(clazz).forEachRemaining(elements::add);
        return new JsonArrayElement<>(parser.getCurrentName(), elements);
    }

    /**
     * Will map each class using the provided mapper and write the result into the {@link OutputStream}
     *
     * @param clazz the class of {@link T}
     * @param mapper the mapper used to map from {@link T} to {@link R}
     *
     * @implNote Before getting an iterator over the type of objects we expect to see at current path we ensure that
     * the cursor is set on the START_ARRAY token and that it is written in our {@link OutputStream}.
     * After that we iterate over each element, apply the provided function and write the result into the {@link OutputStream}
     * And the end we stop the iteration by adding an END_ARRAY to the path (thus popping the START_ARRAY we previously added),
     * and we also write END_ARRAY to the {@link OutputStream} ensuring a valid JSON Array is written
     */
    <T, R> void mapEach(Class<T> clazz, Function<T, R> mapper) {
        ensureStartOfArrayIsWritten();
        Iterator<T> iterator = this.getIterator(clazz);

        while (iterator.hasNext()) {
            T item = iterator.next();
            R mapped = mapper.apply(item);

            writer.write(mapped);
        }

        this.stopIteration();
    }

    @SneakyThrows
    private <T> Iterator<T> getIterator(Class<T> clazz) {
        if (JsonToken.END_ARRAY.equals(parser.nextToken())) {
            return Collections.emptyIterator();
        }

        return readValuesAs(clazz);
    }

    private void stopIteration() {
        JsonToken endArray = JsonToken.END_ARRAY;

        path.updatePath(endArray);
        writer.writeToken();
    }

    /**
     * Returns an iterator over objects of type {@link T}
     *
     * @param clazz the class of {@link T}
     * @return the {@link Iterator} over objects of type {@link T}
     */
    @SneakyThrows
    <T> Iterator<T> readValuesAs(Class<T> clazz) {
        return parser.readValuesAs(clazz);
    }

    boolean hasNext() {
        return path.hasNext();
    }

    private void writeStart() {
        writeNextToken();
    }

    JsonToken writeNextToken() {
        JsonToken token = next();
        writeToken();

        return token;
    }

    /**
     * Will read the next {@link JsonToken} and update the current path using the value it holds
     *
     * @return the {@link JsonToken} that has been read
     */
    @SneakyThrows
    JsonToken next() {
        JsonToken token = parser.nextToken();
        updatePath(token);

        return token;
    }

    @SneakyThrows
    void updatePath(JsonToken token) {
        if (parser.hasToken(JsonToken.FIELD_NAME)) {
            path.setCurrentField(parser.getCurrentName());
        } else {
            path.updatePath(token);
        }
    }

    void writeToken() {
        writer.writeToken();
    }

    boolean checkIsCurrentPath(PathMatcher pathMatcher) {
        return pathMatcher.matches(path.getCurrentPath());
    }

    JsonElementWriter getWriter() {
        return writer;
    }

    private void ensureStartOfArrayIsWritten() {
        ensureCurrentTokenIsOfTypeStartArray();

        writer.writeToken();
    }

    void ensureCurrentTokenIsOfTypeStartArray() {
        if (!parser.isExpectedStartArrayToken()) {
            throw new UnsupportedOperationException("Expected position was at start of array, found " + parser.currentToken().name());
        }
    }

    private void ensureCurrentTokenIsOfTypeFieldName() {
        JsonToken currentToken = parser.getCurrentToken();

        if (!Objects.equals(currentToken, JsonToken.FIELD_NAME)) {
            throw new UnsupportedOperationException("Expected position was a field name, found " + parser.currentToken().name());
        }
    }

    void ensureCurrentTokenIsOfTypeStructStart() {
        JsonToken currentToken = parser.currentToken();
        if (!currentToken.isStructStart()) {
            throw new UnsupportedOperationException("Expected position was at start of a structure, found " + parser.currentToken().name());
        }
    }

    @Override
    public void close() throws Exception {
        this.parser.close();
        this.writer.close();
    }

}
