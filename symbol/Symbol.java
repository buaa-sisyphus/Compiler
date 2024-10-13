package symbol;

public abstract class Symbol {
    protected String name;
    protected int scopeNum;
    protected int type;// 0 var 1 array 2 func
    protected int btype; // 0 int 1 char 2 void
    protected int con;//0 var 1 const
    protected int lineNum;//标识符声明的位置

    @Override
    public String toString() {
        return scopeNum + " " + name + " " + toType() + "\n";
    }

    public String toType() {
        String str = "";
        if (con == 1) str += "Const";

        if (btype == 0) str += "Int";
        else if (btype == 1) str += "Char";
        else str += "Void";

        if (type == 1) str += "Array";
        else if (type == 2) str += "Func";

        return str;
    }

    public int getScopeNum() {
        return scopeNum;
    }

    public String getName() {
        return name;
    }

    public int getBtype() {
        return btype;
    }

    public int getCon() {
        return con;
    }

    public int getLineNum() {
        return lineNum;
    }

    public int getType() {
        return type;
    }
}
