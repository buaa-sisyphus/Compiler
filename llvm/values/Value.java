package llvm.values;

import llvm.IRModule;
import llvm.types.Type;

import java.util.ArrayList;
import java.util.List;

public class Value {
    private final IRModule module = IRModule.getInstance();
    private String name;
    private Type type;
    private List<Use> usesList; // 使用了这个 Value 的 User 列表，这对应着 def-use 关系
    public static int REG_NUMBER = 0; // LLVM 中的寄存器编号
    public static int STR_NUMBER = 0;
    public static int LABEL_NUMBER = 0;

    public Value(String name, Type type) {
        this.name = name;
        this.type = type;
        this.usesList = new ArrayList<>();
    }

    public IRModule getModule() {
        return module;
    }

    public String getName() {
        return name;
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

    public List<Use> getUsesList() {
        return usesList;
    }

    public void setUsesList(List<Use> usesList) {
        this.usesList = usesList;
    }

    public void addUse(Use use) {
        this.usesList.add(use);
    }

    public String getGlobalName() {
        return name.replaceAll("@", "");
    }

    public boolean isNumber() {
        return this instanceof ConstInt;
    }

    public int getNumber() {
        return Integer.parseInt(name);
    }

    public boolean isGlobal() {
        return name.startsWith("@");
    }

    @Override
    public String toString() {
        return type.toString() + " " + name;
    }
}