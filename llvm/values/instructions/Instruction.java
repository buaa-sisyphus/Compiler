package llvm.values.instructions;

import llvm.IRModule;
import llvm.types.Type;
import llvm.values.BasicBlock;
import llvm.values.User;

public class Instruction extends User {
    private Operator operator;
    private int handler;
    private BasicBlock basicBlock;
    private static int HANDLER = 0;

    public Instruction(Type type, Operator operator, BasicBlock basicBlock) {
        super("", type);
        this.operator = operator;
        this.basicBlock = basicBlock;
        this.handler = HANDLER++;
        IRModule.getInstance().addInstruction(this);
    }

    public int getHandler() {
        return handler;
    }

    public BasicBlock getBasicBlock() {
        return basicBlock;
    }

    public Operator getOperator() {
        return operator;
    }
}
