package Symbol;

import java.util.List;

public class ArraySymbol extends Symbol {
    private List<Object> values;

    public ArraySymbol(String name, int scopeNum, SymbolType symbolType, int lineNum, List<Object> values) {
        this.name = name;
        this.scopeNum = scopeNum;
        this.symbolType = symbolType;
        this.lineNum = lineNum;
        this.values = values;
    }
}
