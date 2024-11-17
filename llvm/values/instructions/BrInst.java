package llvm.values.instructions;

import llvm.types.IntegerType;
import llvm.types.Type;
import llvm.types.VoidType;
import llvm.values.BasicBlock;
import llvm.values.ConstInt;
import llvm.values.Value;

public class BrInst extends Instruction {

    public BrInst(BasicBlock basicBlock, BasicBlock trueBlock) {
        super(VoidType.voidType, Operator.Br, basicBlock);
        addOperand(trueBlock);
    }

    public BrInst(BasicBlock basicBlock, BasicBlock trueBlock, BasicBlock falseBlock, Value cond) {
        super(VoidType.voidType, Operator.Br, basicBlock);
        addOperand(cond);
        addOperand(trueBlock);
        addOperand(falseBlock);
    }

    public boolean hasCond() {
        if (getOperands().size() == 3) {
            return true;
        } else {
            return false;
        }
    }

    public Value getTrueBlock() {
        if (hasCond()) {
            return getOperands().get(1);
        } else {
            return getOperands().get(0);
        }
    }

    public Value getCond() {
        if (hasCond()) {
            return getOperands().get(0);
        }else return null;
    }

    public Value getFalseBlock() {
        if (hasCond()) {
            return getOperands().get(2);
        }else{
            return getOperands().get(1);
        }
    }

    @Override
    public String toString() {
        if (this.getOperands().size() == 1) {
            return "br label %" + this.getOperands().get(0).getName();
        } else {
            return "br " + this.getOperands().get(0).getType() + " " + this.getOperands().get(0).getName() + ", label %" + this.getOperands().get(1).getName() + ", label %" + this.getOperands().get(2).getName();
        }
    }
}
