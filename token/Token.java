package token;

public class Token {
    private TokenType type;
    private int lineNum;
    private String content;
    public Token(TokenType type, int lineNum, String content) {{
        this.type = type;
        this.lineNum = lineNum;
        this.content = content;}
    }

    public TokenType getType() {
        return type;
    }

    public int getLineNum() {
        return lineNum;
    }

    public String getContent() {
        return content;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return this.type.toString()+" "+this.content+"\n";
    }
}
