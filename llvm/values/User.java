package llvm.values;

import llvm.types.Type;

import java.util.ArrayList;
import java.util.List;

public class User extends Value{

    private List<Value> operands;

    public User(String name, Type type) {
        super(name, type);
        this.operands = new ArrayList<>();
    }

    public List<Value> getOperands() {
        return operands;
    }

    public Value getOperand(int index) {
        return operands.get(index);
    }

    public void addOperand(Value operand) {
        this.operands.add(operand);
    }
}
