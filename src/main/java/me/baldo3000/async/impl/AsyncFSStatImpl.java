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

    private final FileSystem fileSystem;

    public AsyncFSStatImpl(Vertx vertx) {
        this.fileSystem = vertx.fileSystem();
    }

    public Future<Integer> getFSReport(Path directory) {
        IO.println("Generating FS report for directory: " + directory);
        return getFSReportRecursive(directory.toString(), new HashSet<>());
    }

    private Future<Integer> getFSReportRecursive(String directory, Set<String> visited) {
        Promise<Integer> promise = Promise.promise();

        if (!visited.add(directory)) {
            return Future.succeededFuture(0);
        }
        //log("Recursive step");

        this.fileSystem.readDir(directory).onFailure(promise::fail).onSuccess(paths -> {
            final List<Future<Integer>> futures = new ArrayList<>();
            for (var path : paths) {
                Future<Integer> f = this.fileSystem.lprops(path).compose(props -> {
                    if (props.isRegularFile()) {
                        return Future.succeededFuture(1);
                    } else if (props.isDirectory()) {
                        return getFSReportRecursive(path, visited);
                    } else {
                        return Future.succeededFuture(0);
                    }
                }).recover(_ -> Future.succeededFuture(0));
                futures.add(f);
            }

            Future.all(futures).onSuccess(composite -> {
                var counter = 0;
                for (var res : composite.list()) {
                    if (res instanceof Integer value) {
                        counter = counter + value;
                    }
                }
                promise.complete(counter);
            }).onFailure(promise::fail);
        });

        return promise.future();
    }

    private void log(String msg) {
        System.out.println("[ " + System.currentTimeMillis() + " ][ " + Thread.currentThread() + " ] " + msg);
    }
}
