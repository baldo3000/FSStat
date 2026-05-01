package me.baldo3000.rx.api;

import io.reactivex.rxjava4.core.Flowable;
import me.baldo3000.common.api.FSReport;

import java.nio.file.Path;

public interface RxFSStat {
    /**
     * Generates an FS report reactively of the specified directory.
     *
     * @param directory   the directory
     * @param maxFileSize the maximum file size, representing the last band
     * @param bands       the number of bands
     * @return a {@code Flowable} representing the FS Report that is being generated
     */
    Flowable<FSReport> getFSReport(Path directory, long maxFileSize, int bands);
}
