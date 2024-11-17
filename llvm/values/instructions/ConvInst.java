package llvm.values.instructions;

import llvm.types.IntegerType;
import llvm.types.PointerType;
import llvm.types.VoidType;
import llvm.values.BasicBlock;
import llvm.values.Value;

public class ConvInst extends Instruction {
    public ConvInst(BasicBlock basicBlock, Operator op, Value value) {
        super(VoidType.voidType, op, basicBlock);
        this.setName("%" + REG_NUMBER++);
        if (op == Operator.Zext) {
            setType(IntegerType.i32);
        } else {
            setType(IntegerType.i8);
        }
        addOperand(value);
    }

    @Override
    public String toString() {
        if (getOperator() == Operator.Zext) {
            return getName() + " = zext " + getOperands().get(0).getType() + " " + getOperands().get(0).getName() + " to i32";
        } else {
            return getName() + " = trunc " + getOperands().get(0).getType() + " " + getOperands().get(0).getName() + " to i8";
        }
    }
}
