package com.sdl.lt.lc.json.streaming.element;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author anegruti
 * @since 5/17/2022
 */
@Getter
@RequiredArgsConstructor
public class JsonObjectElement<T> implements JsonElement {

    private final String fieldName;
    private final T element;

}
