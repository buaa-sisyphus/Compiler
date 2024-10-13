package symbol;

import token.Token;

import java.util.*;

public class SymbolTable {
    private Map<String, Symbol> symbolTable = new LinkedHashMap<>();
    private List<SymbolTable> childrenTables = new ArrayList<>();
    private SymbolTable parentTable;
    private int scopeNum;

    public SymbolTable() {
    }

    public void setParentTable(SymbolTable parentTable) {
        this.parentTable = parentTable;
    }

    public void addSymbol(String ident, Symbol symbol) {
        symbolTable.put(ident, symbol);
    }

    public void addChild(SymbolTable child) {
        childrenTables.add(child);
    }

    public boolean findSymbol(String ident) {
        return symbolTable.containsKey(ident);
    }

    public Symbol getSymbol(String ident) {
        return symbolTable.get(ident);
    }

    public Symbol getSymbolDeep(String ident) {
        Symbol symbol = null;
        for (SymbolTable table = this; table != null; table = table.parentTable) {
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

    public List<SymbolTable> getChildrenTables() {
        return childrenTables;
    }

    public SymbolTable getParentTable() {
        return parentTable;
    }

    public int getScopeNum() {
        return scopeNum;
    }

    public void setScopeNum(int scopeNum) {
        this.scopeNum = scopeNum;
    }
}
