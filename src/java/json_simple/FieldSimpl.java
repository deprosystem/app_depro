package json_simple;

public class FieldSimpl {
    public static final int TYPE_LIST_RECORD = 0;
    public static final int TYPE_STRING = 2;
    public static final int TYPE_INTEGER = 3;
    public static final int TYPE_LONG = 4;
    public static final int TYPE_FLOAT = 5;
    public static final int TYPE_DOUBLE = 6;
    public static final int TYPE_BOOLEAN = 7;
    public static final int TYPE_DATE = 8;
    public static final int TYPE_RECORD = 9;
    public static final int TYPE_LIST_FIELD = 10;
    public static final int TYPE_SCREEN = 11;
    public static final int TYPE_FILE_PATH = 12;

    public static final int TYPE_NULL = 20;

    public String name;
    public int type;
    public Object value;

    public FieldSimpl() {
    }

    public FieldSimpl(String name, int type, Object value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public void setValue(Object value, int viewId) {
        this.value = value;
    }
}
