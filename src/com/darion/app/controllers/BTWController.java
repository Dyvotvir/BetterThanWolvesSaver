package com.darion.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BTWController {
    @FXML
    private Label chooseLabel, btwLabel, saverLabel, currentFileLabel, zipLabel;
    @FXML
    private Button chooseButton, createNewButton, overwriteButton, applyButton;

    private Path parent;
    private Path pathToSaveFile;
    private Path pathToZipFile;
    private int lastIndex = 0;

    @FXML
    public void initialize() throws IOException {
        lastIndex = Integer.parseInt(Files.readString(Path.of("C:\\Dev\\01_Personal\\Java\\BTWSaver\\src\\com\\darion\\app\\lastIndex")));
        pathToSaveFile = Path.of(Files.readString(Path.of("C:\\Dev\\01_Personal\\Java\\BTWSaver\\src\\com\\darion\\app\\pathToChosenSaveFile")));
        parent = pathToSaveFile.getParent();
        chooseLabel.setText(pathToSaveFile.toString());
        pathToZipFile = parent.resolve(pathToSaveFile.getFileName().toString() + ".zip");
        updateCurrentFileLabel();
    }

    @FXML
    public void onChooseFile() throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Stage stage = (Stage) chooseButton.getScene().getWindow();

        pathToSaveFile = directoryChooser.showDialog(stage).toPath();
        parent = pathToSaveFile.getParent();
        createPathToZipFile();


        chooseLabel.setText(pathToSaveFile.toString());

        Files.writeString(Path.of("C:\\Dev\\01_Personal\\Java\\BTWSaver\\src\\com\\darion\\app\\pathToChosenSaveFile"), pathToSaveFile.toString());
    }

    private void createPathToZipFile() {
        String nameOfSaveZipFile = pathToSaveFile.getFileName().toString() + ".zip";
        pathToZipFile = parent.resolve(nameOfSaveZipFile);
    }

    @FXML
    public void onDeleteLast() throws IOException {
        if (lastIndex == 0)
            return;

        String currentName;
        Path currentPath;

        currentName = (lastIndex == 1) ? "IMNOTBETTERTHANWOLVES.zip" : "IMNOTBETTERTHANWOLVES" + " (" + lastIndex  + ")" + ".zip";

        currentPath = parent.resolve(currentName);
        Files.delete(currentPath);
        lastIndex--;


        updateCurrentFileLabel();
        updateLastIndex();
    }

    @FXML
    public void onDeleteAll() throws IOException {
        try (Stream<Path> stream = Files.walk(parent)) {
            for (Path path : stream.toList())
                if (path.getFileName().toString().toLowerCase().endsWith(".zip"))
                    Files.delete(path);
        }
        lastIndex = 0;
        updateCurrentFileLabel();
    }

    @FXML
    public void onCreateNew() throws IOException {
        if (lastIndex == 0)
            zip(pathToSaveFile, pathToZipFile);
        else {
            String fileName = "IMNOTBETTERTHANWOLVES" + " (" + (lastIndex + 1) + ")" + ".zip";
            Path fileNamePath = parent.resolve(fileName);
            zip(pathToSaveFile, fileNamePath);

        }
        lastIndex++;
        updateCurrentFileLabel();
        updateLastIndex();
    }

    @FXML
    public void onOverwrite() throws IOException {
        if (lastIndex == 0)
            return;

        Path parent = pathToZipFile.getParent();
        if (lastIndex == 1)
            zip(pathToSaveFile, pathToZipFile);
        else {
            String currentName = "IMNOTBETTERTHANWOLVES" + " (" + lastIndex + ")" + ".zip";
            Path currentPath = parent.resolve(currentName);
            zip(pathToSaveFile, currentPath);
        }
        updateCurrentFileLabel();
    }

    @FXML
    public void onApply() throws IOException {
        if (lastIndex == 0)
            return;

        Path parent = pathToZipFile.getParent();
        if (lastIndex == 1)
            unzip(this.parent, pathToZipFile);
        else {
            String currentName = "IMNOTBETTERTHANWOLVES" + " (" + lastIndex + ")" + ".zip";
            Path currentPath = parent.resolve(currentName);
            unzip(this.parent, currentPath);
        }
    }

    private void updateCurrentFileLabel() {
        if (lastIndex == 0)
            currentFileLabel.setText("No saved files");
        else if (lastIndex == 1) {
            currentFileLabel.setText(pathToZipFile.getFileName().toString());
        }
        else {
            String currentName = "IMNOTBETTERTHANWOLVES" + " (" + lastIndex + ")" + ".zip";
            Path currentPath = parent.resolve(currentName);
            currentFileLabel.setText(currentPath.getFileName().toString());
        }
    }


    private void unzip(Path targetDir, Path zipFile) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                Path outputPath = targetDir.resolve(entry.getName());
                Files.createDirectories(outputPath.getParent());
                Files.copy(zis, outputPath, StandardCopyOption.REPLACE_EXISTING);
                zis.closeEntry();
            }
        }
    }

    private static void zip(Path sourceDir, Path zipFile) throws IOException {
        Path rootName = sourceDir.getFileName();

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            Files.walk(sourceDir)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(rootName.resolve(sourceDir.relativize(path)).toString());
                        try {
                            zos.putNextEntry(zipEntry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    private void updateLastIndex() throws IOException {
        Path lastIndexPath = Path.of("C:\\Dev\\01_Personal\\Java\\BTWSaver\\src\\com\\darion\\app\\lastIndex");
        Files.writeString(lastIndexPath, String.valueOf(lastIndex));
    }

}
