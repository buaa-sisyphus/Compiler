package llvm.values;

import llvm.types.ArrayType;
import llvm.types.IntegerType;
import llvm.types.Type;

import java.util.ArrayList;
import java.util.List;

public class ConstArray extends Const {
    private Type elementType;
    private List<Value> array;
    private int capacity;
    private boolean init = false;
    private boolean isString = false;

    public ConstArray(Type type, Type elementType, int capacity) {
        super("", type);
        this.elementType = elementType;
        this.array = new ArrayList<>();
        this.capacity = capacity;
        Value tmp;
        if (((ArrayType) type).getElementType() == IntegerType.i32) {
            tmp = ConstInt.ZERO;
        } else {
            tmp = ConstChar.ZERO;
        }
        for (int i = 0; i < ((ArrayType) type).getLength(); i++) {
            array.add(tmp);
        }
    }

    public Type getElementType() {
        return elementType;
    }

    public List<Value> getArray() {
        return array;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isInit() {
        return init;
    }

    public void setInit(boolean init) {
        this.init = init;
    }

    public void storeValue(int offset, Value value) {
        array.set(offset, value);
    }

    public void storeValue(Value string) {
        array.set(0, string);
        isString = true;
    }

    public boolean isAllZero() {
        if (!init) return true;
        Value constZero = null;
        if (elementType == IntegerType.i32) {
            constZero = ConstInt.ZERO;
        } else {
            constZero = ConstChar.ZERO;
        }
        for (Value v : array) {
            if (!v.toString().equals(constZero.toString())) {
                return false;
            }
        }
        return true;
    }

    public ConstString getConstString() {
        if (isString) return (ConstString) array.get(0);
        else return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isString) {
            //是一个字符串
            sb.append(((ConstString) array.get(0)).toString());
        } else {
            //是一个字符数组或者数字数组
            sb.append(this.getType().toString()).append(" ");
            if (isAllZero()) {
                //避免tle
                sb.append("zeroinitializer");
            } else {
                sb.append("[");
                for (int i = 0; i < array.size(); i++) {
                    if (i != 0) {
                        sb.append(", ");
                    }
                    sb.append(array.get(i).toString());
                }
                sb.append("]");
            }
        }
        return sb.toString();
    }
}
