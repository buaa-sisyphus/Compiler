package Symbol;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, Symbol> symbolTable = new HashMap<>();
    private SymbolTable parentTable;

    public SymbolTable(Map<String, Symbol> symbolTable, SymbolTable parentTable) {
        this.symbolTable = symbolTable;
        this.parentTable = parentTable;
    }
}
