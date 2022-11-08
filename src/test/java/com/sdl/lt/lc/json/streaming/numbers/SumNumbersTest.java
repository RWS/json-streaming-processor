package com.sdl.lt.lc.json.streaming.numbers;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdl.lt.lc.json.streaming.JsonProcessorBuilder;
import com.sdl.lt.lc.json.streaming.ReadJsonProcessor;
import com.sdl.lt.lc.json.streaming.matchers.PathMatcher;
import com.sdl.lt.lc.json.streaming.matchers.PathMatcherBuilder;
import com.sdl.lt.lc.json.streaming.numbers.model.MyNumbers;
import com.sdl.lt.lc.json.streaming.utils.TestUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author anegruti
 * @since 10/31/2022
 */
class SumNumbersTest {

    @Test
    void shouldSumUpNumbersLibraryOnly() {
        InputStream numbersFile = getNumbersFile();

        ReadJsonProcessor readingProcessor = JsonProcessorBuilder.initProcessor(numbersFile);
        PathMatcher pathMatcher = PathMatcherBuilder.builder()
                .field("numbers").startArray()
                .build();

        Iterator<Integer> numbersIterator = readingProcessor.readValues(pathMatcher, Integer.class);

        long total = 0;
        while (numbersIterator.hasNext()) {
            total += numbersIterator.next();
        }

        assertEquals(1_000_000L, total);
    }

    @Test
    void shouldSumUpNumbersJacksonOnly() throws IOException {
        InputStream numbersFile = getNumbersFile();

        JsonParser parser = new JsonFactory().createParser(numbersFile);

        JsonToken token = parser.nextToken();

        long total = 0;
        while (token != null) {
            token = parser.nextToken();

            if (JsonToken.FIELD_NAME.equals(token) && parser.getCurrentName().equals("numbers")) {
                parser.nextToken(); //Position cursor at START_ARRAY

                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    total += parser.getValueAsInt();
                }
            }
        }

        assertEquals(1_000_000L, total);
    }

    @Test
    void shouldSumUpNumbersInMemory() throws IOException {
        InputStream numbersFile = getNumbersFile();
        ObjectMapper mapper = new ObjectMapper();

        MyNumbers numbers = mapper.readValue(numbersFile, MyNumbers.class);

        long total = numbers.getNumbers().stream()
                .reduce(0, Integer::sum);

        assertEquals(1_000_000L, total);
    }

    public static InputStream getNumbersFile() {
        return TestUtils.getFileFromResources("numbers.json");
    }

}
