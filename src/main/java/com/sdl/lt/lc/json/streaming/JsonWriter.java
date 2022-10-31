package com.sdl.lt.lc.json.streaming;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.sdl.lt.lc.json.streaming.element.JsonElement;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author anegruti
 * @since 5/23/2022
 */
@RequiredArgsConstructor
class JsonWriter implements AutoCloseable, JsonElementWriter {

    private static final Map<JsonToken, BiConsumer<JsonGenerator, JsonParser>> TOKEN_TO_WRITER = Map.ofEntries(
            Map.entry(JsonToken.START_OBJECT, JsonWriter::startObject),
            Map.entry(JsonToken.END_OBJECT, JsonWriter::endObject),
            Map.entry(JsonToken.START_ARRAY, JsonWriter::startArray),
            Map.entry(JsonToken.END_ARRAY, JsonWriter::endArray),
            Map.entry(JsonToken.FIELD_NAME, JsonWriter::startField),
            Map.entry(JsonToken.VALUE_STRING, JsonWriter::writeString),
            Map.entry(JsonToken.VALUE_NUMBER_INT, JsonWriter::writeInt),
            Map.entry(JsonToken.VALUE_NUMBER_FLOAT, JsonWriter::writeFloat),
            Map.entry(JsonToken.VALUE_TRUE, JsonWriter::writeTrue),
            Map.entry(JsonToken.VALUE_FALSE, JsonWriter::writeFalse),
            Map.entry(JsonToken.VALUE_NULL, JsonWriter::writeNull)
    );

    private static final Map<Class<?>, BiConsumer<JsonGenerator, Object>> PRIMITIVE_TO_WRITER = Map.of(
            String.class, JsonWriter::writeString,
            Integer.class, JsonWriter::writeInteger,
            Float.class, JsonWriter::writeFloat
    );

    private final JsonParser parser;
    private final JsonGenerator generator;

    public void writeToken(JsonToken current) {
        BiConsumer<JsonGenerator, JsonParser> currentWriter = TOKEN_TO_WRITER.get(current);

        if (currentWriter == null) {
            throw new UnsupportedOperationException("Unknown token provided");
        }

        currentWriter.accept(generator, parser);
    }

    @Override
    public void writeJsonElement(JsonElement element) {
        writeFieldName(element.getFieldName());
        write(element.getElement());
    }

    @SneakyThrows
    public void writeFieldName(String fieldName) {
        generator.writeFieldName(fieldName);
    }

    @SneakyThrows
    public void write(Object object) {
        BiConsumer<JsonGenerator, Object> writer = PRIMITIVE_TO_WRITER.getOrDefault(
                object.getClass(),
                (g, o) -> writeObject(o)
        );

        writer.accept(this.generator, object);
    }

    @SneakyThrows
    public void writeInteger(Object object) {
        generator.writeNumber((Integer) object);
    }

    @SneakyThrows
    private void writeObject(Object object) {
        this.generator.writeObject(object);
    }

    @Override
    public void close() throws Exception {
        this.generator.flush();
        this.generator.close();
    }

    @SneakyThrows
    private static void startObject(JsonGenerator g, JsonParser p) {
        g.writeStartObject();
    }

    @SneakyThrows
    private static void endObject(JsonGenerator g, JsonParser p) {
        g.writeEndObject();
    }

    @SneakyThrows
    private static void startArray(JsonGenerator g, JsonParser p) {
        g.writeStartArray();
    }

    @SneakyThrows
    private static void endArray(JsonGenerator g, JsonParser p) {
        g.writeEndArray();
    }

    @SneakyThrows
    private static void startField(JsonGenerator g, JsonParser p) {
        g.writeFieldName(p.getCurrentName());
    }

    @SneakyThrows
    private static void writeString(JsonGenerator g, JsonParser p) {
        g.writeString(p.getValueAsString());
    }

    @SneakyThrows
    private static void writeInt(JsonGenerator g, JsonParser p) {
        g.writeNumber(p.getValueAsInt());
    }

    @SneakyThrows
    private static void writeFloat(JsonGenerator g, JsonParser p) {
        g.writeNumber(p.getValueAsDouble());
    }

    @SneakyThrows
    private static void writeTrue(JsonGenerator g, JsonParser p) {
        g.writeBoolean(true);
    }

    @SneakyThrows
    private static void writeFalse(JsonGenerator g, JsonParser p) {
        g.writeBoolean(false);
    }

    @SneakyThrows
    private static void writeNull(JsonGenerator g, JsonParser p) {
        g.writeNull();
    }

    @SneakyThrows
    private static void writeString(JsonGenerator g, Object value) {
        g.writeString((String) value);
    }

    @SneakyThrows
    private static void writeInteger(JsonGenerator g, Object value) {
        g.writeNumber((Integer) value);
    }

    @SneakyThrows
    private static void writeFloat(JsonGenerator g, Object value) {
        g.writeNumber((Float) value);
    }

}
