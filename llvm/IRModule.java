package llvm;

import llvm.values.BasicBlock;
import llvm.values.Function;
import llvm.values.GlobalVar;
import llvm.values.instructions.Instruction;

import java.util.ArrayList;
import java.util.List;

public class IRModule {
    private static final IRModule instance = new IRModule();
    private List<GlobalVar> globals = new ArrayList<>();
    private List<Function> functions = new ArrayList<>();
    private List<Instruction> instructions = new ArrayList<>();

    private IRModule() {
    }

    public static IRModule getInstance() {
        return instance;
    }

    public void addGlobalVar(GlobalVar g) {
        globals.add(g);
    }

    public void addFunction(Function f) {
        functions.add(f);
    }

    public void addInstruction(Instruction i) {
        instructions.add(i);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (GlobalVar globalVar : globals) {
            s.append(globalVar.toString()).append("\n");
        }
        if (!globals.isEmpty()) {
            s.append("\n");
        }

        //先打印输入和输出函数
        int i=0;
        for (i = 0; i < 5; i++) {
            Function function = functions.get(i);
            s.append("declare ").append(function.toString()).append("\n");
        }
        s.append("\n");

        for (i=5; i < functions.size(); i++) {
            Function function = functions.get(i);
            s.append("define dso_local ").append(function.toString()).append("{\n");
            for (BasicBlock basicBlock : function.getBlocks()) {
                s.append(basicBlock.getName()).append(":\n").append(basicBlock.toString());
            }
            s.append("}\n\n");

        }
        return s.toString();
    }
}
