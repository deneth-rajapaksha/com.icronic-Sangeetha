package com.icronic_Sangeetha.controller;

import com.icronic_Sangeetha.dto.response.SongAIInsightsResponse;
import com.icronic_Sangeetha.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/song")
public class SongController {

    @Autowired
    private SongService songService;

    @GetMapping("/get-song-ai-insights/{songId}")
    public ResponseEntity<SongAIInsightsResponse> getSongAIInsights(@PathVariable Long songId) {
        SongAIInsightsResponse response = songService.getSongAIInsights(songId);
        return ResponseEntity.ok(response);
    }
}
