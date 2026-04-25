package me.baldo3000.async.api;

import io.vertx.core.Future;
import me.baldo3000.common.api.FSReport;

import java.nio.file.Path;

public interface AsyncFSStat {
    /**
     * Generates an FS report asynchronously of the specified directory.
     *
     * @param directory   the directory
     * @param maxFileSize the maximum file size, representing the last band
     * @param bands       the number of bands
     * @return the future of the computation
     */
    Future<FSReport> getFSReport(Path directory, long maxFileSize, int bands);
}
