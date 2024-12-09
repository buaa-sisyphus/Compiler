package llvm.values;

import llvm.IRModule;
import llvm.types.Type;

import java.util.ArrayList;
import java.util.List;

public class Value {
    private final IRModule module = IRModule.getInstance();
    private String name;
    private Type type;
    private int id; // 全局唯一的id
    public static int REG_NUMBER = 0; // LLVM 中的寄存器编号
    public static int STR_NUMBER = 0; // 全局字符串的编号
    public static int LABEL_NUMBER = 0; // LLVM 中基本块的编号
    public static int MIPS_ID = 0;

    public Value(String name, Type type) {
        this.name = name;
        this.type = type;
        this.id = MIPS_ID++;
    }

    public String getName() {
        return name;
    }

    public String getNameWithID() {
        if (isNumber()) return getName();
        else if (this instanceof Function) return getName();
        else if (this instanceof GlobalVar) return ((GlobalVar) this).getOriginalName();
        else return getName() + "_" + +id;
    }

    public boolean isNumber() {
        return name.matches("-?[0-9]+");
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type.toString() + " " + name;
    }
}
