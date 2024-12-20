package llvm.values.instructions;

import llvm.values.BasicBlock;
import llvm.values.Value;

public class StoreInst extends Instruction {
    public StoreInst(BasicBlock basicBlock, Value pointer, Value value) {
        super(value.getType(), Operator.Store, basicBlock);
        this.addOperand(value);
        this.addOperand(pointer);
    }

    public Value getValue() {
        return getOperands().get(0);
    }

    public Value getPointer() {
        return getOperands().get(1);
    }

    @Override
    public String toString() {
        return "store " + getValue().getType() + " " + getValue().getName() + ", " + getPointer().getType() + " " + getPointer().getName();
    }
}
