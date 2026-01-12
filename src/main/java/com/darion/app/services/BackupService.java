package com.darion.app.services;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BackupService {

    public void deleteAll(Path parent, String saveFileName) throws IOException {
        try (Stream<Path> stream = Files.walk(parent)) {
            for (Path path : stream.toList()) {
                String currentSaveFileName = path.getFileName().toString().toLowerCase();
                String base = saveFileName.toLowerCase();

                boolean isOriginal = currentSaveFileName.equals(base + ".zip");
                boolean isNumbered = currentSaveFileName.startsWith(base + " (")
                        && currentSaveFileName.endsWith(").zip");

                if (isOriginal || isNumbered)
                    Files.delete(path);
            }
        }
    }

    public void unzip(Path targetDir, Path zipFile) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                Path resolved = targetDir.resolve(zipEntry.getName()).normalize();

                if (!resolved.startsWith(targetDir.normalize()))
                    throw new IOException("Zip entry is outside target dir");

                if (zipEntry.isDirectory())
                    Files.createDirectories(resolved);
                else {
                    Files.createDirectories(resolved.getParent());

                    try (OutputStream os = Files.newOutputStream(resolved)) {
                        zis.transferTo(os);
                    }
                }
                zis.closeEntry();
            }
        }
    }

    public void zip(Path targetDir, Path zipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile));
                Stream<Path> streamOfFiles = Files.walk(targetDir)) {
            for (Path path : streamOfFiles.filter(Files::isRegularFile).toList()) {
                Path relative = targetDir.relativize(path);

                ZipEntry zipEntry = new ZipEntry(relative.toString().replace("\\", "/"));
                zos.putNextEntry(zipEntry);

                Files.copy(path, zos);
                zos.closeEntry();
            }
        }
    }
}
