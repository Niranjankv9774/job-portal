package com.example.jobportal.service;

import com.example.jobportal.exception.BusinessException;
import com.example.jobportal.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/** Saves and loads resume PDFs from the uploads folder. */
@Service
public class FileStorageService {

    private final Path root;      // the uploads folder

    public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) throws IOException {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(root);          // creates the folder on first start
    }

    /** Checks the file is a real PDF, saves it under a random name, returns that name. */
    public String storeResume(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Please choose a file");
        }

        // Check 1: the name must end in .pdf
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!original.endsWith(".pdf")) {
            throw new BusinessException("Only PDF files are allowed");
        }

        // Check 2: every real PDF starts with the 4 characters %PDF
        try (InputStream in = file.getInputStream()) {
            String header = new String(in.readNBytes(4), StandardCharsets.US_ASCII);
            if (!"%PDF".equals(header)) {
                throw new BusinessException("This file is not a valid PDF");
            }
        } catch (IOException e) {
            throw new BusinessException("Could not read the file");
        }

        // Save under a random name, never the name the user chose
        String storedName = UUID.randomUUID() + ".pdf";
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, root.resolve(storedName));
        } catch (IOException e) {
            throw new BusinessException("Could not save the file");
        }
        return storedName;
    }

    /** Opens a saved file for download. */
    public Resource load(String storedName) {
        Path path = root.resolve(storedName).normalize();
        if (!path.startsWith(root)) {                       // stops tricks like ../../secret.txt
            throw new BusinessException("Invalid file path");
        }
        try {
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists()) {
                throw new ResourceNotFoundException("Resume file not found");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Resume file not found");
        }
    }

    /** Deletes an old resume (used when a seeker uploads a new one). */
    public void delete(String storedName) {
        try {
            Path path = root.resolve(storedName).normalize();
            if (path.startsWith(root)) {
                Files.deleteIfExists(path);
            }
        } catch (IOException ignored) {
            // an old file that cannot be deleted is not worth failing the request for
        }
    }
}