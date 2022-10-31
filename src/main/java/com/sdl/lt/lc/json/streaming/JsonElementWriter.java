package com.sdl.lt.lc.json.streaming;


import com.sdl.lt.lc.json.streaming.element.JsonElement;

/**
 * @author anegruti
 * @since 5/24/2022
 */
public interface JsonElementWriter {

    void writeJsonElement(JsonElement element);

}
