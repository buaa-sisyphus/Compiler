package llvm.values.instructions;

import llvm.types.FunctionType;
import llvm.types.Type;
import llvm.types.VoidType;
import llvm.values.BasicBlock;
import llvm.values.Function;
import llvm.values.Value;

import java.util.ArrayList;
import java.util.List;

public class CallInst extends TerminatorInst {

    public CallInst(BasicBlock basicBlock, Function function, List<Value> arguments) {
        super(((FunctionType) function.getType()).getReturnType(), Operator.Call, basicBlock);
        if (((FunctionType) function.getType()).getReturnType() != VoidType.voidType) {
            setName("%var_" + REG_NUMBER++);
        }
        this.addOperand(function);
        for (int i = 0; i < arguments.size(); i++) {
            addOperand(arguments.get(i));
        }
    }

    public Function getFunction() {
        return (Function) this.getOperand(0);
    }

    public List<Value> getArguments() {
        ArrayList<Value> values = new ArrayList<>();
        for (int i = 1; i < this.getOperands().size(); i++) {
            values.add(this.getOperand(i));
        }
        return values;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        Function function = this.getFunction();
        Type returnType = ((FunctionType) function.getType()).getReturnType();
        if (returnType instanceof VoidType) {
            s.append("call ");
        } else {
            s.append(this.getName()).append(" = call ");
        }
        s.append(returnType.toString()).append(" @").append(function.getName()).append("(");
        for (int i = 1; i < this.getOperands().size(); i++) {
            Value argument = this.getOperand(i);
            s.append(argument.getType().toString()).append(" ").append(argument.getName());
            if (i != this.getOperands().size() - 1) {
                s.append(", ");
            }
        }
        s.append(")");
        return s.toString();
    }
}
