package symbol;

import token.Token;

public class FuncParam {
    private String funcName;
    private int btype;//0 int 1 char
    private int type;//0 var 1 array

    public FuncParam(String funcName, int btype, int type) {
        this.funcName = funcName;
        this.btype = btype;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public int getBtype() {
        return btype;
    }

    public String getFuncName() {
        return funcName;
    }

    public String toType() {
        String str = "";
        if (btype == 0) str += "Int";
        else str += "Char";
        if (type == 1) str += "Array";
        return str+"\n";
    }
}
