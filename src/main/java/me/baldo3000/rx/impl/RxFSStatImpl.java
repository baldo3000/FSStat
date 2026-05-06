package me.baldo3000.rx.impl;

import io.reactivex.rxjava4.core.Flowable;
import me.baldo3000.common.api.FSReport;
import me.baldo3000.common.impl.ArrayFSReport;
import me.baldo3000.rx.api.RxFSStat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.stream.StreamSupport;

public class RxFSStatImpl implements RxFSStat {

    @Override
    public Flowable<FSReport> getFSReport(Path path, long maxFileSize, int bands) {

        return Flowable.virtualCreate(emitter -> {
            var report = new ArrayFSReport(path, maxFileSize, bands);
            // Reading file attributes
            BasicFileAttributes attributes;
            try {
                attributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            } catch (IOException e) {
                emitter.emit(report);
                return;
            }

            // Skipping symlinks
            if (attributes.isSymbolicLink() || attributes.isOther()) {
                return;
            }

            if (attributes.isRegularFile()) {
                emitter.emit(report.countFileBySize(attributes.size()));
            } else if (attributes.isDirectory()) {
                List<Path> subPaths;
                try (var stream = Files.newDirectoryStream(path)) {
                    subPaths = StreamSupport.stream(stream.spliterator(), false).toList();
                } catch (IOException e) { // Error listing directory
                    emitter.emit(report);
                    return;
                }
                if (subPaths.isEmpty()) {
                    emitter.emit(report); // Avoid creating a Flowable from an empty iterable
                    return;
                }
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
        });
    }

    private void log(String msg) {
        IO.println("[" + Thread.currentThread() + "] " + msg);
    }
}
