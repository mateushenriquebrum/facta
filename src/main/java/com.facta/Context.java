package com.facta;

import com.facta.Node.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Context {
    Map<Integer, Status> cached = new HashMap<>();
    List<Integer> active = new ArrayList<>();

    public Status prepare(Function<Context, Status> inContext) {
        Status result = inContext.apply(this);
        cached.keySet().retainAll(active);
        active.clear();
        return result;
    }
}
