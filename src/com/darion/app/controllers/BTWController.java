package com.darion.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BTWController {
    @FXML
    private Label chooseLabel, btwLabel, saverLabel, currentFileLabel, zipLabel;
    @FXML
    private Button chooseButton, createNewButton, overwriteButton, applyButton;

    private int lastIndex = 0;
    private Path pathToSaveFile;
    private String saveFileName;
    private Path parent;
    private Path configFile;
    private Properties appProperties = new Properties();

    @FXML
    public void initialize() throws IOException {
        createParentAndConfigFile();
        loadSettings();

        updateChooseLabel();
        updateCurrentFileLabel();
    }

    private void createParentAndConfigFile() throws IOException {
        parent = Paths.get(System.getProperty("user.home"), "Documents", "BTWSaver_Backups");
        configFile = parent.resolve("config.properties");

        if (!Files.exists(parent))
            Files.createDirectories(parent);
        if (!Files.exists(configFile))
            Files.createFile(configFile);
    }

    @FXML
    public void onChooseFile() {
        if (saveFileName != null)
            saveSettings();

        Stage stage = (Stage) chooseButton.getScene().getWindow();
        pathToSaveFile = new DirectoryChooser().showDialog(stage).toPath();
        saveFileName = pathToSaveFile.getFileName().toString();

        loadSettings();
        updateChooseLabel();
        updateCurrentFileLabel();
        saveSettings();
    }

    private void updateChooseLabel() {
        if (pathToSaveFile == null)
            chooseLabel.setText("Choose a save file");
        else
            chooseLabel.setText(pathToSaveFile.toString());
    }

    @FXML
    public void onDeleteLast() throws IOException {
        if (lastIndex == 0)
            return;

        Path pathToLastZipFile = getPathToLastFile();
        if (pathToLastZipFile == null)
            return;

        Files.delete(pathToLastZipFile);
        lastIndex--;

        saveSettings();
        updateCurrentFileLabel();
    }

    @FXML
    public void onDeleteAll() throws IOException {
        try (Stream<Path> stream = Files.walk(parent)) {
            for (Path path : stream.toList()) {
                String currentSaveFileName = path.getFileName().toString().toLowerCase();

                boolean isOriginal = currentSaveFileName.equals(saveFileName.toLowerCase() + ".zip");
                boolean isNumbered = currentSaveFileName.startsWith(saveFileName.toLowerCase() + " (") && currentSaveFileName.endsWith(").zip");

                if (isOriginal || isNumbered)
                    Files.delete(path);
                }
        }
        lastIndex = 0;

        saveSettings();
        updateCurrentFileLabel();
    }

    @FXML
    public void onCreateNew() throws IOException {
        String newZipFileName = (lastIndex == 0) ? saveFileName + ".zip" : saveFileName + " (" + (lastIndex + 1) + ")" + ".zip";
        Path pathToNewZipFile = parent.resolve(newZipFileName);
        zip(pathToSaveFile, pathToNewZipFile);
        lastIndex++;

        saveSettings();
        updateCurrentFileLabel();
    }

    @FXML
    public void onOverwrite() throws IOException {
        if (lastIndex == 0)
            return;

        zip(pathToSaveFile, getPathToLastFile());

    }

    @FXML
    public void onApply() throws IOException {
        if (lastIndex == 0)
            return;

        Path pathToLastZipFile = getPathToLastFile();
        if (pathToLastZipFile == null)
            return;

        if (!Files.exists(pathToLastZipFile)) {
            currentFileLabel.setText("Error: File Missing!");
            return;
        }

        unzip(pathToSaveFile.getParent(), pathToLastZipFile);

    }


    private void updateCurrentFileLabel() {
        if (lastIndex == 0) {
            currentFileLabel.setText("No saved files");
            return;
        }

        currentFileLabel.setText(getLastFileName());
    }

    private Path getPathToLastFile() {
        if (getLastFileName() == null)
            return null;
        return parent.resolve(getLastFileName());
    }

    private String getLastFileName() {
        if (lastIndex == 0)
            return null;

        return (lastIndex == 1) ? saveFileName + ".zip" : saveFileName + " (" + lastIndex + ")" + ".zip";
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

    private void loadSettings() {
        try (InputStream input = Files.newInputStream(configFile)) {
            appProperties.load(input);

            String loadedSavePath = (saveFileName == null) ? appProperties.getProperty("lastOpenedSaveFile") : appProperties.getProperty(saveFileName + ".targetPath");
            if (loadedSavePath != null) {
                pathToSaveFile = Path.of(loadedSavePath);
                saveFileName = pathToSaveFile.getFileName().toString();
            }

            String loadedIndex = appProperties.getProperty(saveFileName + ".lastIndex", "0");
            lastIndex = Integer.parseInt(loadedIndex);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveSettings() {
        if (saveFileName == null) return;

        try (OutputStream output = Files.newOutputStream(configFile)) {
            appProperties.setProperty("lastOpenedSaveFile", pathToSaveFile.toString());
            appProperties.setProperty(saveFileName + ".lastIndex", String.valueOf(lastIndex));
            appProperties.setProperty(saveFileName + ".targetPath", pathToSaveFile.toString());
            appProperties.store(output, "BTWSaver Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
