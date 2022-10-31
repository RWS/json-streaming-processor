package com.sdl.lt.lc.json.streaming.matchers;

import com.sdl.lt.lc.json.streaming.element.JsonPathElement;

import java.util.*;

/**
 * @author anegruti
 * @since 5/13/2022
 */
class JsonPathMatcher implements PathMatcher {

    private final List<JsonPathElement> expectedPath = new ArrayList<>();

    void addElement(JsonPathElement jsonPathElement) {
        expectedPath.add(jsonPathElement);
    }

    @Override
    public boolean matches(Deque<JsonPathElement> pathElements) {
        if (expectedPath.size() != pathElements.size()) {
            return false;
        }

        Iterator<JsonPathElement> iterator = pathElements.descendingIterator();
        int i = 0;
        while (iterator.hasNext()) {
            JsonPathElement element = iterator.next();

            if (!Objects.equals(element, expectedPath.get(i))) {
                return false;
            }

            i++;
        }

        return true;
    }

}
