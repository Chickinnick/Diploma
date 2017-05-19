package com.umbaba.bluetoothvswifidirect.testdata;

import java.io.File;

public interface TestFileModel {
    int FILE_5 = 39062;
    int FILE_10 = 78125;
    int FILE_20 = 156250;

    File getFile(int size);

    File get5MBFile();

    File get10MBFile();

    File get20MBFile();

}
