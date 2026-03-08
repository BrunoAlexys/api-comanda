package br.com.apicomanda.enums;

import java.util.Arrays;

public enum ErrorUserDisableMessages {
    USER_DISABLED("Usuário desabilitado"),
    USER_IS_DISABLED("User is disabled");

    private final String message;

    ErrorUserDisableMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public static boolean contains(String exceptionMessage) {
        return Arrays.stream(values())
                .anyMatch(error -> exceptionMessage.toLowerCase().contains(error.getMessage().toLowerCase()));
    }
}
