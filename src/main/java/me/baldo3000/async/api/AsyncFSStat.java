package me.baldo3000.async.api;

import io.vertx.core.Future;

import java.nio.file.Path;

public interface AsyncFSStat {
    /**
     * Generates an FS report asynchronously of the specified directory
     *
     * @param directory the directory
     * @return the future of the computation
     */
    Future<Integer> getFSReport(Path directory);
}
