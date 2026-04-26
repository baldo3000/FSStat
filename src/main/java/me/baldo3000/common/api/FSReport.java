package me.baldo3000.common.api;

import java.util.List;

/**
 * An FS Report indicating the number of files found in a specific directory, divided in bands based on size.
 */
public interface FSReport {

    /**
     * The directory for which the FS Report was created.
     *
     * @return the directory
     */
    String getDirectory();

    /**
     * The total numbers of files found inside the directory.
     *
     * @return the number of files
     */
    long getTotalFiles();

    /**
     * The distribution of files in bands based on their size.
     *
     * @return a List representing the distribution of files in bands
     */
    List<Long> getFilesDistribution();

    /**
     * Count a new file to the FS Report by specifying its size.
     * <p>The size of the file is assumed to be a positive number.
     *
     * @param size the file size
     */
    void countFileBySize(long size);
}
