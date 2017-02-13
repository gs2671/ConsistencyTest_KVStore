package consistency;
import org.apache.thrift.TEnum;

public enum ErrorCode implements TEnum {
    kSuccess(0),
    kKeyNotFound(1),
    kError(2);

    private final int value;

    private ErrorCode(int value) {
        this.value = value;
    }

    /**
     * Get the integer value of this enum value, as defined in the Thrift IDL.
     */
    public int getValue() {
        return value;
    }

    /**
     * Find a the enum type by its integer value, as defined in the Thrift IDL.
     * @return null if the value is not found.
     */
    public static ErrorCode findByValue(int value) {
        switch (value) {
            case 0:
                return kSuccess;
            case 1:
                return kKeyNotFound;
            case 2:
                return kError;
            default:
                return null;
        }
    }
}
