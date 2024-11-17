package llvm.values;

import llvm.types.LabelType;
import llvm.types.Type;
import llvm.values.instructions.Instruction;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock extends Value {
    private List<Instruction> instructions = new ArrayList<>();
    private Function belongFunc;//这个基本块所属的函数
    private List<BasicBlock> preBlocks = new ArrayList<>();//前驱
    private List<BasicBlock> sucBlocks = new ArrayList<>();//后继

    public BasicBlock(Function function) {
        super(String.valueOf(REG_NUMBER++), new LabelType());
        this.belongFunc = function;
    }

    public void addInstruction(Instruction instruction) {
        instructions.add(instruction);
    }

    public void addPreBlock(BasicBlock bb) {
        preBlocks.add(bb);
    }

    public void addSucBlock(BasicBlock bb) {
        sucBlocks.add(bb);
    }

    public Function getBelongFunc() {
        return belongFunc;
    }

    public List<BasicBlock> getPreBlocks() {
        return preBlocks;
    }

    public List<BasicBlock> getSucBlocks() {
        return sucBlocks;
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
