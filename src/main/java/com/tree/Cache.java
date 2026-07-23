package com.tree;

import java.util.Map;

public record Cache(Map<Integer, Status> cached) { }
