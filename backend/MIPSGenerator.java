package backend;

import llvm.IRModule;
import llvm.types.*;
import llvm.values.*;
import llvm.values.instructions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class MIPSGenerator {
    private static MIPSGenerator instance = new MIPSGenerator();
    private StringBuilder sb;
    private int space = 0;
    private HashMap<String, StackSlot> stack = new HashMap<>();

    public static MIPSGenerator getInstance() {
        return instance;
    }

    private void addSpace(int size) {
        sb.append("\tsubu $sp, $sp, ").append(size).append("\n");
    }

    private void subSpace(int size) {
        sb.append("\taddu $sp, $sp, ").append(size).append("\n");
    }

    private StackSlot push(int size, Value value) {
        if (stack.containsKey(value.getNameWithID())) {
            return stack.get(value.getNameWithID());
        }
        space += size;
        StackSlot stackSlot = new StackSlot(space, value);
        stack.put(value.getNameWithID(), stackSlot);
        return stackSlot;
    }

    public String generateMIPS(IRModule irModule) {
        sb = new StringBuilder();
        space = 0;
        sb.append(".data\n");
        //.data
        for (GlobalVar globalVar : irModule.getGlobals()) {
            stack.put(globalVar.getNameWithID(), new StackSlot(0, globalVar));
            sb.append("\t");
            if (globalVar.isInt()) {
                sb.append(globalVar.getOriginalName()).append(": .word ");
                sb.append(((ConstInt) globalVar.getValue()).getValue());
                sb.append("\n");
            } else if (globalVar.isChar()) {
                sb.append(globalVar.getOriginalName()).append(": .word ");
                sb.append((int) (((ConstChar) globalVar.getValue()).getValue()));
                sb.append("\n");
            } else if (globalVar.isString()) {
                ConstString constString = ((ConstArray) globalVar.getValue()).getConstString();
                sb.append(globalVar.getOriginalName()).append(": .asciiz \"");
                sb.append(constString.getValue().replace("\\00", "").replace("\\0A", "\\n"));
                sb.append("\" \n");
            } else {
                ConstArray array = (ConstArray) globalVar.getValue();
                if (array.getElementType() == IntegerType.i32) {
                    sb.append(globalVar.getOriginalName()).append(": .word ");
                    if (array.isAllZero()) {
                        sb.append("0:").append(array.getCapacity()).append("\n");
                    } else {
                        List<Value> values = array.getArray();
                        for (int i = 0; i < array.getCapacity(); i++) {
                            ConstInt constInt = (ConstInt) values.get(i);
                            sb.append(constInt.getValue());
                            if (i != array.getCapacity() - 1) {
                                sb.append(", ");
                            }
                        }
                        sb.append("\n");
                    }
                } else {
                    sb.append(globalVar.getOriginalName()).append(": .word ");
                    if (array.isAllZero()) {
                        sb.append("0:").append(array.getCapacity());
                        sb.append("\n");
                    } else {
                        List<Value> values = array.getArray();
                        for (int i = 0; i < array.getCapacity(); i++) {
                            ConstChar constChar = (ConstChar) values.get(i);
                            sb.append((int) constChar.getValue());
                            if (i != array.getCapacity() - 1) {
                                sb.append(", ");
                            }
                        }
                        sb.append("\n");
                    }
                }
            }
        }
        sb.append("\n");

        List<Function> functions = irModule.getFunctions();
        //.text
        sb.append(".text\n");
        sb.append("\tj main\n");
        for (int i = 5; i < functions.size(); i++) {
            space = 0;
            Function function = functions.get(i);
            sb.append(function.getName()).append(":\n");
            if (!function.getName().equals("main")) {
                space += 4;
                store("$ra", "$sp", -space);
            }
            List<Argument> arguments = function.getArguments();
            int argNum = arguments.size();
            for (int j = 0; j < 4 && j < argNum; j++) {
                //保存在寄存器里的参数
                Argument argument = arguments.get(j);
                int pos = push(4, argument).getPos();
                store("$a" + j, "$sp", -space);
            }
            for (int j = 4; j < argNum; j++) {
                //保存在栈里的参数
                Argument argument = arguments.get(j);
                load("$t0", "$sp", 4 * (j - 4));//传的时候靠近栈顶的参数index小
                int pos = push(4, argument).getPos();
                store("$t0", "$sp", -space);//保存的时候靠近栈顶的参数index大
            }

            List<BasicBlock> basicBlocks = function.getBlocks();
            for (int j = 0; j < basicBlocks.size(); j++) {
                BasicBlock basicBlock = basicBlocks.get(j);
                sb.append(basicBlock.getNameWithID()).append(":\n");
                List<Instruction> instructions = basicBlock.getInstructions();
                for (int k = 0; k < instructions.size(); k++) {
                    Instruction instruction = instructions.get(k);
                    sb.append("# ").append(instruction).append("\n");
                    if (instruction instanceof BinaryInst) {
                        translateBinaryInst((BinaryInst) instruction);
                    } else if (instruction instanceof BrInst) {
                        translateBrInst((BrInst) instruction);
                    } else if (instruction instanceof CallInst) {
                        translateCallInst((CallInst) instruction);
                    } else if (instruction instanceof TruncInst) {
                        translateTruncInst((TruncInst) instruction);
                    } else if (instruction instanceof ZextInst) {
                        translateZextInst((ZextInst) instruction);
                    } else if (instruction instanceof GEPInst) {
                        translateGEPInst((GEPInst) instruction);
                    } else if (instruction instanceof LoadInst) {
                        translateLoadInst((LoadInst) instruction);
                    } else if (instruction instanceof AllocaInst) {
                        translateAllocaInst((AllocaInst) instruction);
                    } else if (instruction instanceof RetInst) {
                        boolean isExit = false;
                        if (function.getName().equals("main")) isExit = true;
                        translateRetInst((RetInst) instruction, isExit);
                    } else if (instruction instanceof StoreInst) {
                        translateStoreInst((StoreInst) instruction);
                    }
                }
            }
        }
//        stack.forEach((key, value) -> {
//            System.out.println(key + " " + value.getPos() + " " + value.getValue());
//        });
        return sb.toString();
    }

    private void load(String reg, String pointer, int offset) {
        sb.append("\tlw ").append(reg).append(", ").append(offset).append("(").append(pointer).append(")\n");
    }

    private void load(String reg, String name) {
        if (isNumber(name)) {
            sb.append("\tli ").append(reg).append(", ").append(name).append("\n");
        } else if (stack.get(name).getValue() instanceof GlobalVar) {
            sb.append("\tla ").append(reg).append(", ").append(name).append("\n");
        } else {
            StackSlot stackSlot = stack.get(name);
            load(reg, "$sp", -stackSlot.getPos());
        }
    }

    private void store(String reg, String name, int offset) {
        sb.append("\tsw ").append(reg).append(", ").append(offset).append("(").append(name).append(")\n");
    }

    private void store(String reg, String name) {
        StackSlot stackSlot = stack.get(name);
        store(reg, "$sp", -stackSlot.getPos());
    }

    private void translateBinaryInst(BinaryInst inst) {
        Value left = inst.getLeft();
        push(4, inst);
        load("$t0", left.getNameWithID());
        if (inst.isNot()) {
            sb.append("\tnot $t0, $t0\n");
            store("$t0", inst.getNameWithID());
            return;
        }
        Value right = inst.getRight();
        load("$t1", right.getNameWithID());
        String op = "";
        if (inst.isAdd()) {
            op = "addu";
        } else if (inst.isSub()) {
            op = "subu";
        } else if (inst.isMul()) {
            op = "mul";
        } else if (inst.isDiv()) {
            op = "div";
        } else if (inst.isMod()) {
            op = "rem";
        } else if (inst.isGt()) {
            op = "sgt";
        } else if (inst.isLt()) {
            op = "slt";
        } else if (inst.isEq()) {
            op = "seq";
        } else if (inst.isLe()) {
            op = "sle";
        } else if (inst.isGe()) {
            op = "sge";
        } else if (inst.isNe()) {
            op = "sne";
        }
        sb.append('\t').append(op).append(", ").append("$t0").append(", ").append("$t0").append(", ").append("$t1").append("\n");
        store("$t0", inst.getNameWithID());
    }

    private void translateBrInst(BrInst inst) {
        if (inst.hasCond()) {
            Value cond = inst.getCond();
            load("$t0", cond.getNameWithID());
            sb.append("\tbgtz $t0, ").append(inst.getTrueBlock().getNameWithID()).append("\n");
            sb.append("\tj ").append(inst.getFalseBlock().getNameWithID()).append("\n");
        } else {
            sb.append("\tj ").append(inst.getTrueBlock().getNameWithID()).append("\n");
        }
    }

    private void translateCallInst(CallInst inst) {
        Function function = inst.getFunction();
        if (function.isLibraryFunction()) {
            String name = function.getName();
            if (name.equals("getint")) {
                push(4, inst);
                syscall(5);
                store("$v0", inst.getNameWithID());
            } else if (name.equals("getchar")) {
                push(4, inst);
                syscall(12);
                store("$v0", inst.getNameWithID());
            } else if (name.equals("putint")) {
                load("$a0", inst.getOperand(1).getNameWithID());
                syscall(1);
            } else if (name.equals("putchar")) {
                load("$a0", inst.getOperand(1).getNameWithID());
                syscall(11);
            } else if (name.equals("putstr")) {
                load("$a0", inst.getOperand(1).getNameWithID());
                syscall(4);
            }
        } else {
            if (((FunctionType) function.getType()).getReturnType() != VoidType.voidType) {
                push(4, inst);
            }
            List<Value> params = inst.getParams();
            int size = params.size();
            int paramInStack = 0;
            for (int i = 0; i < 4 && i < size; i++) {
                //前四个参数
                load("$a" + i, params.get(i).getNameWithID());
            }
            if (size > 4) {
                space += 4 * (size - 4);
                for (int i = 4; i < size; i++) {
                    //传参的时候靠近栈顶的参数index小
                    load("$t0", params.get(i).getNameWithID());
                    store("$t0", "$sp", -space + 4 * paramInStack);
                    paramInStack++;
                }
            }

            sb.append("\tsubu $sp, $sp, ").append(space).append("\n");
            sb.append("\tjal ").append(function.getNameWithID()).append("\n");
            sb.append("\taddu $sp, $sp, ").append(space).append("\n");
            space -= 4 * paramInStack;
            if (((FunctionType) function.getType()).getReturnType() != VoidType.voidType) {
                store("$v0", inst.getNameWithID());
            }
        }
    }

    private void translateTruncInst(TruncInst inst) {
        push(4, inst);
        load("$t0", inst.getOperand(0).getNameWithID());
        sb.append("\tandi $t0, $t0, 0xFF\n");
        store("$t0", inst.getNameWithID());
    }

    private void translateZextInst(ZextInst inst) {
        //如果一开始就是用32位保存的，是不是就不需要扩展了，这个指令貌似没掉毛用?
//        push(4, inst);
//        load("$t0", inst.getOperand(0).getNameWithID());
//        store("$t0", inst.getNameWithID());
        StackSlot tmp = stack.get(inst.getOperand(0).getNameWithID());
        StackSlot stackSlot = new StackSlot(tmp.getPos(), inst);
        stack.put(inst.getNameWithID(), stackSlot);
    }

    private void translateLoadInst(LoadInst inst) {
        push(4, inst);
        if (inst.getOperand(0) instanceof GEPInst || inst.getOperand(0) instanceof GlobalVar) {
            load("$t0", inst.getOperand(0).getNameWithID());
            load("$t1", "$t0", 0);
            store("$t1", inst.getNameWithID());
        } else {
            load("$t0", inst.getOperand(0).getNameWithID());
            store("$t0", inst.getNameWithID());
        }
    }

    private void translateAllocaInst(AllocaInst inst) {
        if (inst.getAllocaType() instanceof IntegerType) {
            push(4, inst);
        } else if (inst.getAllocaType() instanceof ArrayType) {
            int size = ((ArrayType) inst.getAllocaType()).getLength();
            space += 4 * size;
            sb.append("\tsubu $t0, $sp, ").append(space).append("\n");//计算数组首地址
            push(4, inst);
            store("$t0", inst.getNameWithID());
        } else if (inst.getAllocaType() instanceof PointerType) {
            push(4, inst);
        }
    }

    private void syscall(int op) {
        sb.append("\tli $v0, ").append(op).append("\n\tsyscall\n");
    }

    private void translateStoreInst(StoreInst inst) {
        if (inst.getPointer() instanceof GEPInst || inst.getPointer() instanceof GlobalVar) {
            load("$t0", inst.getValue().getNameWithID());
            load("$t1", inst.getPointer().getNameWithID());
            store("$t0", "$t1", 0);
        } else {
            load("$t0", inst.getValue().getNameWithID());
            store("$t0", inst.getPointer().getNameWithID());
        }
    }

    private void translateRetInst(RetInst inst, boolean isExit) {
        if (isExit) {
            sb.append("\tli $v0, 10\n").append("\tsyscall\n");
            return;
        }
        load("$ra", "$sp", -4);
        if (!inst.isVoid()) {
            load("$v0", inst.getOperand(0).getNameWithID());
        }
        sb.append("\tjr $ra\n");
    }

    private void translateGEPInst(GEPInst inst) {
        push(4, inst);
        load("$t0", inst.getPointer().getNameWithID());
        if (inst.getIndex() == null) {
            load("$t1", "0");
        } else {
            load("$t1", inst.getIndex().getNameWithID());
            sb.append("\tmul $t1, $t1 ,4\n");
        }
        sb.append("\taddu $t0, $t0, $t1\n");
        store("$t0", inst.getNameWithID());//保存地址
    }

    private boolean isNumber(String str) {
        return str.matches("-?[0-9]+");
    }
}
