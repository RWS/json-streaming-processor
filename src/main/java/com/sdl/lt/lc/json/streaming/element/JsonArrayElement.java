package com.sdl.lt.lc.json.streaming.element;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @author anegruti
 * @since 5/17/2022
 */
@Getter
@RequiredArgsConstructor
public class JsonArrayElement<T> implements JsonElement {

    private final String fieldName;
    private final List<T> element;

}
