package symbol;

import java.util.*;

public class SymbolScope {
    private Map<String, Symbol> symbolTable = new LinkedHashMap<>();
    private List<SymbolScope> children = new ArrayList<>();
    private SymbolScope parent;
    private int scopeNum;
    private boolean needReturn = false;
    private boolean isFunc = false;

    public SymbolScope() {
    }

    public void setParent(SymbolScope parentTable) {
        this.parent = parentTable;
    }

    public void addSymbol(String ident, Symbol symbol) {
        symbolTable.put(ident, symbol);
    }

    public void addChild(SymbolScope child) {
        children.add(child);
    }

    public void setFunc(boolean func) {
        isFunc = func;
    }

    public boolean isFunc() {
        return isFunc;
    }

    public Symbol getSymbol(String ident) {
        return symbolTable.get(ident);
    }

    public Symbol getSymbolDeep(String ident) {
        Symbol symbol = null;
        for (SymbolScope table = this; table != null; table = table.parent) {
            symbol = table.getSymbol(ident);
            if (symbol != null) {
                return symbol;
            }
        }
        return symbol;
    }

    public Set<Map.Entry<String, Symbol>> entrySet() {
        return symbolTable.entrySet();
    }

    public List<SymbolScope> getChildren() {
        return children;
    }

    public SymbolScope getParent() {
        return parent;
    }

    public int getScopeNum() {
        return scopeNum;
    }


    public void setScopeNum(int scopeNum) {
        this.scopeNum = scopeNum;
    }

    public void setNeedReturn(boolean needReturn) {
        this.needReturn = needReturn;
    }

    public boolean getNeedReturn() {
        return needReturn;
    }
}
