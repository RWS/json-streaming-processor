package com.sdl.lt.lc.json.streaming.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * @author anegruti
 * @since 10/31/2022
 */
public class TestUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String RESOURCES_PATH = "src/test/resources/";

    @SneakyThrows
    public static InputStream getFileFromResources(String fileName) {
        File initialFile = new File(RESOURCES_PATH + fileName);
        return Files.newInputStream(initialFile.toPath());
    }

    @SneakyThrows
    public static <T> T deserialize(ByteArrayOutputStream stream, Class<T> clazz) {
        return MAPPER.readValue(stream.toByteArray(), clazz);
    }

}
