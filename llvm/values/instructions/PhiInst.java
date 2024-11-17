package llvm.values.instructions;

import llvm.types.Type;
import llvm.values.BasicBlock;
import llvm.values.Value;

import java.util.List;

public class PhiInst extends MemInst{
    public PhiInst(BasicBlock basicBlock, Type type, List<Value> values) {
        super(type, Operator.Phi, basicBlock);
        for (Value value : values) {
            this.addOperand(value);
        }
        this.setName("%" + REG_NUMBER++);
    }
}
