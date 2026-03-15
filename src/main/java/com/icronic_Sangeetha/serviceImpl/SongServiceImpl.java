package com.icronic_Sangeetha.serviceImpl;

import com.icronic_Sangeetha.dto.request.SongRequest;
import com.icronic_Sangeetha.dto.response.PaginatedResponse;
import com.icronic_Sangeetha.dto.response.SongAIInsightsResponse;
import com.icronic_Sangeetha.dto.response.SongResponse;
import com.icronic_Sangeetha.entity.AppUser;
import com.icronic_Sangeetha.entity.Song;
import com.icronic_Sangeetha.repository.AppUserRepository;
import com.icronic_Sangeetha.repository.SongRepository;
import com.icronic_Sangeetha.service.GenericGeminiService;
import com.icronic_Sangeetha.service.SongService;
import com.icronic_Sangeetha.util.FileHandlerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SongServiceImpl implements SongService {

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private FileHandlerUtil fileHandlerUtil;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private GenericGeminiService genericGeminiService;

    @Value("${app.base.url}")
    private String appBaseUrl;

    @Override
    public SongResponse addSong(SongRequest request, MultipartFile songFile, MultipartFile imageFile, String email) {
        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String uniqueId = UUID.randomUUID().toString(); // Ensure unique filenames

        // Logic to process and save files using Utility
        String songUrl = processSongFile(songFile, uniqueId);
        String imageUrl = processImageFile(imageFile, uniqueId);

        Song song = new Song();
        song.setTitle(request.getTitle());
        song.setArtist(request.getArtist());
        song.setSongUrl(songUrl);
        song.setImageUrl(imageUrl);
        song.setAppUser(appUser);

        Song savedSong = songRepository.save(song);
        return SongResponse.fromEntity(savedSong, appBaseUrl); // Conversion to DTO
    }

    private String processSongFile(MultipartFile file, String uniqueId) {
        String extension = fileHandlerUtil.getFileExtension(file.getOriginalFilename());
        String fileName = uniqueId + extension;
        fileHandlerUtil.saveSongFileWithName(file, fileName);
        return "/api/file/song/" + fileName; // Path concatenation
    }

    private String processImageFile(MultipartFile file, String uniqueId) {
        String extension = fileHandlerUtil.getFileExtension(file.getOriginalFilename());
        String fileName = uniqueId + extension;
        fileHandlerUtil.saveImageFileWithName(file, fileName);
        return "/api/file/image/" + fileName;
    }

    @Override
    public Object getAllSongs(Long userId, int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Song> songPage;

        if (search != null && !search.trim().isEmpty()) {
            songPage = songRepository.findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(search.trim(), search.trim(), pageable);
        } else {
            songPage = songRepository.findAll(pageable);
        }

        List<SongResponse> responses = songPage.getContent().stream()
                .map(song -> SongResponse.fromEntity(song, appBaseUrl))
                .collect(Collectors.toList());

        return new PaginatedResponse<>(responses, songPage.getNumber(), songPage.getSize(),
                songPage.getTotalElements(), songPage.getTotalPages(), songPage.isLast(), songPage.isFirst());
    }

    @Override
    public SongResponse updateSong(Long id, SongRequest request, MultipartFile songFile, MultipartFile imageFile,
            String email) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        song.setTitle(request.getTitle());
        song.setArtist(request.getArtist());

        if (songFile != null && !songFile.isEmpty()) {
            // Delete old song file
            if (song.getSongUrl() != null) {
                fileHandlerUtil.deleteSongFile(extractFileName(song.getSongUrl()));
            }
            String uniqueId = UUID.randomUUID().toString();
            String songUrl = processSongFile(songFile, uniqueId);
            song.setSongUrl(songUrl);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            // Delete old image file
            if (song.getImageUrl() != null) {
                fileHandlerUtil.deleteImageFile(extractFileName(song.getImageUrl()));
            }
            String uniqueId = UUID.randomUUID().toString();
            String imageUrl = processImageFile(imageFile, uniqueId);
            song.setImageUrl(imageUrl);
        }

        Song updatedSong = songRepository.save(song);
        return SongResponse.fromEntity(updatedSong, appBaseUrl);
    }

    @Override
    public Object deleteSong(Long id, String email) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        // Physical file deletion logic
        if (song.getSongUrl() != null) {
            fileHandlerUtil.deleteSongFile(extractFileName(song.getSongUrl()));
        }
        if (song.getImageUrl() != null) {
            fileHandlerUtil.deleteImageFile(extractFileName(song.getImageUrl()));
        }

        songRepository.delete(song);
        return "Song deleted successfully";
    }

    @Override
    public SongAIInsightsResponse getSongAIInsights(Long songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        String prompt = buildSongAnalysisPrompt(song);
        return genericGeminiService.generateContent(prompt, SongAIInsightsResponse.class);
    }

    private String buildSongAnalysisPrompt(Song song) {
        return String.format(
                "Analyze the song '%s' by artist '%s' and provide detailed insights in JSON format. " +
                        "The JSON should have these exact fields and types: " +
                        "\"description\" (a brief text analysis 1-3 sentences), " +
                        "\"mood\" (a single mood string like \"Energetic\" or \"Melancholic\"), " +
                        "\"genre\" (the primary genre as a string), " +
                        "\"tempo\" (the estimated tempo as a string like \"120 BPM\"), " +
                        "\"similarArtists\" (array of strings, up to 3 similar artist names), " +
                        "\"funFact\" (a brief, interesting real-world fact about the song or artist). " +
                        "Return ONLY valid JSON, no additional text, and do not wrap in markdown code blocks.",
                song.getTitle(), song.getArtist());
    }

    private String extractFileName(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }
}
