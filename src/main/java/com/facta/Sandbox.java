package com.facta;

import java.util.function.Function;

/**
 * Unsafe class that deals with Threads, States and IO.
 */
public interface Sandbox<B> {

    Status status();
    void spin(Function<B, Boolean> action);

    void close();
}
