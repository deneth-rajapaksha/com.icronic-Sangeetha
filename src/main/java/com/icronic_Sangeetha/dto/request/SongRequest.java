package com.icronic_Sangeetha.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SongRequest {
    @NotBlank(message = "Tital is required")
    @Size (max = 100 , message = "title must not exceed 100 characters")
    private String title;

    @NotBlank(message = "Artist required")
    @Size(max = 100 , message = "Artist must not exceed 100 characters")
    private  String artist;

}
