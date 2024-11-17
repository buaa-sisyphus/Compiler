package llvm.values;

import llvm.IRModule;
import llvm.types.*;
import llvm.values.instructions.*;

import java.util.ArrayList;
import java.util.List;

public class BuildFactory {
    private static final BuildFactory buildFactory = new BuildFactory();

    private BuildFactory() {
    }

    public static BuildFactory getInstance() {
        return buildFactory;
    }

    /**
     * Functions
     **/
    public Function buildFunction(String name, Type ret, List<Type> parametersTypes) {
        Function function = new Function(name, new FunctionType(ret, parametersTypes), false);
        IRModule.getInstance().addFunction(function);
        return function;
    }

    public Function buildLibraryFunction(String name, Type ret, List<Type> parametersTypes) {
        Function function = new Function(name, new FunctionType(ret, parametersTypes), true);
        IRModule.getInstance().addFunction(function);
        return function;
    }

    public List<Argument> getFunctionArguments(Function function) {
        return function.getArguments();
    }

    public Argument buildArgument(Type type, Function function, int index, boolean isLibraryFunction) {
        Argument argument = new Argument(type, index, function, isLibraryFunction);
        function.addArgument(argument);
        return argument;
    }

    public CallInst buildCall(BasicBlock basicBlock, Function function, List<Value> arguments) {
        List<Type> types = ((FunctionType) function.getType()).getParametersType();
        List<Value> newArguments = new ArrayList<Value>();
        for (int i = 0; i < arguments.size(); i++) {
            Type type = types.get(i);
            Value arg = arguments.get(i);
            if (type == IntegerType.i32 && arg.getType() == IntegerType.i8) {
                newArguments.add(buildZext(basicBlock, arg));
            } else if (type == IntegerType.i8 && arg.getType() == IntegerType.i32) {
                newArguments.add(buildTrunc(basicBlock, arg));
            } else {
                newArguments.add(arg);
            }
        }
        CallInst callInst = new CallInst(basicBlock, function, newArguments);
        basicBlock.addInstruction(callInst);
        return callInst;
    }

    /**
     * BasicBlock
     */
    public BasicBlock buildBasicBlock(Function function) {
        BasicBlock block = new BasicBlock(function);
        function.addBlock(block);
        return block;
    }

    /**
     * BinaryInst
     **/
    public BinaryInst buildBinary(BasicBlock basicBlock, Operator op, Value left, Value right) {
        Value newLeft = left;
        Value newRight = right;
        if (left.getType() == IntegerType.i8) {
            newLeft = buildZext(basicBlock, left);
        }
        if (right.getType() == IntegerType.i8) {
            newRight = buildZext(basicBlock, right);
        }
        BinaryInst binaryInst = new BinaryInst(basicBlock, op, newLeft, newRight);
        basicBlock.addInstruction(binaryInst);
        if (op == Operator.And || op == Operator.Or) {
            binaryInst = buildBinary(basicBlock, Operator.Ne, binaryInst, ConstInt.ZERO);
            basicBlock.addInstruction(binaryInst);
        }
        return binaryInst;
    }

    public BinaryInst buildNot(BasicBlock basicBlock, Value value) {
        return buildBinary(basicBlock, Operator.Eq, value, ConstInt.ZERO);
    }

    /**
     * Var
     */
    public GlobalVar buildGlobalVar(String name, Type type, Value value,boolean isConst, boolean isString) {
        GlobalVar var = new GlobalVar(name, type, value,isConst, isString);
        IRModule.getInstance().addGlobalVar(var);
        return var;
    }

    public AllocaInst buildVar(BasicBlock basicBlock, Value value, boolean isConst, Type allocaType) {
        //先分配地址
        AllocaInst allocaInst = new AllocaInst(basicBlock, isConst, allocaType);
        basicBlock.addInstruction(allocaInst);
        if (value != null) {
            //如果变量有值，就保存
            if (allocaType != value.getType()) {
                //如果分配的类型和值的类型不一样，要进行位操作
                if (allocaType == IntegerType.i32) {
                    value = buildZext(basicBlock, value);
                } else {
                    value = buildTrunc(basicBlock, value);
                }
            }
            buildStore(basicBlock, allocaInst, value);
        }
        return allocaInst;
    }

    public ConstInt getConstInt(int value) {
        return new ConstInt(value);
    }

    public ConstChar getConstChar(int value) {
        return new ConstChar(value);
    }

    public ConstString getConstString(String value) {
        return new ConstString(value);
    }

    /**
     * Array
     */
    public GlobalVar buildGlobalArray(String name, Type type, boolean isConst,boolean isString) {
        Value constArray = new ConstArray(type, ((ArrayType) type).getElementType(), ((ArrayType) type).getCapacity());
        GlobalVar var = new GlobalVar(name, type, constArray,isConst, isString);
        IRModule.getInstance().addGlobalVar(var);
        return var;
    }

    public AllocaInst buildArray(BasicBlock basicBlock, boolean isConst, Type arrayType) {
        AllocaInst allocaInst = new AllocaInst(basicBlock, isConst, arrayType);
        basicBlock.addInstruction(allocaInst);
        return allocaInst;
    }

    public void buildInitArray(Value array, int index, Value value) {
        ((ConstArray) ((GlobalVar) array).getValue()).storeValue(index, value);
    }

    public void buildInitArray(Value array, Value value) {
        ((ConstArray) ((GlobalVar) array).getValue()).storeValue(value);
    }

    public ArrayType getArrayType(Type elementType, int length) {
        return new ArrayType(elementType, length);
    }

    public PointerType getPointerType(Type elementType) {
        return new PointerType(elementType);
    }

    /**
     * ConvInst
     */
    public Value buildZext(BasicBlock basicBlock, Value value) {
        if (value instanceof ConstChar) {
            return new ConstInt(((ConstChar) value).getValue());
        }
        ConvInst convInst = new ConvInst(basicBlock, Operator.Zext, value);
        basicBlock.addInstruction(convInst);
        return convInst;
    }

    public Value buildTrunc(BasicBlock basicBlock, Value value) {
        if (value instanceof ConstInt) {
            return new ConstChar(((ConstInt) value).getValue());
        }
        ConvInst convInst = new ConvInst(basicBlock, Operator.Trunc, value);
        basicBlock.addInstruction(convInst);
        return convInst;
    }

    public BinaryInst buildConvToI1(Value val, BasicBlock basicBlock) {
        BinaryInst binaryInst = new BinaryInst(basicBlock, Operator.Ne, val, getConstInt(0));
        basicBlock.addInstruction(binaryInst);
        return binaryInst;
    }

    /**
     * MemInst
     */
    public StoreInst buildStore(BasicBlock basicBlock, Value pointer, Value value) {
        StoreInst storeInst = new StoreInst(basicBlock, pointer, value);
        basicBlock.addInstruction(storeInst);
        return storeInst;
    }

    public LoadInst buildLoad(BasicBlock basicBlock, Value pointer) {
        LoadInst loadInst = new LoadInst(basicBlock, pointer);
        basicBlock.addInstruction(loadInst);
        return loadInst;
    }

    public GEPInst buildGEP(BasicBlock basicBlock, Value pointer, Value index) {
        GEPInst gepInst = new GEPInst(basicBlock, pointer, index);
        basicBlock.addInstruction(gepInst);
        return gepInst;
    }

    public GEPInst buildGEP(BasicBlock basicBlock, Value pointer, int index) {
        return buildGEP(basicBlock, pointer, getConstInt(index));
    }

    /**
     * TerminatorInst
     */
    public RetInst buildRet(BasicBlock basicBlock) {
        RetInst retInst = new RetInst(basicBlock);
        basicBlock.addInstruction(retInst);
        return retInst;
    }

    public RetInst buildRet(BasicBlock basicBlock, Value ret) {
        RetInst retInst = new RetInst(basicBlock, ret);
        basicBlock.addInstruction(retInst);
        return retInst;
    }

    public BrInst buildBr(BasicBlock basicBlock, BasicBlock trueBlock) {
        BrInst brInst = new BrInst(basicBlock, trueBlock);
        basicBlock.addInstruction(brInst);
        return brInst;
    }

    public BrInst buildBr(BasicBlock basicBlock, BasicBlock trueBlock, BasicBlock falseBlock, Value cond) {
        BrInst brInst = new BrInst(basicBlock, trueBlock, falseBlock, cond);
        basicBlock.addInstruction(brInst);
        return brInst;
    }

}
