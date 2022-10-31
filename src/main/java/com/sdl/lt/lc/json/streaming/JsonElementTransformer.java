package com.sdl.lt.lc.json.streaming;

import com.sdl.lt.lc.json.streaming.matchers.PathMatcher;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author anegruti
 * @since 5/18/2022
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class JsonElementTransformer {

    private final PathMatcher pathMatcher;
    private final Runnable executor;

}
