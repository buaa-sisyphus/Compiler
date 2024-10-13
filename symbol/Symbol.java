package symbol;

public abstract class Symbol {
    protected String name;
    protected int scopeNum;
    protected SymbolType symbolType;
    protected int lineNum;//标识符声明的位置

    public enum SymbolType {
        Int,
        Char,
        ConstChar,
        ConstInt,
        ConstCharArray,
        ConstIntArray,
        CharArray,
        IntArray,
        VoidFunc,
        CharFunc,
        IntFunc,
    }

    public Symbol() {
    }

    public String getName() {
        return name;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    @Override
    public String toString() {
        return scopeNum + " " + name + " " + symbolType + "\n";
    }
}
