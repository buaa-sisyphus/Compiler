package llvm.values;

import llvm.types.LabelType;
import llvm.types.Type;
import llvm.values.instructions.BrInst;
import llvm.values.instructions.Instruction;
import llvm.values.instructions.RetInst;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock extends Value {
    private List<Instruction> instructions = new ArrayList<>();
    private Function belongFunc;//这个基本块所属的函数

    public BasicBlock(Function function) {
        super(String.valueOf("label_" + LABEL_NUMBER++), new LabelType());
        this.belongFunc = function;
    }

    public void addInstruction(Instruction instruction) {
        if (!hasEnd()) instructions.add(instruction);
    }

    public boolean hasEnd() {
        int size = instructions.size();
        //如果基本块的最后一句是br或者ret，返回true
        return !instructions.isEmpty() && (instructions.get(size - 1) instanceof BrInst || instructions.get(size - 1) instanceof RetInst);
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (Instruction instruction : instructions) {
            s.append("\t").append(instruction.toString()).append("\n");
        }
        return s.toString();
    }
}
