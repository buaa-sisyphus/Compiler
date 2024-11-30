package llvm.values;

public class ReturnValue {
    public int constValue;
    public Value value;
    public boolean needLoad = false;

    public ReturnValue(int constValue) {
        this.constValue = constValue;
        this.value = null;
    }

    public ReturnValue(Value value) {
        this.value = value;
    }
}
