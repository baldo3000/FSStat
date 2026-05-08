package me.baldo3000.virtual.impl;

import me.baldo3000.virtual.api.CollectorLatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CollectorLatchImpl<T> implements CollectorLatch<T> {

    private final Lock mutex = new ReentrantLock();
    private final Condition noLeft = mutex.newCondition();
    private final List<T> values;
    private int left;

    public CollectorLatchImpl(int nValues) {
        this.left = nValues;
        this.values = new ArrayList<>(nValues);
    }

    @Override
    public void putValue(T value) {
        try {
            mutex.lock();
            if (left <= 0) throw new IllegalStateException("All values already submitted");
            this.values.add(value);
            left--;
            if (allArrived()) {
                noLeft.signalAll();
            }
        } finally {
            mutex.unlock();
        }
    }

    @Override
    public List<T> awaitValues() throws InterruptedException {
        try {
            mutex.lock();
            while (!allArrived()) {
                noLeft.await();
            }
            return Collections.unmodifiableList(this.values);
        } finally {
            mutex.unlock();
        }
    }

    private boolean allArrived() {
        return this.left == 0;
    }
}
