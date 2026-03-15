package com.icronic_Sangeetha.controller;

import com.icronic_Sangeetha.util.FileHandlerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/file") // Base path
public class FileController {

    @Autowired
    private FileHandlerUtil fileHandlerUtil;

    // Endpoint to stream songs
    @GetMapping("/song/{fileName}")
    public ResponseEntity<?> getSong(@PathVariable String fileName) {
        try {
            Resource resource = fileHandlerUtil.loadSongFile(fileName);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/octet-stream")) // Binary stream for audio
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Song not found"));
        }
    }

    // Endpoint to retrieve images
    @GetMapping("/image/{fileName}")
    public ResponseEntity<?> getImage(@PathVariable String fileName) {
        try {
            Resource resource = fileHandlerUtil.loadImageFile(fileName);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG) // Standard image type
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Image not found"));
        }
    }
}
