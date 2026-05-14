package me.baldo3000.rx.impl;

import io.reactivex.rxjava4.core.Flowable;
import io.reactivex.rxjava4.schedulers.Schedulers;
import me.baldo3000.common.api.FSReport;
import me.baldo3000.common.impl.ArrayFSReport;
import me.baldo3000.rx.api.RxFSStat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;

public class RxFSStatImpl implements RxFSStat {

    @Override
    public Flowable<FSReport> getFSReport(Path directory, long maxFileSize, int bands) {
        return Flowable.defer(() -> {
            try {
                // Read directory attributes
                final var attrs = Files.readAttributes(
                        directory, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);

                // If it's a file, create a report with just that file
                if (attrs.isRegularFile()) {
                    var report = new ArrayFSReport(directory, maxFileSize, bands);
                    return Flowable.just(report.countFileBySize(attrs.size()));
                }

                // If it's a directories, create a report dynamically merging the subDirectories reports
                if (attrs.isDirectory()) {
                    final List<Path> subPaths;
                    try (var dirStream = Files.list(directory)) {
                        subPaths = dirStream.toList();
                    }

                    var parentReport = new ArrayFSReport(directory, maxFileSize, bands);
                    var childReports = new HashMap<Path, FSReport>(subPaths.size() * 2);

                    // FlatMap guarantees serial delivery to all downstream operators including map
                    // Access to parentReport and childReports is done sequentially
                    return Flowable.fromIterable(subPaths)
                            .flatMap(subPath -> getFSReport(subPath, maxFileSize, bands))
                            .map(childReport -> {
                                final FSReport previous = childReports.put(
                                        childReport.getDirectory(), childReport);
                                if (previous != null) parentReport.subtract(previous);
                                parentReport.merge(childReport);
                                return parentReport.copy();
                            });
                }

            } catch (IOException | UncheckedIOException e) {
                log("Skipping " + directory + ": " + e.getMessage());
            }

            // Default case if error is thrown or the directory is a symlink
            return Flowable.empty();
        }).subscribeOn(Schedulers.virtual()); // Subscribe on virtual threads schedulers for blocking operations
    }

    private void log(String msg) {
        IO.println("[" + Thread.currentThread() + "] " + msg);
    }
}
