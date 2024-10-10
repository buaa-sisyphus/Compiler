package Symbol;

import java.util.List;

public class FuncSymbol extends Symbol {
    private List<FuncFParam> params;

    public FuncSymbol(String name, int scopeNum, SymbolType symbolType, int lineNum, List<FuncFParam> params) {
        this.name = name;
        this.scopeNum = scopeNum;
        this.symbolType = symbolType;
        this.lineNum = lineNum;
        this.params = params;
    }
}
