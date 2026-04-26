package me.baldo3000.async.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import me.baldo3000.async.api.AsyncFSStat;
import me.baldo3000.common.api.FSReport;
import me.baldo3000.common.impl.ArrayFSReport;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AsyncFSStatImpl implements AsyncFSStat {

    private final FileSystem fileSystem;

    public AsyncFSStatImpl(Vertx vertx) {
        this.fileSystem = vertx.fileSystem();
    }

    public Future<FSReport> getFSReport(Path directory, long maxFileSize, int bands) {
        IO.println("Generating FS report for directory: " + directory);

        var dir = directory.toString();
        var report = new ArrayFSReport(dir, maxFileSize, bands);
        return getFSReportRecursive(dir, new HashSet<>(), report).map(_ -> report);
    }

    private Future<Void> getFSReportRecursive(String directory, Set<String> visited, FSReport report) {
        if (!visited.add(directory)) {
            return Future.succeededFuture();
        }
        //log("Recursive step");

        return this.fileSystem.readDir(directory)
                .recover(_ -> Future.succeededFuture(List.of()))
                .compose(paths -> {
                    if (paths.isEmpty()) {
                        return Future.succeededFuture();
                    }
                    final List<Future<Void>> futures = new ArrayList<>(paths.size());
                    for (var path : paths) {
                        Future<Void> f = this.fileSystem.lprops(path).compose(props -> {
                            if (props.isRegularFile()) {
                                report.countFileBySize(props.size());
                                return Future.succeededFuture();
                            } else if (props.isDirectory()) {
                                return getFSReportRecursive(path, visited, report);
                            }
                            return Future.succeededFuture();
                        }, _ -> Future.succeededFuture());
                        futures.add(f);
                    }

                    return Future.all(futures).mapEmpty();
                });
    }

    private void log(String msg) {
        IO.println("[ " + System.currentTimeMillis() + " ][ " + Thread.currentThread() + " ] " + msg);
    }
}
