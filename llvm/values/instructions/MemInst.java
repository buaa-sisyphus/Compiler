package llvm.values.instructions;

import llvm.types.Type;
import llvm.values.BasicBlock;

public abstract class MemInst extends Instruction{
    public MemInst(Type type, Operator op, BasicBlock basicBlock) {
        super(type, op, basicBlock);
    }
}
