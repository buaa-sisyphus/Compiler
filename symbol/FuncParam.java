package symbol;

import symbol.Symbol.SymbolType;
import token.Token;

public class FuncParam {
    private String funcName;
    private SymbolType type;

    public FuncParam(String funcName, SymbolType type) {
        this.funcName = funcName;
        this.type = type;
    }

    public SymbolType getType() {
        return type;
    }
}
