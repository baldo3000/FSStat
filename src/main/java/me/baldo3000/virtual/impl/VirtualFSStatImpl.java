package me.baldo3000.virtual.impl;

import me.baldo3000.common.api.FSReport;
import me.baldo3000.common.impl.ArrayFSReport;
import me.baldo3000.virtual.api.CollectorLatch;
import me.baldo3000.virtual.api.VirtualFSStat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.CompletableFuture;

public class VirtualFSStatImpl implements VirtualFSStat {
    @Override
    public CompletableFuture<FSReport> getFSReport(Path directory, long maxFileSize, int bands) {
        return CompletableFuture.supplyAsync(() -> getFSReportRecursive(directory, maxFileSize, bands));
    }

    private FSReport getFSReportRecursive(Path path, long maxFileSize, int bands) {
        var report = new ArrayFSReport(path, maxFileSize, bands);

        try {
            var attrs = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);

            if (attrs.isRegularFile()) {
                return report.countFileBySize(attrs.size());
            }

            if (attrs.isDirectory()) {
                try (var dirStream = Files.list(path)) {
                    var subPaths = dirStream.toList();
                    CollectorLatch<FSReport> collector = new CollectorLatchImpl<>(subPaths.size());
                    subPaths.forEach(subPath ->
                            Thread.startVirtualThread(() -> {
                                var childReport = getFSReportRecursive(subPath, maxFileSize, bands);
                                collector.putValue(childReport);
                            }));
                    collector.awaitValues().forEach(report::merge);
                }
            }
        } catch (IOException | UncheckedIOException e) {
            log("Skipping " + path + ": " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // restore interrupted status
        }

        return report;
    }

    private void log(String msg) {
        IO.println("[ " + System.currentTimeMillis() + " ][ " + Thread.currentThread() + " ] " + msg);
    }
}
