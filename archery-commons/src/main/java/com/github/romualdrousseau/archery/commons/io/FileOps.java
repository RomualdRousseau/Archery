package com.github.romualdrousseau.archery.commons.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import com.github.romualdrousseau.archery.commons.strings.StringUtils;

public class FileOps {

    public static BufferedReader createBufferedReaderUtfBOM(final Path filePath) throws IOException {
        return processUtfBOM(new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath.toFile()), StandardCharsets.UTF_8)));
    }

    public static BufferedReader processUtfBOM(final BufferedReader reader) throws IOException {
        reader.mark(1);
        if (reader.read() != StringUtils.BOM_CHAR) {
            reader.reset(); // skip BOM if present
        }
        return reader;
    }

    public static void copyDir(final Path src, final Path dest) {
        try {
            Files.walk(src).forEach(source -> copyFile(source, dest.resolve(src.relativize(source))));
        } catch (final IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    public static void deleteDir(final Path dir) {
        try {
            if (dir.toFile().exists()) {
                Files.walk(dir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }
            assert !dir.toFile().exists();
        } catch (final IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    public static void copyFile(final Path src, final Path dest) {
        try {
            dest.getParent().toFile().mkdirs();
            Files.copy(src, dest);
        } catch (final IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    public static void renameFileName(final Path filename1, final Path filename2) {
        final var file1 = filename1.toFile();
        final var file2 = filename2.toFile();
        file1.renameTo(file2);
    }

    public static String getExtension(final String filename) {
        if (filename == null) {
            return filename;
        }
        final var index = filename.lastIndexOf(".");
        if (index < 0) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }

    public static String changeExtension(final String filename, final String newExtension) {
        if (filename == null || newExtension == null) {
            return filename;
        }
        return FileOps.removeExtension(filename) + "." + newExtension;
    }

    public static String removeExtension(final String filename) {
        if (filename == null) {
            return filename;
        }
        final var index = filename.lastIndexOf(".");
        if (index < 0) {
            return filename;
        } else {
            return filename.substring(0, index);
        }
    }
}
