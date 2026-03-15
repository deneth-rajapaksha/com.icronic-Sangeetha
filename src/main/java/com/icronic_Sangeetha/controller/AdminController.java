package com.icronic_Sangeetha.controller;

import com.icronic_Sangeetha.dto.request.SongRequest;
import com.icronic_Sangeetha.dto.response.SongResponse;
import com.icronic_Sangeetha.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin") // Base path
@Validated
public class AdminController {

    @Autowired
    private SongService songService;

    @PostMapping("/add-song")
    @PreAuthorize("hasRole('ADMIN')") // RBAC Security
    public ResponseEntity<SongResponse> addSong(
            @RequestParam String title,
            @RequestParam String artist,
            @RequestParam MultipartFile songFile,
            @RequestParam MultipartFile imageFile,
            Authentication authentication) {

        SongRequest request = new SongRequest();
        request.setTitle(title);
        request.setArtist(artist);

        SongResponse response = songService.addSong(request, songFile, imageFile, authentication.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/get-all-songs")
    public ResponseEntity<?> getAllSongs(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(songService.getAllSongs(userId, page, size, search));
    }

    @PutMapping("/update-song/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SongResponse> updateSong(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String artist,
            @RequestParam(required = false) MultipartFile songFile,
            @RequestParam(required = false) MultipartFile imageFile,
            Authentication authentication) {

        SongRequest request = new SongRequest();
        request.setTitle(title);
        request.setArtist(artist);

        SongResponse response = songService.updateSong(id, request, songFile, imageFile, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-song/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSong(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(songService.deleteSong(id, authentication.getName()));
    }
}
