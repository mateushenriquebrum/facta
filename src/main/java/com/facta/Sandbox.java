package com.facta;

import com.facta.Tree.Ticked;

import java.util.function.Function;

/**
 * Unsafe class that deals with Threads, States and IO.
 */
public interface Sandbox<B> {
    Status status();
    void spin(Function<B, Boolean> ticked);
    void close();
}
