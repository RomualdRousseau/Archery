package com.github.romualdrousseau.archery.commons.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;

/**
 * This class provides functionality for working with zip files.
 * It includes methods for zipping and unzipping files, as well as
 * methods for adding and removing files from an existing zip file.
 */
public class Zip {

    public static String zip(final String s) throws IOException {
        try (
                final var fos = new ByteArrayOutputStream();
                final var zos = new ZipOutputStream(fos)) {
            final var ze = new ZipParameters();
            ze.setFileNameInZip("string");
            zos.putNextEntry(ze);
            zos.write(s.getBytes("UTF-8"));
            zos.closeEntry();
            zos.close();
            return Base64.getEncoder().encodeToString(fos.toByteArray());
        }
    }

    public static String unzip(final String s) throws IOException {
        try (
                final var fos = new ByteArrayOutputStream();
                final var zis = new ZipInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(s)))) {
            final var buffer = new byte[4096];
            final var ze = zis.getNextEntry();
            if (ze != null) {
                if (ze.getFileName().equals("string")) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
            return new String(fos.toByteArray(), "UTF-8");
        }
    }

    public static void zip(final Path sourceDirPath, final File zipFilePath) throws IOException {
        Zip.zip(sourceDirPath, zipFilePath.toPath());
    }

    public static void zip(final File sourceDirPath, final Path zipFilePath) throws IOException {
        Zip.zip(sourceDirPath.toPath(), zipFilePath);
    }

    public static void zip(final File sourceDirPath, final File zipFilePath) throws IOException {
        Zip.zip(sourceDirPath.toPath(), zipFilePath.toPath());
    }

    public static void zip(final Path sourceDirPath, final Path zipFilePath) throws IOException {
        try (final var zos = new ZipOutputStream(new FileOutputStream(zipFilePath.toFile()))) {
            Files.walk(sourceDirPath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        final var ze = new ZipParameters();
                        ze.setFileNameInZip(sourceDirPath.relativize(path).toString().replace("\\", "/"));
                        try {
                            zos.putNextEntry(ze);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (final IOException x) {
                            throw new RuntimeException(x);
                        }
                    });
        }
    }

    public static void unzip(final Path zipFile, final File destFolder) throws IOException {
        Zip.unzip(zipFile, destFolder.toPath(), null, null);
    }

    public static void unzip(final File zipFile, final Path destFolder) throws IOException {
        Zip.unzip(zipFile.toPath(), destFolder, null, null);
    }

    public static void unzip(final File zipFile, final File destFolder) throws IOException {
        Zip.unzip(zipFile.toPath(), destFolder.toPath(), null, null);
    }

    public static void unzip(final Path zipFile, final Path destFolder) throws IOException {
        Zip.unzip(zipFile, destFolder, null, null);
    }

    public static void unzip(final Path zipFile, final File destFolder, final char[] password, final Charset charset)
            throws IOException {
        Zip.unzip(zipFile, destFolder.toPath(), password, charset);
    }

    public static void unzip(final File zipFile, final Path destFolder, final char[] password, final Charset charset)
            throws IOException {
        Zip.unzip(zipFile.toPath(), destFolder, password, charset);
    }

    public static void unzip(final File zipFile, final File destFolder, final char[] password, final Charset charset)
            throws IOException {
        Zip.unzip(zipFile.toPath(), destFolder.toPath(), password, charset);
    }

    public static void unzip(final Path zipFile, final Path destFolder, final char[] password, final Charset charset)
            throws IOException {
        if (!destFolder.toFile().exists()) {
            destFolder.toFile().mkdir();
        }

        try (final var zis = new ZipInputStream(new FileInputStream(zipFile.toFile()), password, charset)) {
            final var buffer = new byte[4096];
            var ze = zis.getNextEntry();
            while (ze != null) {
                final var fileName = ze.getFileName();
                final var newFile = destFolder.toAbsolutePath().resolve(fileName).toFile();

                // Create all non exists folders to avoid FileNotFoundException
                newFile.getParentFile().mkdirs();

                if (!ze.isDirectory()) {
                    try (final var fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                // zis.close();
                ze = zis.getNextEntry();
            }
        }
    }
}
