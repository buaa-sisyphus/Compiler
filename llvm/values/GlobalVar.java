package llvm.values;

import llvm.IRModule;
import llvm.types.PointerType;
import llvm.types.Type;

public class GlobalVar extends User {
    private boolean isConst; // 是否是常量
    private boolean isString;
    private Value value;

    public GlobalVar(String name, Type type, Value value, boolean isConst, boolean isString) {
        super("@" + name, new PointerType(type));
        this.isConst = isConst;
        this.value = value;
        this.isString = isString;
    }

    public boolean isConst() {
        return isConst;
    }

    public void setConst(boolean aConst) {
        isConst = aConst;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public boolean isString() {
        return value instanceof ConstString;
    }

    public boolean isInt() {
        return value instanceof ConstInt;
    }

    public boolean isArray() {
        return value instanceof ConstArray;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getName()).append(" = ");
        if (isString) {
            sb.append("private unnamed_addr constant ");
        } else if (isConst) {
            sb.append("constant ");
        } else {
            sb.append("global ");
        }
        if (value != null) {
            sb.append(value);
        }
        return sb.toString();
    }
}