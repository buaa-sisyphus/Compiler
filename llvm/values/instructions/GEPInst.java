package llvm.values.instructions;

import llvm.types.ArrayType;
import llvm.types.IntegerType;
import llvm.types.PointerType;
import llvm.types.Type;
import llvm.values.BasicBlock;
import llvm.values.Value;

public class GEPInst extends MemInst{
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
        return getOperands().get(1);
    }

    private static Type getElementType(Value pointer) {
        Type type = pointer.getType();
        if (type instanceof PointerType) {
            Type targetType = ((PointerType) type).getTargetType();
            if (targetType instanceof ArrayType) {
                return ((ArrayType) targetType).getElementType();
            }else if(targetType instanceof IntegerType) {
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
        s.append(getName()).append(" = getelementptr ");
        s.append(((PointerType)pointer.getType()).getTargetType()).append(", ");
        s.append(pointer.getType()).append(" ").append(pointer.getName()).append(", ");
        s.append("i32 0, ").append(index.getType()).append(" ").append(index.getName());
        return s.toString();
    }
}
