package me.baldo3000.async.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import me.baldo3000.async.api.AsyncFSStat;
import me.baldo3000.common.api.FSReport;
import me.baldo3000.common.impl.ArrayFSReport;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AsyncFSStatImpl implements AsyncFSStat {

    private sealed interface StatResult permits StatResult.RegularFile, StatResult.Directory, StatResult.Skip {
        record RegularFile(long size) implements StatResult {
        }

        record Directory(List<Path> subPaths) implements StatResult {
        }

        record Skip() implements StatResult {
        }
    }

    private final WorkerExecutor workerExecutor;

    public AsyncFSStatImpl(Vertx vertx) {
        this(vertx, Runtime.getRuntime().availableProcessors() * 2); // 2x tested optimal
    }

    public AsyncFSStatImpl(Vertx vertx, int workerThreads) {
        this.workerExecutor = vertx.createSharedWorkerExecutor(
                "fs-stat-worker",
                workerThreads,
                2,
                TimeUnit.MINUTES
        );
    }

    public Future<FSReport> getFSReport(Path directory, long maxFileSize, int bands) {
        var report = new ArrayFSReport(directory, maxFileSize, bands);
        return getFSReportRecursive(directory, report).map(_ -> report);
    }

    private Future<Void> getFSReportRecursive(Path directory, FSReport report) {
        return this.workerExecutor.<StatResult>executeBlocking(() -> {
            // Code below executed by the worker threads
            try {
                var attrs = Files.readAttributes(directory, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);

                if (attrs.isRegularFile()) {
                    return new StatResult.RegularFile(attrs.size());
                }

                if (attrs.isDirectory()) {
                    try (var dirStream = Files.list(directory)) {
                        var subPaths = dirStream.toList();
                        return new StatResult.Directory(subPaths);
                    }
                }
            } catch (IOException | UncheckedIOException e) {
                log("Skipping " + directory + ": " + e.getMessage());
            }

            return new StatResult.Skip();
        }, false).compose(result -> switch (result) {
            // Code below executed by the event-loop
            case StatResult.RegularFile(long size) -> {
                report.countFileBySize(size);
                yield Future.succeededFuture();
            }
            case StatResult.Directory(var subPaths) -> {
                var futures = subPaths.stream()
                        .map(child -> getFSReportRecursive(child, report))
                        .toList();
                yield Future.all(futures).mapEmpty();
            }
            case StatResult.Skip() -> Future.succeededFuture();
        });
    }

    @Override
    public void shutdown() {
        this.workerExecutor.close();
    }

    private void log(String msg) {
        IO.println("[ " + System.currentTimeMillis() + " ][ " + Thread.currentThread() + " ] " + msg);
    }
}
