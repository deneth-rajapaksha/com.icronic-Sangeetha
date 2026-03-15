package com.icronic_Sangeetha.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;

@Component
public class FileHandlerUtil {

    @Value("${file.storage.song.path}") // Defined in application.properties
    private String songStoragePath;

    @Value("${file.storage.image.path}")
    private String imageStoragePath;

    // Logic to save song files
    public String saveSongFileWithName(MultipartFile file, String customName) {
        return saveFile(file, songStoragePath, customName);
    }

    // Logic to save image files
    public String saveImageFileWithName(MultipartFile file, String customName) {
        return saveFile(file, imageStoragePath, customName);
    }

    private String saveFile(MultipartFile file, String storagePath, String customName) {
        try {
            if (file.isEmpty())
                throw new RuntimeException("Failed to store empty file");
            Path directoryPath = Paths.get(storagePath);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }
            Path destinationPath = directoryPath.resolve(customName);
            Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
            return customName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    // Logic to load files as a Resource
    public Resource loadSongFile(String fileName) {
        return loadFile(fileName, songStoragePath);
    }

    public Resource loadImageFile(String fileName) {
        return loadFile(fileName, imageStoragePath);
    }

    private Resource loadFile(String fileName, String storagePath) {
        try {
            Path filePath = Paths.get(storagePath).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error loading file", e);
        }
    }

    // Logic to delete files
    public void deleteSongFile(String fileName) {
        deleteFile(fileName, songStoragePath);
    }

    public void deleteImageFile(String fileName) {
        deleteFile(fileName, imageStoragePath);
    }

    private void deleteFile(String fileName, String storagePath) {
        try {
            Path filePath = Paths.get(storagePath).resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + fileName, e);
        }
    }

    // Extraction methods
    public String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }
}
