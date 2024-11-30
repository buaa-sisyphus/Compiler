package llvm.values.instructions;

import llvm.types.IntegerType;
import llvm.types.Type;
import llvm.types.VoidType;
import llvm.values.BasicBlock;
import llvm.values.Value;

public class ZextInst extends Instruction{
    public ZextInst(BasicBlock basicBlock, Operator op, Value value) {
        super(VoidType.voidType, op, basicBlock);
        this.setName("%var_" + REG_NUMBER++);
        setType(IntegerType.i32);
        addOperand(value);
    }

    @Override
    public String toString() {
        return getName() + " = zext " + getOperands().get(0).getType() + " " + getOperands().get(0).getName() + " to i32";
    }
}
