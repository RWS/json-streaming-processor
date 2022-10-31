package com.sdl.lt.lc.json.streaming;

import com.fasterxml.jackson.core.JsonToken;
import com.sdl.lt.lc.json.streaming.element.JsonPathElement;
import lombok.Getter;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;

/**
 * @author anegruti
 * @since 5/25/2022
 */
@Getter
class JsonPathHolder {

    private final Deque<JsonPathElement> currentPath = new LinkedList<>();

    public void updatePath(JsonToken token) {
        if (token.isStructStart()) {
            currentPath.push(JsonPathElement.element(token));
        } else if (token.isStructEnd()) {
            currentPath.pop();
        }
    }

    public void setCurrentField(String fieldName) {
        Optional.ofNullable(currentPath.peek())
                .ifPresent(p -> p.setFieldName(fieldName));
    }

    public boolean hasNext() {
        return !currentPath.isEmpty();
    }

}
