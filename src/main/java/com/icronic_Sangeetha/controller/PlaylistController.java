package com.icronic_Sangeetha.controller;

import com.icronic_Sangeetha.dto.request.PlaylistRequest;
import com.icronic_Sangeetha.dto.response.MessegeResponse;
import com.icronic_Sangeetha.dto.response.PaginatedResponse;
import com.icronic_Sangeetha.dto.response.PlaylistResponse;
import com.icronic_Sangeetha.dto.response.PlaylistWithSongsResponse;
import com.icronic_Sangeetha.service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/play")
@Validated
public class PlaylistController {

    @Autowired
    private PlaylistService playlistService;

    @PostMapping("/create-playlist")
    public ResponseEntity<PlaylistResponse> createPlaylist(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "false") boolean isPublic,
            @RequestParam MultipartFile imageFile,
            Authentication authentication) {

        PlaylistRequest request = new PlaylistRequest(name, description, isPublic);
        PlaylistResponse response = playlistService.createPlaylist(request, imageFile, authentication.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/update-playlist-privacy/{id}")
    public ResponseEntity<PlaylistResponse> updatePlaylistPrivacy(
            @PathVariable Long id,
            @RequestParam boolean isPublic,
            Authentication authentication) {
        return ResponseEntity.ok(playlistService.updatePlaylistPrivacy(id, isPublic, authentication.getName()));
    }

    @PostMapping("/add-song-to-playlist/{playlistId}")
    public ResponseEntity<MessegeResponse> addSongToPlaylist(
            @PathVariable Long playlistId,
            @RequestParam Long songId,
            Authentication authentication) {
        return ResponseEntity.ok(playlistService.addSongToPlaylist(playlistId, songId, authentication.getName()));
    }

    @DeleteMapping("/remove-song-from-playlist/{playlistId}")
    public ResponseEntity<MessegeResponse> removeSongFromPlaylist(
            @PathVariable Long playlistId,
            @RequestParam Long songId,
            Authentication authentication) {
        return ResponseEntity.ok(playlistService.removeSongFromPlaylist(playlistId, songId, authentication.getName()));
    }

    @PatchMapping("/reorder-song-in-playlist/{playlistId}")
    public ResponseEntity<MessegeResponse> reorderSongInPlaylist(
            @PathVariable Long playlistId,
            @RequestParam Long songId,
            @RequestParam int newPosition,
            Authentication authentication) {
        return ResponseEntity
                .ok(playlistService.reorderSongInPlaylist(playlistId, songId, newPosition, authentication.getName()));
    }

    @GetMapping("/get-all-public-playlists")
    public ResponseEntity<PaginatedResponse<PlaylistResponse>> getAllPublicPlaylists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(playlistService.getAllPublicPlaylists(page, size, search));
    }

    @GetMapping("/get-playlist-with-song/{playlistId}")
    public ResponseEntity<PlaylistWithSongsResponse> getPlaylistWithSongs(
            @PathVariable Long playlistId,
            Authentication authentication) {
        String email = (authentication != null) ? authentication.getName() : null;
        return ResponseEntity.ok(playlistService.getPlaylistWithSongs(playlistId, email));
    }

    @DeleteMapping("/delete-playlist/{playlistId}")
    public ResponseEntity<MessegeResponse> deletePlaylist(@PathVariable Long playlistId,
            Authentication authentication) {
        return ResponseEntity.ok(playlistService.deletePlaylist(playlistId, authentication.getName()));
    }
}
