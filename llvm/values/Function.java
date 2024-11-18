package llvm.values;

import llvm.types.FunctionType;
import llvm.types.Type;

import java.util.ArrayList;
import java.util.List;

public class Function extends Value {
    private List<BasicBlock> blocks = new ArrayList<>();
    private List<Argument> arguments = new ArrayList<>();
    private boolean isLibraryFunction = false;

    public Function(String name, Type type, boolean isLibraryFunction) {
        super(name, type);
        //重置REG_NUMBER和LABEL_NUMBER
        REG_NUMBER = 0;
        LABEL_NUMBER=0;
        this.isLibraryFunction = isLibraryFunction;
    }

    public void addBlock(BasicBlock block) {
        blocks.add(block);
    }

    public void addArgument(Argument argument) {
        arguments.add(argument);
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    public void setArguments(List<Argument> arguments) {
        this.arguments = arguments;
    }

    public boolean isLibraryFunction() {
        return isLibraryFunction;
    }

    public List<BasicBlock> getBlocks() {
        return blocks;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(((FunctionType) this.getType()).getReturnType()).append(" @").append(this.getName()).append("(");
        if (isLibraryFunction) {
            List<Type> types = ((FunctionType) this.getType()).getParametersType();
            for (int i = 0; i < types.size(); i++) {
                Type t = types.get(i);
                s.append(t.toString());
                if (i != types.size() - 1) {
                    s.append(", ");
                }
            }
        } else {
            for (int i = 0; i < arguments.size(); i++) {
                Argument argument = arguments.get(i);
                s.append(argument.getType()).append(" ");
                s.append(argument.getName());
                if (i != arguments.size() - 1) {
                    s.append(", ");
                }
            }
        }
        s.append(")");
        return s.toString();
    }
}
