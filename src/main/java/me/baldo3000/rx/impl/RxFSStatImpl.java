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

public class RxFSStatImpl implements RxFSStat {

    @Override
    public Flowable<FSReport> getFSReport(Path path, long maxFileSize, int bands) {

        return Flowable.virtualCreate(emitter -> {
            BasicFileAttributes attributes;
            try {
                attributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            } catch (IOException e) {
                emitter.emit(new ArrayFSReport(path.toString(), maxFileSize, bands));
                return;
            }
            // Skipping symlinks
            if (attributes.isSymbolicLink() || attributes.isOther()) {
                return;
            }
            var report = new ArrayFSReport(path.toString(), maxFileSize, bands);
//            if ("C:\\Users\\andre\\AppData".equals(report.getDirectory())) log("mow");
            if (attributes.isRegularFile()) {
                emitter.emit(report.countFileBySize(attributes.size()));

            } else if (attributes.isDirectory()) {
                var subPaths = path.toFile().listFiles();
                if (subPaths == null || subPaths.length == 0) {
                    return;
                }
                var childReports = new HashMap<String, FSReport>();
                Flowable.fromArray(subPaths)
                        .flatMap(subPath -> getFSReport(subPath.toPath(), maxFileSize, bands))
                        .blockingSubscribe(childReport -> {
//                            if ("C:\\Users\\andre\\AppData".equals(report.getDirectory())) {
//                                log("Merging: " + childReport.getDirectory());
//                                log("Report being generated inside: " + report);
//                            }
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
