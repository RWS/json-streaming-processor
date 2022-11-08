package com.sdl.lt.lc.json.streaming.numbers;

import com.sdl.lt.lc.json.streaming.JsonProcessorBuilder;
import com.sdl.lt.lc.json.streaming.JsonVisitor;
import com.sdl.lt.lc.json.streaming.VisitJsonProcessor;
import com.sdl.lt.lc.json.streaming.matchers.PathMatcher;
import com.sdl.lt.lc.json.streaming.matchers.PathMatcherBuilder;
import com.sdl.lt.lc.json.streaming.numbers.model.MyNumbers;
import com.sdl.lt.lc.json.streaming.utils.TestUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author anegruti
 * @since 10/31/2022
 */
public class NumbersLibraryTest {

    private static final PathMatcher USERNAME_PATH = PathMatcherBuilder.builder()
            .field("requester").field("username")
            .build();

    private static final PathMatcher NUMBERS_PATH = PathMatcherBuilder.builder()
            .field("numbers")
            .build();

    private static final PathMatcher NUMBERS_ARRAY_PATH = PathMatcherBuilder.builder()
            .field("numbers").startArray()
            .build();

    @Test
    void shouldReplaceUsername() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JsonProcessorBuilder builder = JsonProcessorBuilder.initBuilder(getNumbersFile(), outputStream);

        try (VisitJsonProcessor visitingProcessor = builder.build()) {
            visitingProcessor.visit(
                    JsonVisitor.withTransformer(builder.replace(USERNAME_PATH, "newUsername"))
            );
        }

        MyNumbers numbers = TestUtils.deserialize(outputStream, MyNumbers.class);

        assertThat(numbers.getRequester().getUsername(), is("newUsername"));
    }

    @Test
    void shouldReadUsernameAndWriteToOutputByDefault() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JsonProcessorBuilder builder = JsonProcessorBuilder.initBuilder(getNumbersFile(), outputStream);

        AtomicReference<String> usernameRef = new AtomicReference<>();
        try (VisitJsonProcessor visitingProcessor = builder.build()) {
            visitingProcessor.visit(
                    JsonVisitor.withTransformer(builder.read(USERNAME_PATH, String.class, s -> usernameRef.set(s.getElement())))
            );
        }

        MyNumbers numbers = TestUtils.deserialize(outputStream, MyNumbers.class);

        assertThat(numbers.getRequester().getUsername(), is(usernameRef.get()));
    }

    @Test
    void shouldConsumeUsernameAndNotWriteToOutputStream() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JsonProcessorBuilder builder = JsonProcessorBuilder.initBuilder(getNumbersFile(), outputStream);

        AtomicReference<String> usernameRef = new AtomicReference<>();
        try (VisitJsonProcessor visitingProcessor = builder.build()) {
            visitingProcessor.visit(
                    JsonVisitor.withTransformer(builder.consume(USERNAME_PATH, String.class, s -> usernameRef.set(s.getElement())))
            );
        }

        MyNumbers numbers = TestUtils.deserialize(outputStream, MyNumbers.class);

        assertThat(numbers.getRequester().getUsername(), nullValue());
        assertThat(usernameRef.get(), is("admin"));
    }

    @Test
    void shouldReadAllNumbersAndWriteToOutputStream() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JsonProcessorBuilder builder = JsonProcessorBuilder.initBuilder(getNumbersFile(), outputStream);

        List<Integer> numbersArray = new ArrayList<>();
        try (VisitJsonProcessor visitingProcessor = builder.build()) {
            visitingProcessor.visit(
                    JsonVisitor.withTransformer(builder.readAll(NUMBERS_PATH, Integer.class, nrs -> numbersArray.addAll(nrs.getElement())))
            );
        }

        MyNumbers numbers = TestUtils.deserialize(outputStream, MyNumbers.class);

        assertThat(numbers.getNumbers(), is(numbersArray));
    }

    @Test
    void shouldConsumeAllNumbersAndNotWriteToOutputStream() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JsonProcessorBuilder builder = JsonProcessorBuilder.initBuilder(getNumbersFile(), outputStream);

        List<Integer> numbersArray = new ArrayList<>();
        try (VisitJsonProcessor visitingProcessor = builder.build()) {
            visitingProcessor.visit(
                    JsonVisitor.withTransformer(builder.consumeAll(NUMBERS_PATH, Integer.class, nrs -> numbersArray.addAll(nrs.getElement())))
            );
        }

        MyNumbers numbers = TestUtils.deserialize(outputStream, MyNumbers.class);

        assertThat(numbers.getNumbers(), nullValue());
        assertThat(numbersArray, contains(321312, 43234, 3242, 12, 1924, 235, 325, 42342, 484243, 103131));
    }

    @Test
    void shouldAddPlusOneToEachNumber() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JsonProcessorBuilder builder = JsonProcessorBuilder.initBuilder(getNumbersFile(), outputStream);

        try (VisitJsonProcessor visitingProcessor = builder.build()) {
            visitingProcessor.visit(
                    JsonVisitor.withTransformer(builder.mapEach(NUMBERS_ARRAY_PATH, Integer.class, nr -> nr + 1))
            );
        }

        MyNumbers numbers = TestUtils.deserialize(outputStream, MyNumbers.class);

        assertThat(numbers.getNumbers(), contains(321313, 43235, 3243, 13, 1925, 236, 326, 42343, 484244, 103132));
    }

    public static InputStream getNumbersFile() {
        return TestUtils.getFileFromResources("numbers.json");
    }

}
