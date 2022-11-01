package com.sdl.lt.lc.json.streaming;

import com.fasterxml.jackson.core.JsonToken;
import com.sdl.lt.lc.json.streaming.matchers.PathMatcher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author anegruti
 * @since 5/24/2022
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class JsonVisitingProcessor implements AutoCloseable {

    private static final int INITIAL_COUNTER_VALUE = 1;
    private final JsonPathProcessor processor;

    /**
     * Will return an iterator over all JSON elements that match the provided {@link PathMatcher}
     *
     * @param pathMatcher the {@link PathMatcher} used to find over what type of paths to iterate
     * @return a {@link PathIterator}
     */
    public PathIterator getIterator(PathMatcher pathMatcher) {
        return new JsonPathIterator(
                pathMatcher,
                this::visitObject
        );
    }

    /**
     * Will visit the entire object once applying the provided {@link JsonVisitor}
     *
     * @param visitor a {@link JsonVisitor} used while travelling over the JSON object
     */
    public void visit(JsonVisitor visitor) {
        this.visitObject(visitor);
    }

    /**
     * Iterator over every token of the Object at the current cursor point in the Stream
     *
     * @param visitor a {@link JsonVisitor} used while travelling over the JSON object
     * @implNote We start by ensuring the cursor is on a {@link JsonToken#START_OBJECT START_OBJECT}
     * or {@link JsonToken#START_ARRAY START_ARRAY} position.
     * A counter starts with the value 1 and is used to track the number of time we start a structure (increments the counter)
     * or the number of times we end a structure (decreases the counter)
     * Whenever one of the provided transformers matches the current path we will execute the {@link JsonElementTransformer} and
     * skip forward.
     * If there is no matching transformer we continue to write tokens and move forward.
     *
     * When entering and leaving the JSON Object we will be using the provided hooks of the {@link JsonVisitor}
     * @see JsonVisitor
     * @see JsonToken
     */
    @SneakyThrows
    private void visitObject(JsonVisitor visitor) {
        processor.ensureCurrentTokenIsOfTypeStructStart();
        int counter = INITIAL_COUNTER_VALUE;

        visitor.entering(processor.getWriter());
        while (counter != 0) {
            JsonToken jsonToken = processor.next();

            if (existsTransformerWithMatchingPath(visitor)) {
                runMatchingTransformers(visitor);
                continue;
            }

            if (jsonToken.isStructStart()) {
                counter++;
            } else if (jsonToken.isStructEnd()) {
                counter--;
            }

            if (counter == 0) {
                visitor.beforeLeaving(processor.getWriter());
            }

            processor.writeToken();
        }
        visitor.leaving(processor.getWriter());
    }

    /**
     * @param visitor the {@link JsonVisitor} that holds the transformers
     * @return Returns:
     * <ul>
     *     <li>True - if there is at least one transformer which has a matching {@link PathMatcher}</li>
     *     <li>False - if there is no transformer which has a matching {@link PathMatcher}</li>
     * </ul>
     */
    private boolean existsTransformerWithMatchingPath(JsonVisitor visitor) {
        return visitor.getTransformers().stream()
                .map(JsonElementTransformer::getPathMatcher)
                .anyMatch(processor::checkIsCurrentPath);
    }

    /**
     * Will run all the transformers that have a matching {@link PathMatcher}
     *
     * @apiNote Used in conjunction with {@link #existsTransformerWithMatchingPath(JsonVisitor)}
     * It is recommended that only one transformer matches one path. If this is not true the behaviour is not promised.
     * @param visitor the {@link JsonVisitor} that holds the transformers
     */
    private void runMatchingTransformers(JsonVisitor visitor) {
        visitor.getTransformers().stream()
                .filter(p -> processor.checkIsCurrentPath(p.getPathMatcher()))
                .map(JsonElementTransformer::getExecutor)
                .forEach(Runnable::run);
    }

    @Override
    public void close() throws Exception {
        this.processor.close();
    }

    /**
     * An implementation of {@link PathIterator}
     */
    @RequiredArgsConstructor
    private class JsonPathIterator implements PathIterator {

        private final PathMatcher rootPath;
        private final Consumer<JsonVisitor> visit;
        private boolean hasNext = false;

        /**
         *
         * @return Returns:
         * <ul>
         *     <li>True - if there is another starting object matching the {@link PathMatcher}</li>
         *     <li>False - if there is no other starting object matching the {@link PathMatcher}</li>
         * </ul>
         * @implNote We first check if we found that the cursor has been positioned already at another starting object
         * that matches the {@link PathMatcher}.
         * If this is not the case we travel over the tokens. As soon as we find the start of an object matching the current
         * path we stop the iteration, and we set the {@link JsonPathIterator#hasNext} value to true in order to avoid further
         * processing.
         */
        @Override
        public boolean hasNext() {
            if (this.hasNext) {
                return true;
            }

            while (processor.hasNext()) {
                if (processor.checkIsCurrentPath(rootPath) && nextTokenIsStartOfObject()) {
                    this.hasNext = true;
                    return true;
                }

                processor.writeNextToken();
            }

            return false;
        }

        private boolean nextTokenIsStartOfObject() {
            JsonToken nextToken = processor.writeNextToken();

            return Objects.equals(nextToken, JsonToken.START_OBJECT);
        }

        /**
         *
         * @param visitor the {@link JsonVisitor} used while travelling over the JSON object
         * @implNote We first reset the {@link JsonPathIterator#hasNext} to ensure that when we call {@link JsonPathIterator#hasNext()}
         * again we move the cursor forward.
         * After that we will call the {@link JsonVisitingProcessor#visitObject(JsonVisitor)} method provided as a consumer
         * with the provided {@link JsonVisitor}
         */
        @Override
        public void visit(JsonVisitor visitor) {
            this.hasNext = false;
            this.visit.accept(visitor);
        }

    }

}
