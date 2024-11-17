package llvm.values.instructions;

import llvm.types.ArrayType;
import llvm.types.PointerType;
import llvm.types.Type;
import llvm.values.BasicBlock;

public class AllocaInst extends MemInst{
    private boolean isConst;
    private Type allocaType;

    public AllocaInst(BasicBlock basicBlock, boolean isConst, Type allocaType) {
        super(new PointerType(allocaType), Operator.Alloca, basicBlock);
        this.setName("%" + REG_NUMBER++);
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

    public void setAllocaType(Type allocaType) {
        this.allocaType = allocaType;
    }

    @Override
    public String toString() {
        return this.getName() + " = alloca " + this.getAllocaType();
    }
}
