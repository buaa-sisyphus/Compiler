package symbol;

import token.Token;

import java.util.List;

public class VarSymbol extends Symbol {
    private List<Object> values;

    public void set(Token token, int scopeNum, int type, int btype, int con) {
        this.name = token.getContent();
        this.lineNum = token.getLineNum();
        this.scopeNum = scopeNum;
        this.type = type;
        this.btype = btype;
        this.con = con;
    }

    public List<Object> getValues() {
        return values;
    }
}
