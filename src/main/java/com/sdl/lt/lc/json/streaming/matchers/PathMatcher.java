package com.sdl.lt.lc.json.streaming.matchers;

import com.sdl.lt.lc.json.streaming.element.JsonPathElement;

import java.util.Deque;

/**
 * @author anegruti
 * @since 5/13/2022
 */
public interface PathMatcher {
    boolean matches(Deque<JsonPathElement> pathElements);

    default PathMatcher or(PathMatcher other) {
        return e -> this.matches(e) || other.matches(e);
    }

    static PathMatcher neverMatch() {
        return e -> false;
    }

    static PathMatcher alwaysMatch() {
        return e -> true;
    }

}
