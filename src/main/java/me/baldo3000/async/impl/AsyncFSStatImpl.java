package me.baldo3000.async.impl;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import me.baldo3000.async.api.AsyncFSStat;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AsyncFSStatImpl implements AsyncFSStat {

    private final Vertx vertx;
    private final FileSystem fileSystem;

    public AsyncFSStatImpl() {
        this.vertx = Vertx.vertx();
        this.fileSystem = vertx.fileSystem();
    }

    public Future<List<String>> getFSReport(Path directory) {
        IO.println("Generating FS report for directory: " + directory);
        return getFSReportRecursive(directory.toString(), new HashSet<>());
    }

    private Future<List<String>> getFSReportRecursive(String directory, Set<String> visited) {
        Promise<List<String>> promise = Promise.promise();

        if (!visited.add(directory)) {
            return Future.succeededFuture(List.of());
        }
        // log("Recursive step");

        this.fileSystem.readDir(directory).onFailure(promise::fail).onSuccess(paths -> {
            final List<Future<List<String>>> futures = new ArrayList<>();
            for (var path : paths) {
                Future<List<String>> f = this.fileSystem.lprops(path).compose(props -> {
                    if (props.isRegularFile()) {
                        return Future.succeededFuture(List.of(path));
                    } else if (props.isDirectory()) {
                        return getFSReportRecursive(path, visited);
                    } else {
                        return Future.succeededFuture(List.of());
                    }
                }).recover(_ -> Future.succeededFuture(List.of()));
                futures.add(f);
            }

            Future.all(futures).onSuccess(composite -> {
                final List<String> files = new ArrayList<>();
                for (var res : composite.list()) {
                    if (res instanceof List<?> list) {
                        files.addAll((List<? extends String>) list);
                    }
                }
                promise.complete(new ArrayList<>(files));
            }).onFailure(promise::fail);
        });

        return promise.future();
    }

    private void log(String msg) {
        System.out.println("[ " + System.currentTimeMillis() + " ][ " + Thread.currentThread() + " ] " + msg);
    }

    @Override
    public Future<Void> close() {
        return this.vertx.close();
    }
}
