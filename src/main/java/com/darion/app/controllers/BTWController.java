package com.darion.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.darion.app.config.ConfigManager;
import com.darion.app.services.BackupService;

public class BTWController {
    @FXML
    private Label chooseLabel, btwLabel, saverLabel, currentFileLabel, zipLabel;
    @FXML
    private Button chooseButton, createNewButton, overwriteButton, applyButton;
    @FXML
    private CheckMenuItem warnBeforeActionItem;
    @FXML
    private Label aysDeleteLastLabel, aysDeleteAllLabel, aysCreateNewLabel, aysOverwriteLabel, aysApplyLabel;
    @FXML
    private Button yesDeleteLastButton, noDeleteLastButton, yesDeleteAllButton, noDeleteAllButton, yesCreateNewButton,
            noCreateNewButton, yesOverwriteButton, noOverwriteButton, yesApplyButton, noApplyButton;

    private int lastIndex = 0;
    private Path pathToSaveFile;
    private String saveFileName;
    private BackupService backupService = new BackupService();
    private ConfigManager configManager;

    @FXML
    public void initialize() throws IOException {
        configManager = new ConfigManager();
        loadSettings();

        updateChooseLabel();
        updateCurrentFileLabel();
    }

    @FXML
    public void onChooseFile() throws IOException {
        if (saveFileName != null)
            saveSettings();

        Stage stage = (Stage) chooseButton.getScene().getWindow();
        File selectedDir = new DirectoryChooser().showDialog(stage);
        if (selectedDir == null)
            return;

        pathToSaveFile = selectedDir.toPath();
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
    public void onYesDeleteLast() {

    }

    @FXML
    public void onNoDeleteLast() {

    }

    @FXML
    public void onDeleteAll() throws IOException {
        backupService.deleteAll(configManager.getParent(), saveFileName);
        lastIndex = 0;

        saveSettings();
        updateCurrentFileLabel();
    }

    @FXML
    public void onYesDeleteAll() {

    }

    @FXML
    public void onNoDeleteAll() {

    }

    @FXML
    public void onCreateNew() throws IOException {
        String newZipFileName = (lastIndex == 0) ? saveFileName + ".zip"
                : saveFileName + " (" + (lastIndex + 1) + ")" + ".zip";
        Path pathToNewZipFile = configManager.getParent().resolve(newZipFileName);
        backupService.zip(pathToSaveFile, pathToNewZipFile);
        lastIndex++;

        saveSettings();
        updateCurrentFileLabel();
    }

    @FXML
    public void onYesCreateNew() {

    }

    @FXML
    public void onNoCreateNew() {

    }

    @FXML
    public void onOverwrite() throws IOException {
        if (lastIndex == 0)
            return;

        backupService.zip(pathToSaveFile, getPathToLastFile());

    }

    @FXML
    public void onYesOverwrite() {

    }

    @FXML
    public void onNoOverwrite() {

    }

    @FXML
    public void onApply() throws IOException {
        if (lastIndex == 0)
            return;

        if (!warnBeforeActionItem.isSelected()) {
            doTheApply();
            return;
        }

        setConfirmationMenu(aysApplyLabel, yesApplyButton, noApplyButton, true);

    }

    private void doTheApply() throws IOException {
        Path pathToLastZipFile = getPathToLastFile();

        if (!Files.exists(pathToLastZipFile)) {
            currentFileLabel.setText("Error: File Missing!");
            return;
        }

        backupService.unzip(pathToSaveFile.getParent(), pathToLastZipFile);

        setConfirmationMenu(aysApplyLabel, yesApplyButton, noApplyButton, false);
    }

    @FXML
    public void onYesApply() throws IOException {
        doTheApply();
    }

    @FXML
    public void onNoApply() {
        setConfirmationMenu(aysApplyLabel, yesApplyButton, noApplyButton, false);
    }

    private void setConfirmationMenu(Label label, Button yesButton, Button noButton, boolean areVisible) {
        label.setVisible(areVisible);
        yesButton.setVisible(areVisible);
        noButton.setVisible(areVisible);
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
        return configManager.getParent().resolve(getLastFileName());
    }

    private String getLastFileName() {
        if (lastIndex == 0)
            return null;

        return (lastIndex == 1) ? saveFileName + ".zip" : saveFileName + " (" + lastIndex + ")" + ".zip";
    }

    private void loadSettings() {

        String loadedSavePath = (saveFileName == null) ? configManager.getProperty("lastOpenedSaveFile")
                : configManager.getProperty(saveFileName + ".targetPath"); // Will be null if there is no save file
        if (loadedSavePath != null) {
            pathToSaveFile = Path.of(loadedSavePath);
            saveFileName = pathToSaveFile.getFileName().toString();
        }

        String loadedIndex = configManager.getProperty(saveFileName + ".lastIndex", "0");
        lastIndex = Integer.parseInt(loadedIndex);

    }

    private void saveSettings() throws IOException {
        if (saveFileName == null)
            return;

        configManager.setProperty("lastOpenedSaveFile", pathToSaveFile.toString());
        configManager.setProperty(saveFileName + ".lastIndex", String.valueOf(lastIndex));
        configManager.setProperty(saveFileName + ".targetPath", pathToSaveFile.toString());

        configManager.save();

    }

}
