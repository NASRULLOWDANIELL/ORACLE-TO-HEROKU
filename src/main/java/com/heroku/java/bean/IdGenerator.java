package com.heroku.java.bean;

import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {
    private static AtomicInteger counter = new AtomicInteger(0);

    public static int getNextId() {
        return counter.incrementAndGet();
    }

    public static void setCounter(int value) {
        counter.set(value);
    }
}