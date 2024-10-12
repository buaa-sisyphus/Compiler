package symbol;

import token.Token;
import token.TokenType;

import java.util.List;

public class FuncSymbol extends Symbol {
    private List<FuncParam> params;

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
}
