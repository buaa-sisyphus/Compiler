package llvm.values;

public class returnValue {
    public int constValue;
    public Value value;
    public returnValue(int constValue) {
        this.constValue = constValue;
        this.value = null;
    }
    public returnValue(Value value) {
        this.value = value;
    }
}
