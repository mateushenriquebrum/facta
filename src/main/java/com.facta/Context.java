package com.facta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Context {
    Map<Integer, Node.Status> cached = new HashMap<>();
    List<Integer> active = new ArrayList<>();

    public void prepareCollectActive() {
        active.clear();
    }

    public void removeInactive() {
        cached.keySet().retainAll(active);
    }
}
