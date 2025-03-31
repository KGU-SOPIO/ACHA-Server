package sopio.acha.domain.activity.domain;

public enum SubmitType {
    DONE,
    LATE,
    MISS,
    NONE;

    public static SubmitType fronString(String value) {
        if (value == null || value.isBlank()) return SubmitType.NONE;
        return SubmitType.valueOf(value.toUpperCase());
    }
}
