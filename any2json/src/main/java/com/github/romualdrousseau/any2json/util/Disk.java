package com.github.romualdrousseau.any2json.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Disk
{
    private static final List<String> DANGEROUS_PATH = List.of("..", ".");

    public static void copyDir(Path src, Path dest) throws IOException {
        Files.walk(src).forEach(source -> copyFile(source, dest.resolve(src.relativize(source))));
    }

    public static void deleteDir(final Path dir) throws IOException {
        if (dir.toFile().exists()) {
            Files.walk(dir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
        assert !dir.toFile().exists();
    }

    public static void zipDir(final Path sourceDirPath, final File zipFilePath) throws IOException {
        try (final var zs = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
            Files.walk(sourceDirPath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        final var zipEntry = new ZipEntry(sourceDirPath.relativize(path).toString().replace("\\", "/"));
                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(path, zs);
                            zs.closeEntry();
                        } catch (final IOException x) {
                            throw new RuntimeException(x);
                        }
                    });
        }
    }

    public static void unzipDir(final Path zipFile, final Path folder) throws IOException {
        final byte[] buffer = new byte[4096];
        try (final var zis = new ZipInputStream(new FileInputStream(zipFile.toFile()))) {
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                if (DANGEROUS_PATH.contains(ze.getName())) {
                    continue;
                }

                final var newFile = folder.resolve(ze.getName());

                // Ensure parent directory exists
                newFile.getParent().toFile().mkdirs();

                if (!ze.isDirectory()) {
                    try (final var fos = new FileOutputStream(newFile.toFile())) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
        }
    }

    public static void copyFile(Path src, Path dest) {
        try {
            dest.getParent().toFile().mkdirs();
            Files.copy(src, dest);
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    public static void removeFileName(final Path filename1, final Path filename2) {
        final var file1 = filename1.toFile();
        final var file2 = filename2.toFile();
        file1.renameTo(file2);
    }

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
