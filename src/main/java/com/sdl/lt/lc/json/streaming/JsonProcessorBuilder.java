package com.sdl.lt.lc.json.streaming;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdl.lt.lc.json.streaming.element.JsonArrayElement;
import com.sdl.lt.lc.json.streaming.element.JsonObjectElement;
import com.sdl.lt.lc.json.streaming.matchers.PathMatcher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Class that provides access to either a {@link VisitJsonProcessor} or {@link ReadJsonProcessor}
 * There is no other way to use any of those processors but through this builder.
 * If a builder is created it can be used to create {@link JsonElementTransformer} that refer the processor
 *
 * @apiNote the {@link JsonProcessorBuilder} implements {@link AutoCloseable}. It is recommended you use this class in
 * conjunction with a try-with-resources statement
 * @author anegruti
 * @since 5/24/2022
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonProcessorBuilder implements AutoCloseable {

    private final JsonPathProcessor processor;

    public static JsonProcessorBuilder initBuilder(InputStream inputStream, OutputStream outputStream) {
        return initBuilder(inputStream, outputStream, new ObjectMapper());
    }

    /**
     * Initialises a {@link JsonProcessorBuilder} by creating the processor
     * The builder can later be used to create various transformers to be used by {@link JsonVisitor}'s
     *
     * @param inputStream the {@link InputStream} from which the JSON will be read
     * @param outputStream the {@link OutputStream} to which the JSON will be written
     * @param mapper the {@link }
     * @return the {@link JsonProcessorBuilder} used to create a {@link VisitJsonProcessor} and to create Transformer's
     */
    public static JsonProcessorBuilder initBuilder(InputStream inputStream, OutputStream outputStream, ObjectMapper mapper) {
        JsonPathProcessor processor = JsonPathProcessor.init(inputStream, outputStream, mapper);

        return new JsonProcessorBuilder(processor);
    }

    public static ReadJsonProcessor initProcessor(InputStream inputStream) {
        return initProcessor(inputStream, new ObjectMapper());
    }

    /**
     * Initialises a {@link ReadJsonProcessor} by creating a {@link JsonPathProcessor} with a null {@link OutputStream}
     * The null {@link OutputStream} is provided to ensure that any potential writes will be discarded
     *
     * @param inputStream the {@link InputStream} from which the JSON will be read
     * @param mapper
     * @return the {@link ReadJsonProcessor} used to read parts of a JSON and skipping the rest
     */
    public static ReadJsonProcessor initProcessor(InputStream inputStream, ObjectMapper mapper) {
        JsonPathProcessor processor = JsonPathProcessor.init(inputStream, OutputStream.nullOutputStream(), mapper);

        return new ReadJsonProcessor(processor);
    }

    /**
     * Returns a {@link JsonElementTransformer} that will replace the element found at the provided path
     *
     * @param pathMatcher the path to be used when finding where to apply the executor
     * @param replacer object to be used for replacement
     * @return a {@link JsonElementTransformer}
     * @see JsonElementTransformer
     * @see JsonVisitor
     */
    public <T> JsonElementTransformer replace(PathMatcher pathMatcher, T replacer) {
        return new JsonElementTransformer(
                pathMatcher,
                () -> this.processor.replace(replacer)
        );
    }

    /**
     * Returns a {@link JsonElementTransformer} that will read an object of type {@link T} and call the provided
     * {@link Consumer}
     * The element read will be written to the OutputStream if provided.
     *
     * @param pathMatcher the path to be used when finding where to apply the executor
     * @param clazz the class of {@link T}
     * @param consumer the {@link Consumer} that will be used to consume the {@link JsonObjectElement} found
     * @return a {@link JsonElementTransformer}
     * @see JsonElementTransformer
     * @see JsonVisitor
     */
    public <T> JsonElementTransformer peek(PathMatcher pathMatcher, Class<T> clazz, Consumer<JsonObjectElement<T>> consumer) {
        return new JsonElementTransformer(
                pathMatcher,
                () -> this.processor.peek(clazz, consumer)
        );
    }

    /**
     * Returns a {@link JsonElementTransformer} that will read a list of objects of type {@link T} and call the provided
     * {@link Consumer}
     * The elements read will be written to the OutputStream if provided.
     *
     * @param pathMatcher the path to be used when finding where to apply the executor
     * @param clazz the class of {@link T}
     * @param consumer the {@link Consumer} that will be used to consume the {@link JsonArrayElement} found
     * @return a {@link JsonElementTransformer}
     * @see JsonElementTransformer
     * @see JsonVisitor
     */
    public <T> JsonElementTransformer peekAll(PathMatcher pathMatcher, Class<T> clazz, Consumer<JsonArrayElement<T>> consumer) {
        return new JsonElementTransformer(
                pathMatcher,
                () -> this.processor.peekAll(clazz, consumer)
        );
    }

    /**
     * Returns a {@link JsonElementTransformer} that will consume an object of type {@link T} and call the provided
     * {@link Consumer}
     * Writing to the provided OutputStream will be omitted
     *
     * @param pathMatcher the path to be used when finding where to apply the executor
     * @param clazz the class of {@link T}
     * @param consumer the {@link Consumer} that will be used to consume the {@link JsonObjectElement} found
     * @return a {@link JsonElementTransformer}
     * @see JsonElementTransformer
     * @see JsonVisitor
     */
    public <T> JsonElementTransformer consume(PathMatcher pathMatcher, Class<T> clazz, Consumer<JsonObjectElement<T>> consumer) {
        return new JsonElementTransformer(
                pathMatcher,
                () -> this.processor.consume(clazz, consumer)
        );
    }

    /**
     * Returns a {@link JsonElementTransformer} that will consume a list of objects of type {@link T} and call the provided
     * {@link Consumer}
     *
     * @param pathMatcher the path to be used when finding where to apply the executor
     * @param clazz the class of {@link T}
     * @param consumer the {@link Consumer} that will be used to consume the {@link JsonArrayElement} found
     * @return a {@link JsonElementTransformer}
     * @see JsonElementTransformer
     * @see JsonVisitor
     */
    public <T> JsonElementTransformer consumeAll(PathMatcher pathMatcher, Class<T> clazz, Consumer<JsonArrayElement<T>> consumer) {
        return new JsonElementTransformer(
                pathMatcher,
                () -> this.processor.consumeAll(clazz, consumer)
        );
    }

    /**
     * Returns a {@link JsonElementTransformer} that will map each element of type {@link T} found at the provided {@link PathMatcher}
     * with an object of type {@link R}
     *
     * @param pathMatcher the path to be used when finding where to apply the executor
     * @param clazz the class of {@link T}
     * @param mapper the {@link Function} used to map from {@link T} to {@link R}
     * @return a {@link JsonElementTransformer}
     * @see JsonElementTransformer
     * @see JsonVisitor
     */
    public <T, R> JsonElementTransformer mapEach(PathMatcher pathMatcher, Class<T> clazz, Function<T, R> mapper) {
        return new JsonElementTransformer(
                pathMatcher,
                () -> this.processor.mapEach(clazz, mapper)
        );
    }

    /**
     * @return a {@link VisitJsonProcessor} used to visit a JSON provided an implementation of a {@link JsonVisitor}
     * @see VisitJsonProcessor
     * @see JsonElementTransformer
     * @see JsonVisitor
     */
    public VisitJsonProcessor build() {
        return new VisitJsonProcessor(processor);
    }

    @Override
    public void close() throws Exception {
        this.processor.close();
    }

}
