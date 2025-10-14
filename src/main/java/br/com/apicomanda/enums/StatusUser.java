package br.com.apicomanda.enums;

public enum StatusUser {
    ENABLED(true),
    DISABLED(false);

    private final boolean statusValue;

    StatusUser(boolean statusValue) {
        this.statusValue = statusValue;
    }

    public boolean getStatusValue() {
        return statusValue;
    }
}
