package llvm.values;

import llvm.types.IntegerType;
import llvm.types.PointerType;

public class ConstString extends Const {
    private String value;
    private int length;

    public ConstString(String value) {
        super("str_" + STR_NUMBER++, new PointerType(IntegerType.i8));
        this.length = value.length() + 1;
        this.value = value.replace("\n", "\\0A") + "\\00";
    }

    public String getValue() {
        return value;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "[" + length + " x " + ((PointerType) getType()).getTargetType() + "] c\"" + value + "\"";
    }
}
