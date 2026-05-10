package me.baldo3000.rx.impl;

import io.reactivex.rxjava4.core.Flowable;
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

public class RxFSStatImpl implements RxFSStat {

    @Override
    public Flowable<FSReport> getFSReport(Path directory, long maxFileSize, int bands) {
        return Flowable.virtualCreate(emitter -> {
            var report = new ArrayFSReport(directory, maxFileSize, bands);

            try {
                var attrs = Files.readAttributes(directory, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);

                if (attrs.isRegularFile()) {
                    emitter.emit(report.countFileBySize(attrs.size()));
                } else if (attrs.isDirectory()) {
                    try (var dirStream = Files.list(directory)) {
                        var subPaths = dirStream.toList();
                        var childReports = new HashMap<Path, FSReport>(subPaths.size() * 2);
                        Flowable.fromIterable(subPaths)
                                .flatMap(subPath -> getFSReport(subPath, maxFileSize, bands))
                                .blockingSubscribe(childReport -> {
                                    FSReport previous = childReports.put(childReport.getDirectory(), childReport);
                                    if (previous != null) {
                                        report.subtract(previous);
                                    }
                                    report.merge(childReport);
                                    emitter.emit(report.copy());
                                });
                    }
                }
            } catch (IOException | UncheckedIOException e) {
                log("Skipping " + directory + ": " + e.getMessage());
                emitter.emit(report); // Emit empty report
            }
        });
    }

    private void log(String msg) {
        IO.println("[" + Thread.currentThread() + "] " + msg);
    }
}
