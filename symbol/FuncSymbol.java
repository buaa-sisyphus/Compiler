package symbol;

import token.Token;
import token.TokenType;

import java.util.ArrayList;
import java.util.List;

public class FuncSymbol extends Symbol {
    private List<FuncParam> params = new ArrayList<>();

    public FuncSymbol() {
    }

    public void set(Token token, int scopeNum, int type, int btype, int con) {
        this.name = token.getContent();
        this.lineNum = token.getLineNum();
        this.scopeNum = scopeNum;
        this.type = type;
        this.btype = btype;
        this.con = con;
    }

    public int getParamsCount() {
        return params.size();
    }

    public List<FuncParam> getParams() {
        return params;
    }

    public void addParam(FuncParam param) {
        params.add(param);
    }
}
