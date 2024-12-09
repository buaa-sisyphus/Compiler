package llvm.types;

import llvm.values.ConstInt;
import llvm.values.Value;

import java.util.ArrayList;
import java.util.List;

public class ArrayType implements Type {
    private final Type elementType;
    private final int length;

    public ArrayType(Type elementType) {
        this.elementType = elementType;
        this.length = 0;
    }

    public ArrayType(Type elementType, int length) {
        this.elementType = elementType;
        this.length = length;
    }

    public Type getElementType() {
        return elementType;
    }

    public boolean isString() {
        return elementType instanceof IntegerType && ((IntegerType) elementType).isI8();
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "[" + length + " x " + elementType.toString() + "]";
    }

}
