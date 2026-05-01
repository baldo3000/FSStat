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

public class RxFSStatImpl implements RxFSStat {

    @Override
    public Flowable<FSReport> getFSReport(Path path, long maxFileSize, int bands) {

        return Flowable.virtualCreate(emitter -> {
            var report = new ArrayFSReport(path.toString(), maxFileSize, bands);
            //if ("C:/Users/andre/Documents/PROGRAMMAZIONE".equals(report.getDirectory())) log("mow");
            BasicFileAttributes attributes;
            try {
                attributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            } catch (IOException e) {
                emitter.emit(report);
                return;
            }

            // Skipping symlinks
            if (attributes.isSymbolicLink() || attributes.isOther()) {
                emitter.emit(report);
                return;
            }

            if (attributes.isRegularFile()) {
                emitter.emit(report.countFileBySize(attributes.size()));

            } else if (attributes.isDirectory()) {
                var children = path.toFile().listFiles();
                if (children == null) {
                    emitter.emit(report);
                    return;
                }
                Flowable.fromArray(children)
                        .flatMap(child -> getFSReport(child.toPath(), maxFileSize, bands).takeLast(1))
                        .blockingSubscribe(merged -> {
                                    report.merge(merged);
                                    /*if ("C:/Users/andre/Documents/PROGRAMMAZIONE".equals(report.getDirectory())) {
                                        log("Merging: " + merged.getDirectory());
                                        log("Report being generated inside: " + report.toString());
                                    }*/
                                }
                        );
                emitter.emit(report);
            }
        });
    }

    private void log(String msg) {
        IO.println("[" + Thread.currentThread() + "] " + msg);
    }
}
