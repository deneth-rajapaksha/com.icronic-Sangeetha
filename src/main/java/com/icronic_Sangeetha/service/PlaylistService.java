package com.icronic_Sangeetha.service;

import com.icronic_Sangeetha.dto.request.PlaylistRequest;
import com.icronic_Sangeetha.dto.response.MessegeResponse;
import com.icronic_Sangeetha.dto.response.PaginatedResponse;
import com.icronic_Sangeetha.dto.response.PlaylistResponse;
import com.icronic_Sangeetha.dto.response.PlaylistWithSongsResponse;
import org.springframework.web.multipart.MultipartFile;

public interface PlaylistService {
    PlaylistResponse createPlaylist(PlaylistRequest request, MultipartFile imageFile, String email);

    PlaylistResponse updatePlaylistPrivacy(Long id, boolean isPublic, String email);

    MessegeResponse addSongToPlaylist(Long playlistId, Long songId, String email);

    MessegeResponse removeSongFromPlaylist(Long playlistId, Long songId, String email);

    MessegeResponse reorderSongInPlaylist(Long playlistId, Long songId, int newPos, String email);

    PaginatedResponse<PlaylistResponse> getAllPublicPlaylists(int page, int size, String search);

    PlaylistWithSongsResponse getPlaylistWithSongs(Long playlistId, String email);

    MessegeResponse deletePlaylist(Long playlistId, String email);
}
