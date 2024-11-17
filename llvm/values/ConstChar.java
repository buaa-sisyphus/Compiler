package llvm.values;

import llvm.types.IntegerType;

public class ConstChar extends Const{
    private int value;
    public static ConstChar ZERO = new ConstChar('\0');
    public ConstChar(int value) {
        super(String.valueOf(value), IntegerType.i8);
        this.value = value;
    }

    public ConstChar() {
        super("", IntegerType.i8);
        value='\0';
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "i8 "+this.value;
    }
}
