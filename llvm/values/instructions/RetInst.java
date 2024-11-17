package llvm.values.instructions;

import llvm.types.VoidType;
import llvm.values.BasicBlock;
import llvm.values.Value;

public class RetInst extends TerminatorInst {
    public RetInst(BasicBlock basicBlock) {
        //无返回值
        super(VoidType.voidType, Operator.Ret, basicBlock);
    }

    public RetInst(BasicBlock basicBlock, Value ret) {
        //有返回值
        super(ret.getType(), Operator.Ret, basicBlock);
        addOperand(ret);
    }

    public boolean isVoid() {
        return getOperands().isEmpty();
    }

    public Value getRet() {
        return getOperands().get(0);
    }

    @Override
    public String toString() {
        if (isVoid()) {
            return "ret void";
        } else {
            return "ret " + getOperands().get(0).getType() + " " + getOperands().get(0).getName();
        }
    }
}
