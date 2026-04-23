package me.baldo3000.async.api;

import io.vertx.core.Future;

import java.nio.file.Path;
import java.util.List;

public interface AsyncFSStat {
    /**
     * Generates an FS report asynchronously of the specified directory
     *
     * @param directory the directory
     * @return the future of the computation
     */
    Future<List<String>> getFSReport(Path directory);

    /**
     * Closes the {@code AsyncFSStat} instance, releasing all resources.
     */
    Future<Void> close();
}
