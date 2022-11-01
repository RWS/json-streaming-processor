package com.sdl.lt.lc.json.streaming;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
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

    private static final Map<Class<?>, BiConsumer<JsonGenerator, Object>> PRIMITIVE_TO_WRITER = Map.of(
            String.class, JsonWriter::writeString,
            Integer.class, JsonWriter::writeInteger,
            Float.class, JsonWriter::writeFloat
    );

    private final JsonParser parser;
    private final JsonGenerator generator;

    @SneakyThrows
    public void writeToken() {
        generator.copyCurrentEvent(parser);
    }

    @Override
    @SneakyThrows
    public void writeJsonElement(JsonElement element) {
        generator.writeFieldName(element.getFieldName());
        write(element.getElement());
    }

    @SneakyThrows
    public void write(Object object) {
        BiConsumer<JsonGenerator, Object> writer = PRIMITIVE_TO_WRITER.getOrDefault(
                object.getClass(),
                JsonWriter::writeObject
        );

        writer.accept(this.generator, object);
    }

    @Override
    public void close() throws Exception {
        this.generator.flush();
        this.generator.close();
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

    @SneakyThrows
    private static void writeObject(JsonGenerator g, Object value) {
        g.writeObject(value);
    }

}
