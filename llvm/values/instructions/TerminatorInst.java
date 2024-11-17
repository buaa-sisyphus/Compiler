package llvm.values.instructions;

import llvm.types.Type;
import llvm.values.BasicBlock;

public class TerminatorInst extends Instruction{
    public TerminatorInst(Type type, Operator operator, BasicBlock basicBlock) {
        super(type, operator, basicBlock);
    }
}
