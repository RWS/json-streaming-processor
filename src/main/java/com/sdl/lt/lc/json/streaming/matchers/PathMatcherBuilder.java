package com.sdl.lt.lc.json.streaming.matchers;

import com.fasterxml.jackson.core.JsonToken;
import com.sdl.lt.lc.json.streaming.element.JsonPathElement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * @author anegruti
 * @since 5/13/2022
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PathMatcherBuilder {

    private final JsonPathMatcher matcher;

    public static PathMatcherBuilder builder() {
        return new PathMatcherBuilder(new JsonPathMatcher());
    }

    public PathMatcherBuilder field(String fieldName) {
        JsonPathElement element = JsonPathElement.element(JsonToken.START_OBJECT);
        element.setFieldName(fieldName);

        matcher.addElement(element);
        return this;
    }

    public PathMatcherBuilder startArray() {
        JsonPathElement element = JsonPathElement.element(JsonToken.START_ARRAY);

        matcher.addElement(element);
        return this;
    }

    public PathMatcherBuilder startObject() {
        JsonPathElement element = JsonPathElement.element(JsonToken.START_OBJECT);

        matcher.addElement(element);
        return this;
    }

    public PathMatcher build() {
        return matcher;
    }

}
