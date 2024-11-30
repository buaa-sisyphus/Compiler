package llvm.values.instructions;

import llvm.types.PointerType;
import llvm.types.Type;
import llvm.values.BasicBlock;

public class AllocaInst extends Instruction {
    private boolean isConst;
    private Type allocaType;

    public AllocaInst(BasicBlock basicBlock, boolean isConst, Type allocaType) {
        super(new PointerType(allocaType), Operator.Alloca, basicBlock);
        this.setName("%var_" + REG_NUMBER++);
        this.isConst = isConst;
        this.allocaType = allocaType;
    }

    public boolean isConst() {
        return isConst;
    }

    public void setConst(boolean aConst) {
        isConst = aConst;
    }

    public Type getAllocaType() {
        return allocaType;
    }

    @Override
    public String toString() {
        return this.getName() + " = alloca " + this.getAllocaType();
    }
}
