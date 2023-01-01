package com.github.romualdrousseau.any2json.util;

public class Disk {
    public static String changeExtension(final String filename, final String newExtentsion) {
        final int index = filename.lastIndexOf(".");
        if (index < 0) {
            return filename + "." + newExtentsion;
        } else {
            return filename.substring(0, index) + "." + newExtentsion;
        }
    }

    public static String removeExtension(String fileName) {
        if(fileName == null) {
            return null;
        }
        return fileName.replaceFirst("[.][^.]+$", "");
    }
}
