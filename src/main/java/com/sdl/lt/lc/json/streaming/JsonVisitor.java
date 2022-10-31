package com.sdl.lt.lc.json.streaming;

import java.util.Collections;
import java.util.List;

/**
 * @author anegruti
 * @since 5/18/2022
 */
public interface JsonVisitor {

    default void entering(JsonElementWriter writer) {

    }

    default void beforeLeaving(JsonElementWriter writer) {

    }

    default void leaving(JsonElementWriter writer) {

    }

    List<JsonElementTransformer> getTransformers();

    static JsonVisitor withTransformer(JsonElementTransformer transformer) {
        return withTransformers(Collections.singletonList(transformer));
    }

    static JsonVisitor withTransformers(List<JsonElementTransformer> transformers) {
        return () -> transformers;
    }

}
