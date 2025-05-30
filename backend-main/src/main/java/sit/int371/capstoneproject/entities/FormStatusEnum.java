package sit.int371.capstoneproject.entities;

public enum FormStatusEnum {
    reserved("reserved"),
    checkIn("checkIn"),
    success("success");

    private String value;

    // Constructor to initialize the value
    FormStatusEnum(String value) {
        this.value = value;
    }

    // Getter to retrieve the value
    public String getValue() {
        return value;
    }
}
