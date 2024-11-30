package llvm.values;

import llvm.IRModule;
import llvm.types.IntegerType;
import llvm.types.PointerType;
import llvm.types.Type;

public class GlobalVar extends User {
    private boolean isConst; // 是否是常量
    private boolean isString;
    private Value value;
    private String originalName;

    public GlobalVar(String name, Type type, Value value, boolean isConst, boolean isString) {
        super("@" + name, new PointerType(type));
        this.isConst = isConst;
        this.value = value;
        this.isString = isString;
        this.originalName = name;
    }

    public boolean isConst() {
        return isConst;
    }

    public Value getValue() {
        return value;
    }

    public boolean isString() {
        return isString;
    }

    public String getOriginalName() {
        return originalName;
    }

    public boolean isInt() {
        return ((PointerType) getType()).getTargetType() == IntegerType.i32;
    }

    public boolean isChar() {
        return ((PointerType) getType()).getTargetType() == IntegerType.i8;
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
