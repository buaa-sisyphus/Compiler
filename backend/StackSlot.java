package backend;

import llvm.values.Value;

// 栈中的一个单元
public class StackSlot {
    private int pos;
    private Value value;

    public StackSlot(int pos, Value value) {
        this.pos = pos;
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    public int getPos() {
        return pos;
    }
}
