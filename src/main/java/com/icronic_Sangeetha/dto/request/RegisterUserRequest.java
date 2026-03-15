package com.icronic_Sangeetha.dto.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserRequest {
    @Size(min = 1, max = 50, message = "Name must be between 1 and 50 characters")
    private String name;

    @Email(message = "Email should be valid")
    private String email;

    @Pattern(regexp = "^(USER|ADMIN)$", message = "Role must be either USER or ADMIN")
    private String role;
}
