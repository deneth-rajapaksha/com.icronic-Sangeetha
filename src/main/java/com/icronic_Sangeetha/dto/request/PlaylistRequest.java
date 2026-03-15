package com.icronic_Sangeetha.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class PlaylistRequest {
    @NotBlank(message = "name is required")
    @Size (min = 1,max = 100,message = "name must be between 1 -100 characters")
    private String name;

    @NotBlank(message = "description is required")
    @Size (min = 1,max = 500,message = "description must be between 1 - 500 characters")
    private String description;

    private Boolean isPublic;
}
