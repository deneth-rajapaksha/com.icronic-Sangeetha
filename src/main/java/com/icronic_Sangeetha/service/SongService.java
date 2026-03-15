package com.icronic_Sangeetha.service;

import com.icronic_Sangeetha.dto.request.SongRequest;
import com.icronic_Sangeetha.dto.response.SongAIInsightsResponse;
import com.icronic_Sangeetha.dto.response.SongResponse;
import org.springframework.web.multipart.MultipartFile;

public interface SongService {
    SongResponse addSong(SongRequest request, MultipartFile songFile, MultipartFile imageFile, String email);

    Object getAllSongs(Long userId, int page, int size, String search);

    SongResponse updateSong(Long id, SongRequest request, MultipartFile songFile, MultipartFile imageFile,
            String email);

    Object deleteSong(Long id, String email);

    SongAIInsightsResponse getSongAIInsights(Long songId);
}
