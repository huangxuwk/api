package com.dl.test5;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class LRUCache {
    private Map<Integer, Integer> cache = new HashMap<>();
    private LinkedList<Integer> list = new LinkedList<>();
    private int capacity;

    public LRUCache(int capacity) {
        this.capacity = capacity;
    }
    
    public int get(int key) {
        Integer value = cache.get(key);
        if (value != null) {
            list.remove((Integer)key);
            list.add(key);
            return value;
        }
        return -1;
    }
    
    public void put(int key, int value) {
        if (cache.containsKey(key)) {
            list.remove((Integer)key);
            list.add(key);
            cache.put(key, value);
            return;
        }    
        if (cache.size() == capacity) {
            cache.remove(list.get(0));
            list.remove(0);
        }
        cache.put(key, value);
        list.add(key);
    }

}
