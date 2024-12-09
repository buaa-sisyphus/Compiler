package node;

import token.Token;
import utils.IOUtils;

import java.util.HashMap;
import java.util.Map;

// Character → CharConst
public class CharacterNode extends Node {
    private Token charConst;

    public CharacterNode(Token charConst) {
        this.charConst = charConst;
        this.type = NodeType.Character;
    }

    @Override
    public void print() {
        IOUtils.writeSymbol(charConst.toString());
        IOUtils.writeSymbol(typeToString());
    }

    public static final Map<String, Character> ESCAPE_CHAR_MAP = new HashMap<>() {{
        put("\\a", '\u0007');
        put("\\b", '\b');
        put("\\t", '\t');
        put("\\n", '\n');
        put("\\v", '\u000B');
        put("\\f", '\f');
        put("\\r", '\r');
        put("\\\"", '\"');
        put("\\'", '\'');
        put("\\\\", '\\');
        put("\\0", '\0');
    }};

    public char getChar() {
        String content = charConst.getContent();
        content = content.substring(1, content.length() - 1);
        return ESCAPE_CHAR_MAP.getOrDefault(content, content.charAt(0));
    }

}
