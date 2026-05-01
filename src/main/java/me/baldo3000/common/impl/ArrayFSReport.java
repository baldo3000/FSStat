package me.baldo3000.common.impl;

import me.baldo3000.common.api.FSReport;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ArrayFSReport implements FSReport {

    private final String directory;
    private final long maxFileSize;
    private final int bands;
    private final long bandWidth;
    private final long[] filesDistribution;
    private long totalFiles = 0L;

    public ArrayFSReport(String directory, long maxFileSize, int bands) {
        if (maxFileSize < 0L) {
            throw new IllegalArgumentException("Max file size must be non-negative: " + maxFileSize);
        }
        if (bands < 1) {
            throw new IllegalArgumentException("Bands must be >= 1: " + bands);
        }

        this.directory = Objects.requireNonNull(directory, "Directory must not be null");
        this.maxFileSize = maxFileSize;
        this.bands = bands;
        this.filesDistribution = new long[bands + 1];
        this.bandWidth = maxFileSize / bands;
    }

    @Override
    public String getDirectory() {
        return this.directory;
    }

    @Override
    public int getBands() {
        return this.bands;
    }

    @Override
    public long getTotalFiles() {
        return this.totalFiles;
    }

    @Override
    public List<Long> getFilesDistribution() {
        return Arrays.stream(this.filesDistribution).boxed().toList();
    }

    @Override
    public void countFileBySize(long size) {
        this.filesDistribution[sizeToBandIndex(size)]++;
        this.totalFiles++;
    }

    @Override
    public void merge(FSReport other) {
        if (this.bands != other.getBands()) {
            throw new IllegalArgumentException("Can't merge the two FS Reports, different bands: " + this.bands + " != " + other.getBands());
        }
        var otherDistribution = other.getFilesDistribution();
        for (int i = 0; i <= this.bands; i++) {
            this.filesDistribution[i] += otherDistribution.get(i);
        }
    }

    @Override
    public String toString() {
        return "[FSReport] Directory: " + this.directory + ", Files: " + this.totalFiles + ", Bands: " + Arrays.toString(this.filesDistribution);
    }

    private int sizeToBandIndex(long size) {
        if (size > this.maxFileSize) {
            return this.bands;
        }
        return (int) (size / this.bandWidth);
    }
}
