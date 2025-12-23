package br.com.apicomanda.helpers;

public class ApplicationConstants {
    // Versão e Segurança
    public static final String
            VERSION = "/v1";
    public static final String $ROLE_ADMIN = "ROLE_ADMIN";
    public static final String $ROLE_USER = "ROLE_USER";
    public static final String $ISSUER = "COMANDA_ONLINE";

    // Expressões Regulares
    public static final String REGEX_PASSWORD = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#.])([A-Za-z\\d@$!%*?&#.]{8,})$";
    public static final String REGEX_TELEPHONE = "^\\d{10,11}$";

    // Mensagens de Validação
    public static final String MSG_NAME_REQUIRED = "Nome é obrigatório.";
    public static final String MSG_EMAIL_REQUIRED = "E-mail é obrigatório.";
    public static final String MSG_TELEPHONE_REQUIRED = "Telefone é obrigatório.";
    public static final String MSG_PASSWORD_REQUIRED = "Password é obrigatório";
    public static final String MSG_PROFILE_REQUIRED = "Profile é obrigatório";
    public static final String MSG_TELEPHONE_INVALID_FORMAT = "O número de telefone deve estar no formato 11999999999";
    public static final String MSG_PASSWORD_INVALID_FORMAT = "A senha deve ter pelo menos 8 caracteres, 1 letra maiúscula, 1 número e 1 caractere especial";

    // Nomes dos Perfis (sem o prefixo ROLE_)
    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";

    // Constantes de Autorização
    public static final String IS_ADMIN_OR_USER = "hasAnyRole('" + ADMIN + "', '" + USER + "')";
    public static final String IS_ADMIN = "hasRole('" + ADMIN + "')";
}
