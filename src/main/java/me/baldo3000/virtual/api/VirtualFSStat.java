package me.baldo3000.virtual.api;

import me.baldo3000.common.api.FSReport;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public interface VirtualFSStat {
    /**
     * Generates an FS report asynchronously of the specified directory.
     *
     * @param directory   the directory
     * @param maxFileSize the maximum file size, representing the last band
     * @param bands       the number of bands
     * @return a {@code CompletableFuture} of the computation
     */
    CompletableFuture<FSReport> getFSReport(Path directory, long maxFileSize, int bands);
}
