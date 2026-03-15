package com.icronic_Sangeetha.serviceImpl;

import com.icronic_Sangeetha.dto.request.PlaylistRequest;
import com.icronic_Sangeetha.dto.response.MessegeResponse;
import com.icronic_Sangeetha.dto.response.PaginatedResponse;
import com.icronic_Sangeetha.dto.response.PlaylistResponse;
import com.icronic_Sangeetha.dto.response.PlaylistWithSongsResponse;
import com.icronic_Sangeetha.entity.AppUser;
import com.icronic_Sangeetha.entity.Playlist;
import com.icronic_Sangeetha.entity.PlaylistSong;
import com.icronic_Sangeetha.entity.Song;
import com.icronic_Sangeetha.repository.AppUserRepository;
import com.icronic_Sangeetha.repository.PlaylistRepository;
import com.icronic_Sangeetha.repository.PlaylistSongRepository;
import com.icronic_Sangeetha.repository.SongRepository;
import com.icronic_Sangeetha.service.PlaylistService;
import com.icronic_Sangeetha.util.FileHandlerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PlaylistServiceImpl implements PlaylistService {

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private PlaylistSongRepository playlistSongRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private FileHandlerUtil fileHandlerUtil;

    @Value("${app.base.url}")
    private String baseUrl;

    @Override
    public PlaylistResponse createPlaylist(PlaylistRequest request, MultipartFile imageFile, String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Playlist playlist = new Playlist();
        playlist.setName(request.getName());
        playlist.setDescription(request.getDescription());
        playlist.setIsPublic(request.getIsPublic());
        playlist.setAppUser(user);

        if (imageFile != null && !imageFile.isEmpty()) {
            String uniqueId = UUID.randomUUID().toString();
            String fileName = uniqueId + fileHandlerUtil.getFileExtension(imageFile.getOriginalFilename());
            fileHandlerUtil.saveImageFileWithName(imageFile, fileName);
            playlist.setImageUrl("/api/file/image/" + fileName);
        }

        Playlist saved = playlistRepository.save(playlist);
        return PlaylistResponse.fromEntity(saved, baseUrl);
    }

    @Override
    public PlaylistResponse updatePlaylistPrivacy(Long id, boolean isPublic, String email) {
        Playlist playlist = validatePlaylistAccess(id, email);
        playlist.setIsPublic(isPublic);
        Playlist updated = playlistRepository.save(playlist);
        return PlaylistResponse.fromEntity(updated, baseUrl);
    }

    @Override
    @Transactional
    public MessegeResponse addSongToPlaylist(Long playlistId, Long songId, String email) {
        Playlist playlist = validatePlaylistAccess(playlistId, email);
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        if (playlistSongRepository.existsByPlaylistIdAndSongId(playlistId, songId)) {
            throw new RuntimeException("Song already exists in playlist");
        }

        List<PlaylistSong> existing = playlistSongRepository.findByPlaylistIdOrderByPositionAsc(playlistId);
        int nextPos = existing.isEmpty() ? 1 : existing.get(existing.size() - 1).getPosition() + 1;

        PlaylistSong ps = new PlaylistSong();
        ps.setPlaylist(playlist);
        ps.setSong(song);
        ps.setPosition(nextPos);
        playlistSongRepository.save(ps);

        return new MessegeResponse("Song added to playlist successfully");
    }

    @Override
    @Transactional
    public MessegeResponse removeSongFromPlaylist(Long playlistId, Long songId, String email) {
        validatePlaylistAccess(playlistId, email);
        PlaylistSong target = playlistSongRepository.findByPlaylistIdAndSongId(playlistId, songId)
                .orElseThrow(() -> new RuntimeException("Song not in playlist"));

        int removedPosition = target.getPosition();
        playlistSongRepository.delete(target);

        // Shift down positions of songs after the removed one
        List<PlaylistSong> remaining = playlistSongRepository.findByPlaylistIdOrderByPositionAsc(playlistId);
        for (PlaylistSong ps : remaining) {
            if (ps.getPosition() > removedPosition) {
                ps.setPosition(ps.getPosition() - 1);
                playlistSongRepository.save(ps);
            }
        }

        return new MessegeResponse("Song removed from playlist successfully");
    }

    @Override
    @Transactional
    public MessegeResponse reorderSongInPlaylist(Long playlistId, Long songId, int newPosition, String email) {
        validatePlaylistAccess(playlistId, email);
        PlaylistSong target = playlistSongRepository.findByPlaylistIdAndSongId(playlistId, songId)
                .orElseThrow(() -> new RuntimeException("Song not in playlist"));

        int currentPos = target.getPosition();
        List<PlaylistSong> allSongs = playlistSongRepository.findByPlaylistIdOrderByPositionAsc(playlistId);

        // Logic for shifting positions up or down
        for (PlaylistSong ps : allSongs) {
            if (newPosition > currentPos) {
                if (ps.getPosition() > currentPos && ps.getPosition() <= newPosition) {
                    ps.setPosition(ps.getPosition() - 1);
                    playlistSongRepository.save(ps);
                }
            } else {
                if (ps.getPosition() >= newPosition && ps.getPosition() < currentPos) {
                    ps.setPosition(ps.getPosition() + 1);
                    playlistSongRepository.save(ps);
                }
            }
        }

        target.setPosition(newPosition);
        playlistSongRepository.save(target);
        return new MessegeResponse("Song reordered successfully");
    }

    @Override
    public PaginatedResponse<PlaylistResponse> getAllPublicPlaylists(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Playlist> playlistPage;

        if (search != null && !search.trim().isEmpty()) {
            playlistPage = playlistRepository.findByIsPublicTrueAndNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            playlistPage = playlistRepository.findByIsPublicTrue(pageable);
        }

        List<PlaylistResponse> responses = playlistPage.getContent().stream()
                .map(playlist -> PlaylistResponse.fromEntity(playlist, baseUrl))
                .collect(Collectors.toList());

        return new PaginatedResponse<>(responses, playlistPage.getNumber(), playlistPage.getSize(),
                playlistPage.getTotalElements(), playlistPage.getTotalPages(), playlistPage.isLast(),
                playlistPage.isFirst());
    }

    @Override
    public PlaylistWithSongsResponse getPlaylistWithSongs(Long playlistId, String email) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        // If playlist is private, only owner or admin can view
        if (!playlist.getIsPublic()) {
            if (email == null) {
                throw new RuntimeException("Authentication required to view private playlist");
            }
            AppUser user = appUserRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            boolean isOwner = playlist.getAppUser().getId().equals(user.getId());
            boolean isAdmin = "ADMIN".equals(user.getRole());
            if (!isOwner && !isAdmin) {
                throw new RuntimeException("No permission to view this playlist");
            }
        }

        List<PlaylistSong> playlistSongs = playlistSongRepository.findByPlaylistIdOrderByPositionAsc(playlistId);
        return PlaylistWithSongsResponse.fromEntity(playlist, playlistSongs, baseUrl);
    }

    @Override
    @Transactional
    public MessegeResponse deletePlaylist(Long playlistId, String email) {
        validatePlaylistAccess(playlistId, email);

        // Delete all playlist songs first
        playlistSongRepository.deleteByPlaylistId(playlistId);

        // Delete the playlist itself
        playlistRepository.deleteById(playlistId);

        return new MessegeResponse("Playlist deleted successfully");
    }

    private Playlist validatePlaylistAccess(Long id, String email) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isOwner = playlist.getAppUser().getId().equals(user.getId());
        boolean isAdmin = "ADMIN".equals(user.getRole());

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("No permission to modify this playlist");
        }
        return playlist;
    }
}
