package com.sdl.lt.lc.json.streaming;

import com.fasterxml.jackson.core.JsonToken;
import com.sdl.lt.lc.json.streaming.matchers.PathMatcher;
import com.sdl.lt.lc.json.streaming.matchers.PathMatcherBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author anegruti
 * @since 10/31/2022
 */
class JsonPathHolderTest {

    @Test
    void shouldCreateMatchingPath() {
        JsonPathHolder pathHolder = new JsonPathHolder();
        pathHolder.updatePath(JsonToken.START_OBJECT);
        pathHolder.setCurrentField("field1");
        pathHolder.updatePath(JsonToken.START_ARRAY);
        pathHolder.updatePath(JsonToken.START_OBJECT);

        PathMatcher pathMatcher = PathMatcherBuilder.builder()
                .field("field1").startArray()
                .startObject()
                .build();

        boolean matches = pathMatcher.matches(pathHolder.getCurrentPath());

        assertTrue(matches);
    }

    @Test
    void shouldWriteFieldOverCurrent() {
        JsonPathHolder pathHolder = new JsonPathHolder();
        pathHolder.updatePath(JsonToken.START_OBJECT);
        pathHolder.setCurrentField("field1");
        pathHolder.setCurrentField("field2");
        pathHolder.setCurrentField("field3");

        PathMatcher pathMatcher = PathMatcherBuilder.builder().field("field3").build();

        boolean matches = pathMatcher.matches(pathHolder.getCurrentPath());

        assertTrue(matches);
    }

    @Test
    void shouldHaveNext() {
        JsonPathHolder pathHolder = new JsonPathHolder();
        pathHolder.updatePath(JsonToken.START_OBJECT);

        assertTrue(pathHolder.hasNext());
    }

    @Test
    void emptyPathHolderShouldNotHaveNext() {
        JsonPathHolder pathHolder = new JsonPathHolder();

        assertFalse(pathHolder.hasNext());
    }

    @Test
    void emptiedPathHolderShouldNotHaveNext() {
        JsonPathHolder pathHolder = new JsonPathHolder();
        pathHolder.updatePath(JsonToken.START_ARRAY);
        pathHolder.updatePath(JsonToken.END_ARRAY);

        assertFalse(pathHolder.hasNext());
    }

}