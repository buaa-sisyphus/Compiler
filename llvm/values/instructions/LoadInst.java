package llvm.values.instructions;

import llvm.types.ArrayType;
import llvm.types.PointerType;
import llvm.values.BasicBlock;
import llvm.values.Value;

public class LoadInst extends Instruction {
    public LoadInst(BasicBlock basicBlock, Value pointer) {
        super(((PointerType) pointer.getType()).getTargetType(), Operator.Load, basicBlock);
        this.setName("%var_" + REG_NUMBER++);
        if (getType() instanceof ArrayType) {
            setType(new PointerType(((ArrayType) getType()).getElementType()));
        }
        this.addOperand(pointer);
    }

    public Value getPointer() {
        return getOperands().get(0);
    }

    @Override
    public String toString() {
        return getName() + " = load " + getType() + ", " + getPointer().getType() + " " + getPointer().getName();
    }
}
