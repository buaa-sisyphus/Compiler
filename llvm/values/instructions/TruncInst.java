package llvm.values.instructions;

import llvm.types.IntegerType;
import llvm.types.VoidType;
import llvm.values.BasicBlock;
import llvm.values.Value;

public class TruncInst extends Instruction {
    public TruncInst(BasicBlock basicBlock, Operator op, Value value) {
        super(VoidType.voidType, op, basicBlock);
        this.setName("%var_" + REG_NUMBER++);
        setType(IntegerType.i8);
        addOperand(value);
    }

    @Override
    public String toString() {
        return getName() + " = trunc " + getOperands().get(0).getType() + " " + getOperands().get(0).getName() + " to i8";
    }
}
