package com.sdl.lt.lc.json.streaming.element;

import com.fasterxml.jackson.core.JsonToken;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

/**
 * @author anegruti
 * @since 5/13/2022
 */
@EqualsAndHashCode
@ToString
public class JsonPathElement {

    private final JsonToken token;
    @Getter
    @Setter
    private String fieldName;

    private JsonPathElement(JsonToken token) {
        this.token = token;
    }

    public static JsonPathElement element(JsonToken token) {
        return new JsonPathElement(token);
    }

    public boolean hasToken(JsonToken token) {
        return Objects.equals(this.token, token);
    }

}
