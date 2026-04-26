package me.baldo3000.async.impl;

import io.vertx.core.Future;
import io.vertx.core.Promise;
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
        var dir = directory.toString();
        IO.println("Generating FS report for directory: " + dir);
        return getFSReportRecursive(dir, new HashSet<>(), new ArrayFSReport(dir, maxFileSize, bands));
    }

    private Future<FSReport> getFSReportRecursive(String directory, Set<String> visited, FSReport report) {
        Promise<FSReport> promise = Promise.promise();

        if (!visited.add(directory)) {
            return Future.succeededFuture(report);
        }
        //log("Recursive step");

        this.fileSystem.readDir(directory).onFailure(promise::fail).onSuccess(paths -> {
            final List<Future<FSReport>> futures = new ArrayList<>(paths.size());
            for (var path : paths) {
                //this.fileSystem.
                Future<FSReport> f = this.fileSystem.lprops(path).compose(props -> {
                    if (props.isRegularFile()) {
                        report.countFileBySize(props.size());
                        return Future.succeededFuture(report);
                    } else if (props.isDirectory()) {
                        return getFSReportRecursive(path, visited, report);
                    } else {
                        return Future.succeededFuture(report);
                    }
                }).recover(_ -> Future.succeededFuture(report));
                futures.add(f);
            }

            Future.all(futures)
                    .onSuccess(_ -> promise.complete(report))
                    .onFailure(promise::fail);
        });

        return promise.future();
    }

    private void log(String msg) {
        IO.println("[ " + System.currentTimeMillis() + " ][ " + Thread.currentThread() + " ] " + msg);
    }
}
