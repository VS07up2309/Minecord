package com.tisawesomeness.minecord.util.type;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class EitherTest {

    @Test
    @DisplayName("Left factory method does not alter value")
    public void testLeft() {
        Object o = new Object();
        Either<Object, Object> either = Either.left(o);
        assertThat(either.isRight()).isFalse();
        assertThat(either.getLeft()).isEqualTo(o);
    }
    @Test
    @DisplayName("Right factory method does not alter value")
    public void testRight() {
        Object o = new Object();
        Either<Object, Object> either = Either.right(o);
        assertThat(either.isRight()).isTrue();
        assertThat(either.getRight()).isEqualTo(o);
    }

    @Test
    @DisplayName("getLeft() throws IllegalStateException if Either is a Right")
    public void testGetLeft() {
        Either<Object, Object> either = Either.right(new Object());
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(either::getLeft);
    }
    @Test
    @DisplayName("getRight() throws IllegalStateException if Either is a Left")
    public void testGetRight() {
        Either<Object, Object> either = Either.left(new Object());
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(either::getRight);
    }

    @Test
    @DisplayName("Calling mapLeft() on a Left applies the mapper")
    public void testMapLeft() {
        Function<Integer, Integer> mapper = x -> x * 2;
        int i = 2;
        Either<Integer, Integer> either = Either.left(i);
        Either<Integer, Integer> mappedEither = either.mapLeft(mapper);
        assertThat(mappedEither.isRight()).isFalse();
        assertThat(mappedEither.getLeft()).isEqualTo(mapper.apply(i));
    }
    @Test
    @DisplayName("Calling mapLeft() on a Right keeps the value")
    public void testMapLeftOnRight() {
        Function<Integer, Integer> mapper = x -> x * 2;
        int i = 2;
        Either<Integer, Integer> either = Either.right(i);
        Either<Integer, Integer> mappedEither = either.mapLeft(mapper);
        assertThat(mappedEither.isRight()).isTrue();
        assertThat(mappedEither.getRight()).isEqualTo(i);
    }
    @Test
    @DisplayName("Calling mapRight() on a Right applies the mapper")
    public void testMapRight() {
        Function<Integer, Integer> mapper = x -> x * 2;
        int i = 2;
        Either<Integer, Integer> either = Either.right(i);
        Either<Integer, Integer> mappedEither = either.mapRight(mapper);
        assertThat(mappedEither.isRight()).isTrue();
        assertThat(mappedEither.getRight()).isEqualTo(mapper.apply(i));
    }
    @Test
    @DisplayName("Calling mapRight() on a Left keeps the value")
    public void testMapRightOnLeft() {
        Function<Integer, Integer> mapper = x -> x * 2;
        int i = 2;
        Either<Integer, Integer> either = Either.left(i);
        Either<Integer, Integer> mappedEither = either.mapRight(mapper);
        assertThat(mappedEither.isRight()).isFalse();
        assertThat(mappedEither.getLeft()).isEqualTo(i);
    }

    @Test
    @DisplayName("Folding a Left applies the left mapper")
    public void testFoldOnLeft() {
        Function<String, Integer> mapper = String::length;
        Function<String, Integer> wrongMapper = i -> -1;
        String s = "A string";
        Either<String, String> either = Either.left(s);
        assertThat(either.fold(mapper, wrongMapper)).isEqualTo(s.length());
    }
    @Test
    @DisplayName("Folding a Right applies the right mapper")
    public void testFoldOnRight() {
        Function<String, Integer> mapper = String::length;
        Function<String, Integer> wrongMapper = i -> -1;
        String s = "A string";
        Either<String, String> either = Either.right(s);
        assertThat(either.fold(wrongMapper, mapper)).isEqualTo(s.length());
    }

}
