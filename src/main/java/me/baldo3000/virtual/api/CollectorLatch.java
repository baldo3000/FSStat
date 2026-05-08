package me.baldo3000.virtual.api;

import java.util.List;

/**
 * A latch representing the collection of different values,
 * enabling blocking until all the values have been collected.
 *
 * @param <T> the type of collected results
 */
public interface CollectorLatch<T> {
    /**
     * Puts a values inside this collector latch.
     *
     * @param value the value to put inside the collector
     */
    void putValue(T value);

    /**
     * Wait until everyone has put its value inside the collector latch and then returns the results.
     *
     * @return a list of results
     * @throws InterruptedException if thread is interrupted
     */
    List<T> awaitValues() throws InterruptedException;
}
