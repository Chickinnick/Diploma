package com.umbaba.bluetoothvswifidirect.testdata;

import java.io.File;

public interface TestFileModel {
    int FILE_5 = 5;
    int FILE_10 = 10;
    int FILE_20 = 20;

    File getFile(int size);

    File get5MBFile();

    File get10MBFile();

    File get20MBFile();

}
