package cn.aixcyi.plugin.tinysnake.enumeration;

public enum StringQuote {
    DOUBLE("\"", '"'),
    SINGLE("'", '\'');

    public final String string;
    public final char character;

    StringQuote(String s, char c) {
        string = s;
        character = c;
    }

    public String wrap(String content) {
        if (content == null) return null;
        return string + content + string;
    }
}
