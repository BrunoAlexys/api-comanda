package br.com.apicomanda.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import static br.com.apicomanda.helpers.ApplicationConstants.*;

public record CreateUserRequest(
        @NotBlank(message = MSG_NAME_REQUIRED)
        String name,
        @NotBlank(message = MSG_EMAIL_REQUIRED)
        @Email
        String email,
        @NotBlank(message = MSG_TELEPHONE_REQUIRED)
        @Pattern(regexp = REGEX_TELEPHONE, message = MSG_TELEPHONE_INVALID_FORMAT)
        String telephone,
        @NotBlank(message = MSG_PASSWORD_REQUIRED)
        @Pattern(regexp = REGEX_PASSWORD, message = MSG_PASSWORD_INVALID_FORMAT)
        String password,
        @NotNull(message = MSG_PROFILE_REQUIRED)
        Long idProfile
) {
}
