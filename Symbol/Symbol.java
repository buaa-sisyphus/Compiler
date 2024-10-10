package Symbol;

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

    @Override
    public String toString() {
        return scopeNum + " " + name + " " + symbolType + "\n";
    }
}
