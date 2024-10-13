package symbol;

import token.Token;
import token.TokenType;

import java.util.ArrayList;
import java.util.List;

public class FuncSymbol extends Symbol {
    private List<FuncParam> params = new ArrayList<>();

    public FuncSymbol() {
    }

    public void set(Token token, int scopeNum, TokenType type) {
        this.name = token.getContent();
        this.lineNum = token.getLineNum();
        this.scopeNum = scopeNum;
        if (type == TokenType.VOIDTK) {
            this.symbolType = SymbolType.VoidFunc;
        } else if (type == TokenType.INTTK) {
            this.symbolType = SymbolType.IntFunc;
        } else {
            this.symbolType = SymbolType.CharFunc;
        }
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

    public SymbolType getSymbolType() {
        return symbolType;
    }
}
