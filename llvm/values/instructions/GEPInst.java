package llvm.values.instructions;

import llvm.types.ArrayType;
import llvm.types.IntegerType;
import llvm.types.PointerType;
import llvm.types.Type;
import llvm.values.BasicBlock;
import llvm.values.Value;

public class GEPInst extends Instruction {
    private Type elementType;

    public GEPInst(BasicBlock basicBlock, Value pointer, Value index) {
        super(new PointerType(getElementType(pointer)), Operator.GEP, basicBlock);
        this.setName("%var_" + REG_NUMBER++);
        this.addOperand(pointer);
        this.addOperand(index);
        this.elementType = getElementType(pointer);
    }

    public Value getPointer() {
        return getOperands().get(0);
    }

    public Value getIndex() {
        if (getOperands().size() > 1)
            return getOperands().get(1);
        else return null;
    }

    private static Type getElementType(Value pointer) {
        Type type = pointer.getType();
        if (type instanceof PointerType) {
            Type targetType = ((PointerType) type).getTargetType();
            if (targetType instanceof ArrayType) {
                return ((ArrayType) targetType).getElementType();
            } else if (targetType instanceof IntegerType) {
                return targetType;
            }
        }
        //寄
        return null;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        Value pointer = getPointer();
        Value index = getIndex();
        s.append(getName()).append(" = getelementptr inbounds ");
        s.append(((PointerType) pointer.getType()).getTargetType()).append(", ");
        s.append(pointer.getType()).append(" ").append(pointer.getName()).append(", ");

        if (((PointerType) pointer.getType()).getTargetType() instanceof ArrayType) {
            //一维数组指针，如[i8 x 8]*
            s.append("i32 0, ").append(index.getType()).append(" ").append(index.getName());
        } else {
            //普通指针，如i8*
            s.append(index.getType()).append(" ").append(index.getName());
        }

        return s.toString();
    }
}
