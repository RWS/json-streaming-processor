package com.sdl.lt.lc.json.streaming;

/**
 * @author anegruti
 * @since 5/27/2022
 */
public interface PathIterator {

    boolean hasNext();
    void visit(JsonVisitor visitor);

}
