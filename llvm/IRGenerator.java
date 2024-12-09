package llvm;

import llvm.types.*;
import llvm.values.*;
import llvm.values.instructions.Operator;
import node.*;
import token.TokenType;

import java.util.ArrayList;
import java.util.List;

public class IRGenerator {
    private static final IRGenerator instance = new IRGenerator();

    private IRGenerator() {
    }

    public static IRGenerator getInstance() {
        return instance;
    }

    private ValueTable root;
    private ValueTable cur;
    private BasicBlock curBlock = null;
    private BuildFactory buildFactory;

    private BasicBlock forEndBlock = null;
    private BasicBlock continueBlock = null;
    private BasicBlock curTrueBlock = null;
    private BasicBlock curFalseBlock = null;

    private void initTable() {
        root = new ValueTable();
        cur = root;
        buildFactory = BuildFactory.getInstance();
    }

    private void pushTable() {
        ValueTable newTable = new ValueTable();
        newTable.setParentTable(cur);
        cur.addChild(newTable);
        cur = newTable;
    }

    private void popTable() {
        cur = cur.getParentTable();
    }

    private void addGlobalValue(String name, Value value) {
        root.addValue(name, value);
    }

    private boolean isHaveGlobalStr(String str) {
        for (Value value : root.getValueTable().values()) {
            if (value instanceof ConstString) {
                if (((ConstString) value).getValue().equals(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void callPutStr(StringBuilder str) {
        List<Value> args = new ArrayList<>();
        String value = str.toString();
        ConstString tmpString;
        if (isHaveGlobalStr(value)) {
            tmpString = new ConstString(value);
        } else {
            tmpString = buildFactory.buildConstString(value);
            root.addValue("0_str_+" + value, tmpString);//数字在前保证不会和标识符冲突
        }
        ArrayType arrayType = new ArrayType(IntegerType.i8, tmpString.getLength());
        Value array = buildFactory.buildGlobalArray(tmpString.getName(), arrayType, true, true);
        buildFactory.buildInitArray(array, tmpString);
        args.add(buildFactory.buildGEP(curBlock, array, 0));

        buildFactory.buildCall(curBlock, (Function) root.getValueDeep("putstr"), args);
    }

    public void CompUnit(CompUnitNode compUnitNode) {
        // CompUnit → {Decl} {FuncDef} MainFuncDef
        initTable();
        List<Type> putintArgs = new ArrayList<>() {{
            add(IntegerType.i32);
        }};
        List<Type> putcharArgs = new ArrayList<>() {{
            add(IntegerType.i8);
        }};
        List<Type> putstrArgs = new ArrayList<>() {{
            add(buildFactory.buildPointerType(IntegerType.i8));
        }};
        Function getint = buildFactory.buildLibraryFunction("getint", IntegerType.i32, new ArrayList<>());
        Function getchar = buildFactory.buildLibraryFunction("getchar", IntegerType.i32, new ArrayList<>());
        Function putint = buildFactory.buildLibraryFunction("putint", VoidType.voidType, putintArgs);
        Function putchar = buildFactory.buildLibraryFunction("putchar", VoidType.voidType, putcharArgs);
        Function putstr = buildFactory.buildLibraryFunction("putstr", VoidType.voidType, putstrArgs);
        cur.addValue("getint", getint);
        cur.addValue("getchar", getchar);
        cur.addValue("putint", putint);
        cur.addValue("putchar", putchar);
        cur.addValue("putstr", putstr);
        for (DeclNode declNode : compUnitNode.getDeclNodes()) {
            Decl(declNode, true);
        }
        for (FuncDefNode funcDefNode : compUnitNode.getFuncDefNodes()) {
            FuncDef(funcDefNode);
        }
        MainFuncDef(compUnitNode.getMainFuncDefNode());
    }

    private void MainFuncDef(MainFuncDefNode mainFuncDefNode) {
        // MainFuncDef → 'int' 'main' '(' ')' Block
        Function function = buildFactory.buildFunction("main", IntegerType.i32, new ArrayList<Type>());
        cur.addValue("main", function);
        pushTable();
        cur.addValue("main", function);
        curBlock = buildFactory.buildBasicBlock(function);
        Block(mainFuncDefNode.getBlock(), function);
        popTable();
    }

    private void FuncDef(FuncDefNode funcDefNode) {
        // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        Type type = FuncType(funcDefNode.getFuncTypeNode());
        String name = funcDefNode.getIdent().getContent();
        List<Type> typeList = new ArrayList<>();
        //curFunction有变化
        Function function = buildFactory.buildFunction(name, type, typeList);
        cur.addValue(name, function);
        pushTable();
        cur.addValue(name, function);
        if (funcDefNode.getFuncFParamsNode() != null) {
            //给tmpTypeList添加元素
            FuncFParams(funcDefNode.getFuncFParamsNode(), typeList, function);
        }
        curBlock = buildFactory.buildBasicBlock(function);
        if (funcDefNode.getFuncFParamsNode() != null) {
            //加入curBlock
            FuncFParams(funcDefNode.getFuncFParamsNode(), null, function);
        }
        Block(funcDefNode.getBlockNode(), function);
        if (((FunctionType) function.getType()).getReturnType() == VoidType.voidType) {
            //在addInstruction中会检查curBlock的末尾语句，这里不需要检查。
            //防止没有return的情况
            buildFactory.buildRet(curBlock);
        }
        popTable();
    }

    private void Block(BlockNode blockNode, Function curFunction) {
        // Block → '{' { BlockItem } '}'
        for (BlockItemNode blockItemNode : blockNode.getBlockItemNodes()) {
            BlockItem(blockItemNode, curFunction);
        }
    }

    private void BlockItem(BlockItemNode blockItemNode, Function curFunction) {
        // BlockItem → Decl | Stmt
        if (blockItemNode.getDeclNode() != null) {
            Decl(blockItemNode.getDeclNode(), false);
        } else {
            Stmt(blockItemNode.getStmtNode(), curFunction);
        }
    }

    private void Stmt(StmtNode stmtNode, Function curFunction) {
        StmtNode.StmtType stmtType = stmtNode.getStmtType();
        Value callValue = null;
        Value addr = null;
        switch (stmtType) {
            case LVal:
                // Stmt → LVal '=' Exp ';'
                Value pointer = LVal(stmtNode.getlValNode(), false).value;
                Value exp = Exp(stmtNode.getExpNode(), false).value;
                buildFactory.buildStore(curBlock, pointer, exp);
                break;
            case Exp:
                // Stmt → [Exp] ';'
                if (stmtNode.getExpNode() != null) {
                    //貌似用不到返回值
                    Exp(stmtNode.getExpNode(), false);
                }
                break;
            case If:
                //Stmt → 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
                if (stmtNode.getElseToken() == null) {
                    BasicBlock basicBlock = curBlock;

                    BasicBlock trueBlock = buildFactory.buildBasicBlock(curFunction);
                    curBlock = trueBlock;
                    Stmt(stmtNode.getStmtNodes().get(0), curFunction);
                    BasicBlock finalBlock = buildFactory.buildBasicBlock(curFunction);
                    buildFactory.buildBr(curBlock, finalBlock);

                    curTrueBlock = trueBlock;
                    curFalseBlock = finalBlock;
                    curBlock = basicBlock;
                    Cond(stmtNode.getCondNode(), curFunction);

                    curBlock = finalBlock;
                } else {
                    //进入if前的基本块
                    BasicBlock basicBlock = curBlock;

                    BasicBlock trueBlock = buildFactory.buildBasicBlock(curFunction);
                    curBlock = trueBlock;
                    Stmt(stmtNode.getStmtNodes().get(0), curFunction);
                    BasicBlock trueEndBlock = curBlock;//不可少

                    BasicBlock falseBlock = buildFactory.buildBasicBlock(curFunction);
                    curBlock = falseBlock;
                    Stmt(stmtNode.getStmtNodes().get(1), curFunction);
                    BasicBlock falseEndBlock = curBlock;//不可少

                    curBlock = basicBlock;
                    curTrueBlock = trueBlock;
                    curFalseBlock = falseBlock;
                    Cond(stmtNode.getCondNode(), curFunction);

                    BasicBlock finalBlock = buildFactory.buildBasicBlock(curFunction);
                    buildFactory.buildBr(trueEndBlock, finalBlock);
                    buildFactory.buildBr(falseEndBlock, finalBlock);
                    curBlock = finalBlock;
                }
                break;
            case For:
                //Stmt → 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
                BasicBlock basicBlock = curBlock;
                if (stmtNode.getForStmtNodeFir() != null) {
                    ForStmt(stmtNode.getForStmtNodeFir());
                }

                BasicBlock condBlock = null;
                if (stmtNode.getCondNode() != null) {
                    condBlock = buildFactory.buildBasicBlock(curFunction);
                }
                BasicBlock forStmtBlock = null;
                if (stmtNode.getForStmtNodeSec() != null) {
                    forStmtBlock = buildFactory.buildBasicBlock(curFunction);
                }
                BasicBlock forBlock = buildFactory.buildBasicBlock(curFunction);
                BasicBlock finalBlock = buildFactory.buildBasicBlock(curFunction);
                buildFactory.buildBr(curBlock, condBlock == null ? forBlock : condBlock);
                if (forStmtBlock != null) {
                    continueBlock = forStmtBlock;
                } else if (condBlock != null) {
                    continueBlock = condBlock;
                } else {
                    continueBlock = forBlock;
                }
                forEndBlock = finalBlock;
                curBlock = forBlock;
                Stmt(stmtNode.getStmtNode(), curFunction);

                //进行到这里，curBlock是for循环体的最后一个语句块
                if (forStmtBlock != null) {
                    buildFactory.buildBr(curBlock, forStmtBlock);
                } else if (condBlock != null) {
                    buildFactory.buildBr(curBlock, condBlock);
                } else {
                    buildFactory.buildBr(curBlock, forBlock);
                }

                if (stmtNode.getForStmtNodeSec() != null) {
                    curBlock = forStmtBlock;
                    ForStmt(stmtNode.getForStmtNodeSec());
                }
                buildFactory.buildBr(curBlock, condBlock == null ? forBlock : condBlock);

                curBlock = condBlock;
                curTrueBlock = forBlock;
                curFalseBlock = finalBlock;
                if (stmtNode.getCondNode() != null) {
                    Cond(stmtNode.getCondNode(), curFunction);
                }
                curBlock = finalBlock;
                break;
            case Break:
                // Stmt → 'break' ';'
                buildFactory.buildBr(curBlock, forEndBlock);
                break;
            case Continue:
                // Stmt → 'continue' ';'
                buildFactory.buildBr(curBlock, continueBlock);
                break;
            case Return:
                // Stmt → 'return' [Exp] ';'
                if (stmtNode.getExpNode() != null) {
                    Value tmpValue = Exp(stmtNode.getExpNode(), false).value;
                    if (tmpValue.getType() != ((FunctionType) curFunction.getType()).getReturnType()) {
                        if (tmpValue.getType() == IntegerType.i32) {
                            //与函数返回类型i8不一样，当前为i32
                            tmpValue = buildFactory.buildTrunc(curBlock, tmpValue);
                        } else {
                            //与函数返回类型i32不一样，当前为i8
                            tmpValue = buildFactory.buildZext(curBlock, tmpValue);
                        }
                    }
                    buildFactory.buildRet(curBlock, tmpValue);
                } else {
                    buildFactory.buildRet(curBlock);
                }
                break;
            case Printf:
                // Stmt → 'printf''('StringConst {','Exp}')'';'
                String stringConst = stmtNode.getStringContent().replace("\\n", "\n");
                List<Value> exps = new ArrayList<>();
                for (ExpNode expNode : stmtNode.getExpNodes()) {
                    exps.add(Exp(expNode, false).value);
                }
                StringBuilder str = new StringBuilder();
                for (int i = 0; i < stringConst.length(); i++) {
                    if (i <= stringConst.length() - 2 && stringConst.charAt(i) == '%' && stringConst.charAt(i + 1) == 'd') {
                        if (!str.isEmpty()) {
                            //如果现在是%d,那么将之前的字符串保存，并且调用putstr
                            callPutStr(str);
                            str.setLength(0);
                        }
                        List<Value> args = new ArrayList<>();
                        args.add(exps.get(0));
                        exps.remove(0);
                        buildFactory.buildCall(curBlock, (Function) root.getValueDeep("putint"), args);
                        i++;
                    } else if (i <= stringConst.length() - 2 && stringConst.charAt(i) == '%' && stringConst.charAt(i + 1) == 'c') {
                        if (!str.isEmpty()) {
                            //如果现在是%c,那么将之前的字符串保存，并且调用putstr
                            callPutStr(str);
                            str.setLength(0);
                        }
                        List<Value> args = new ArrayList<>();
                        args.add(exps.get(0));
                        exps.remove(0);
                        buildFactory.buildCall(curBlock, (Function) root.getValueDeep("putchar"), args);
                        i++;
                    } else {
                        str.append(stringConst.charAt(i));
                    }
                }
                if (!str.isEmpty()) {
                    //将最后一个%d或%c后面的字符串保存，并且调用putstr
                    callPutStr(str);
                    str.setLength(0);
                }
                break;
            case GetChar:
                // Stmt → LVal '=' 'getchar''('')'';'
                addr = LVal(stmtNode.getlValNode(), false).value;
                callValue = buildFactory.buildCall(curBlock, (Function) cur.getValueDeep("getchar"), new ArrayList<>());//i32
                Value truncValue = buildFactory.buildTrunc(curBlock, callValue);//截断成i8
                buildFactory.buildStore(curBlock, addr, callValue);
                break;
            case GetInt:
                // Stmt → LVal '=' 'getint''('')'';'
                addr = LVal(stmtNode.getlValNode(), false).value;
                callValue = buildFactory.buildCall(curBlock, (Function) cur.getValueDeep("getint"), new ArrayList<>());
                buildFactory.buildStore(curBlock, addr, callValue);
                break;
            case Block:
                // Stmt → Block
                pushTable();
                Block(stmtNode.getBlockNode(), curFunction);
                popTable();
                break;
            default:
                System.out.println("StmtType Error");
                break;
        }
    }

    private void ForStmt(ForStmtNode forStmtNode) {
        // ForStmt → LVal '=' Exp
        String name = forStmtNode.getlValNode().getIdent().getContent();
        Value value = cur.getValueDeep(name);
        Value tmpValue = Exp(forStmtNode.getExpNode(), false).value;
        buildFactory.buildStore(curBlock, value, tmpValue);
    }

    private void Cond(CondNode condNode, Function curFunction) {
        // Cond → LOrExp
        LOrExp(condNode.getlOrExpNode(), curFunction);
    }

    private void LOrExp(LOrExpNode lOrExpNode, Function curFunction) {
        // LOrExp → LAndExp ['||' LOrExp]
        BasicBlock trueBlock = curTrueBlock;//整个if为true时跳转到的
        BasicBlock falseBlock = curFalseBlock;//整个if为false时跳转到的
        BasicBlock nextBlock = null;
        BasicBlock landFalseBlock = curFalseBlock;//如果land为false，要跳到的基本快
        if (lOrExpNode.getlOrExpNode() != null) {
            nextBlock = buildFactory.buildBasicBlock(curFunction);
            landFalseBlock = nextBlock;
        }
        curFalseBlock = landFalseBlock;
        LAndExp(lOrExpNode.getlAndExpNode(), curFunction);
        curTrueBlock = trueBlock;
        curFalseBlock = falseBlock;
        if (lOrExpNode.getlOrExpNode() != null) {
            curBlock = nextBlock;
            LOrExp(lOrExpNode.getlOrExpNode(), curFunction);
        }
    }

    private void LAndExp(LAndExpNode lAndExpNode, Function curFunction) {
        //LAndExp → EqExp ['&&' LAndExp]
        BasicBlock trueBlock = curTrueBlock;//整个if为true时跳转到的
        BasicBlock falseBlock = curFalseBlock;//整个if为false时跳转到的
        BasicBlock nextBlock = null;
        BasicBlock eqTrueBlock = curTrueBlock;//如果eq为true，要跳到的基本块
        if (lAndExpNode.getlAndExpNode() != null) {
            nextBlock = buildFactory.buildBasicBlock(curFunction);
            eqTrueBlock = nextBlock;
        }
        curTrueBlock = eqTrueBlock;
        Value tmpValue = EqExp(lAndExpNode.getEqExpNode()).value;
        tmpValue = buildFactory.buildNeZero(curBlock, tmpValue);
        buildFactory.buildBr(curBlock, curTrueBlock, curFalseBlock, tmpValue);
        curFalseBlock = falseBlock;
        curTrueBlock = trueBlock;
        if (lAndExpNode.getlAndExpNode() != null) {
            curBlock = nextBlock;
            LAndExp(lAndExpNode.getlAndExpNode(), curFunction);
        }
    }

    private ReturnValue EqExp(EqExpNode eqExpNode) {
        //EqExp → RelExp [('==' | '!=') EqExp]
        Value leftValue = RelExp(eqExpNode.getRelExpNode()).value;
        EqExpNode curEqExpNode = eqExpNode;
        while (curEqExpNode.getEqExpNode() != null) {
            TokenType op = curEqExpNode.getOpType();
            curEqExpNode = curEqExpNode.getEqExpNode();
            Value rightValue = RelExp(curEqExpNode.getRelExpNode()).value;
            if (op == TokenType.EQL) {
                leftValue = buildFactory.buildBinary(curBlock, Operator.Eq, leftValue, rightValue);
            } else {
                leftValue = buildFactory.buildBinary(curBlock, Operator.Ne, leftValue, rightValue);
            }
        }
        return new ReturnValue(leftValue);
    }

    private ReturnValue RelExp(RelExpNode relExpNode) {
        //RelExp → AddExp [('<' | '>' | '<=' | '>=') RelExp]
        Value leftValue = AddExp(relExpNode.getAddExpNode(), false).value;
        RelExpNode curRelExpNode = relExpNode;
        while (curRelExpNode.getRelExpNode() != null) {
            TokenType op = curRelExpNode.getOpType();
            curRelExpNode = curRelExpNode.getRelExpNode();
            Value rightValue = AddExp(curRelExpNode.getAddExpNode(), false).value;
            if (op == TokenType.GEQ) {
                leftValue = buildFactory.buildBinary(curBlock, Operator.Ge, leftValue, rightValue);
            } else if (op == TokenType.LEQ) {
                leftValue = buildFactory.buildBinary(curBlock, Operator.Le, leftValue, rightValue);
            } else if (op == TokenType.GRE) {
                leftValue = buildFactory.buildBinary(curBlock, Operator.Gt, leftValue, rightValue);
            } else {
                leftValue = buildFactory.buildBinary(curBlock, Operator.Lt, leftValue, rightValue);
            }
        }
        return new ReturnValue(leftValue);
    }

    private void FuncFParams(FuncFParamsNode funcFParamsNode, List<Type> typeList, Function curFunction) {
        // FuncFParams → FuncFParam { ',' FuncFParam }
        int index = 0;
        for (FuncFParamNode funcFParamNode : funcFParamsNode.getFuncFParamNodes()) {
            FuncFParam(funcFParamNode, index, typeList, curFunction);
            index++;
        }
    }

    private void FuncFParam(FuncFParamNode funcFParamNode, int index, List<Type> typeList, Function curFunction) {
        // FuncFParam → BType Ident ['[' ']']
        if (typeList != null) {
            Type type = BType(funcFParamNode.getbTypeNode());
            if (funcFParamNode.isArray() == 1) {
                //数组类型参数，实际上是指针类型
                type = buildFactory.buildPointerType(type);
            }
            typeList.add(type);
            Value tmpValue = buildFactory.buildArgument(type, curFunction, index, false);
            String name = funcFParamNode.getIdent().getContent();
            cur.addValue(name, tmpValue);//先暂时加入表中
        } else {
            Type type = BType(funcFParamNode.getbTypeNode());
            if (funcFParamNode.isArray() == 1) {
                type = buildFactory.buildPointerType(type);
            }
            String name = funcFParamNode.getIdent().getContent();
            Value tmpValue = cur.getValueDeep(name);
            Value value = buildFactory.buildVar(curBlock, tmpValue, false, type);
            cur.addValue(name, value);
        }
    }

    private Type FuncType(FuncTypeNode funcTypeNode) {
        if (funcTypeNode.getToken().getType() == TokenType.INTTK) {
            return IntegerType.i32;
        } else if (funcTypeNode.getToken().getType() == TokenType.CHARTK) {
            return IntegerType.i8;
        } else {
            return VoidType.voidType;
        }
    }

    private void Decl(DeclNode declNode, boolean isGlobal) {
        // Decl → ConstDecl | VarDecl
        ConstDeclNode constDeclNode = declNode.getConstDeclNode();
        if (constDeclNode != null) {
            ConstDecl(constDeclNode, isGlobal);
        } else {
            VarDeclNode varDeclNode = declNode.getVarDeclNode();
            VarDecl(varDeclNode, isGlobal);
        }
    }

    private void VarDecl(VarDeclNode varDeclNode, boolean isGlobal) {
        // VarDecl → BType VarDef { ',' VarDef } ';'
        Type type = BType(varDeclNode.getbTypeNode());
        for (VarDefNode varDefNode : varDeclNode.getVarDefNodes()) {
            VarDef(varDefNode, type, isGlobal, false);
        }
    }

    private void VarDef(VarDefNode varDefNode, Type type, boolean isGlobal, boolean isConst) {
        // VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
        String name = varDefNode.getIdent().getContent();
        if (varDefNode.getConstExpNode() != null) {
            //数组
            int constExp = ConstExp(varDefNode.getConstExpNode()).constValue;
            Type arrayType = buildFactory.buildArrayType(type, constExp);
            Value tmpValue = null;
            if (isGlobal) {
                tmpValue = buildFactory.buildGlobalArray(name, arrayType, false, false);
                if (varDefNode.getInitValNode() != null) {
                    ((ConstArray) ((GlobalVar) tmpValue).getValue()).setInit(true);
                }
            } else {
                tmpValue = buildFactory.buildArray(curBlock, false, arrayType);
            }
            cur.addValue(name, tmpValue);
            Value curArray = tmpValue;
            if (varDefNode.getInitValNode() != null) {
                InitVal(varDefNode.getInitValNode(), type, curArray, isGlobal, isConst);
            }
        } else {
            //非数组
            ReturnValue returnValue = null;
            if (isGlobal) {
                isConst = true;
            }
            if (varDefNode.getInitValNode() != null) {
                returnValue = InitVal(varDefNode.getInitValNode(), type, null, isGlobal, isConst);
            }
            if (isGlobal) {
                Value value;
                if (type == IntegerType.i32) {
                    value = buildFactory.buildConstInt(returnValue == null ? 0 : returnValue.constValue);
                } else {
                    value = buildFactory.buildConstChar(returnValue == null ? 0 : returnValue.constValue);
                }
                Value tmpValue = buildFactory.buildGlobalVar(name, type, value, false, false);
                cur.addValue(name, tmpValue);
            } else {
                Value tmpValue = buildFactory.buildVar(curBlock, returnValue == null ? null : returnValue.value, false, type);
                cur.addValue(name, tmpValue);
            }
        }
    }

    private ReturnValue InitVal(InitValNode initValNode, Type type, Value curArray, boolean isGlobal, boolean isConst) {
        // InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst
        if (initValNode.getExpNode() != null) {
            return Exp(initValNode.getExpNode(), isConst);
        } else if (initValNode.getStringConst() != null) {
            //去除原来保存的双引号，给末尾加上\0
            String str = initValNode.getStringContent() + '\0';
            char[] chars = str.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                Value tmpValue = buildFactory.buildConstChar(chars[i]);
                if (isGlobal) {
                    buildFactory.buildInitArray(curArray, i, tmpValue);
                } else {
                    buildFactory.buildStore(curBlock, buildFactory.buildGEP(curBlock, curArray, i), tmpValue);
                }
            }
            return new ReturnValue(curArray);
        } else {
            if (isGlobal) isConst = true;//如果是全局变量，那么数组元素有初始值或者为0
            int offset = 0;
            for (ExpNode expNode : initValNode.getExpNodes()) {
                ReturnValue returnValue = Exp(expNode, isConst);
                if (isGlobal) {
                    Value tmpValue = null;
                    if (type == IntegerType.i32 || (type instanceof ArrayType && ((ArrayType) type).getElementType() == IntegerType.i32)) {
                        tmpValue = buildFactory.buildConstInt(returnValue.constValue);
                    } else {
                        tmpValue = buildFactory.buildConstChar(returnValue.constValue);
                    }
                    buildFactory.buildInitArray(curArray, offset, tmpValue);
                } else {
                    //计算数组元素地址，保存值
                    buildFactory.buildStore(curBlock, buildFactory.buildGEP(curBlock, curArray, offset), returnValue.value);
                }
                offset++;
            }
            return new ReturnValue(curArray);
        }
    }

    private ReturnValue Exp(ExpNode expNode, boolean isConst) {
        // Exp → AddExp
        return AddExp(expNode.getAddExpNode(), isConst);
    }

    private void ConstDecl(ConstDeclNode constDeclNode, boolean isGlobal) {
        // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        Type type = BType(constDeclNode.getbTypeNode());
        for (ConstDefNode constDefNode : constDeclNode.getConstDefNodes()) {
            ConstDef(constDefNode, type, isGlobal);
        }
    }

    private void ConstDef(ConstDefNode constDefNode, Type type, boolean isGlobal) {
        // ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal
        String name = constDefNode.getIdent().getContent();
        if (constDefNode.getConstExpNode() == null) {
            //非数组
            int constExp = ConstInitVal(constDefNode.getConstInitValNode(), type, null, null, isGlobal).constValue;
            Value tmpValue = null;
            if (type == IntegerType.i32) {
                tmpValue = buildFactory.buildConstInt(constExp);//将常量包装成constInt
            } else {
                tmpValue = buildFactory.buildConstChar(constExp);//将常量包装成constChar
            }
            cur.addConstValue(name, constExp);
            if (isGlobal) {
                //包装成globalVar，加入表
                tmpValue = buildFactory.buildGlobalVar(name, type, tmpValue, true, false);
                cur.addValue(name, tmpValue);
            } else {
                tmpValue = buildFactory.buildVar(curBlock, tmpValue, true, type);
                cur.addValue(name, tmpValue);
            }
        } else {
            //数组
            int constExp = ConstExp(constDefNode.getConstExpNode()).constValue;//给saveValue赋值
            Type tmpType = buildFactory.buildArrayType(type, constExp);
            Value tmpValue = null;
            if (isGlobal) {
                tmpValue = buildFactory.buildGlobalArray(name, tmpType, true, false);
                ((ConstArray) ((GlobalVar) tmpValue).getValue()).setInit(true);
            } else {
                tmpValue = buildFactory.buildArray(curBlock, true, tmpType);
            }
            cur.addValue(name, tmpValue);
            Value curArray = tmpValue;
            ConstInitVal(constDefNode.getConstInitValNode(), type, name, curArray, isGlobal);
        }
    }

    private ReturnValue ConstInitVal(ConstInitValNode constInitValNode, Type type, String name, Value curArray, boolean isGlobal) {
        // ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
        if (constInitValNode.getConstExp() != null) {
            return ConstExp(constInitValNode.getConstExp());
        } else if (constInitValNode.getStringConst() != null) {
            //去除原来保存的双引号
            String str = constInitValNode.getStringConst().getContent().replace("\"", "");
            char[] chars = str.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                Value tmpValue = buildFactory.buildConstChar(chars[i]);
                if (isGlobal) {
                    buildFactory.buildInitArray(curArray, i, tmpValue);
                } else {
                    buildFactory.buildStore(curBlock, buildFactory.buildGEP(curBlock, curArray, i), tmpValue);
                }
                StringBuilder tmpName = new StringBuilder(name);
                tmpName.append(";").append(i);//比如arr[1]就是arr;1
                cur.addConstValue(tmpName.toString(), (int) chars[i]);
            }
            return new ReturnValue(curArray);//其实好像不需要return啥
        } else {
            int tmpOffset = 0;
            for (ConstExpNode constExpNode : constInitValNode.getConstExpNodes()) {
                int exp = ConstExp(constExpNode).constValue;//返回值在saveValue中
                Value tmpValue = type == IntegerType.i32 ? buildFactory.buildConstInt(exp) : buildFactory.buildConstChar(exp);
                if (isGlobal) {
                    //如果是全局数组，直接保存值
                    buildFactory.buildInitArray(curArray, tmpOffset, tmpValue);
                } else {
                    //计算数组元素地址，保存值
                    buildFactory.buildStore(curBlock, buildFactory.buildGEP(curBlock, curArray, tmpOffset), tmpValue);
                }
                StringBuilder tmpName = new StringBuilder(name);
                tmpName.append(";").append(tmpOffset);//比如arr[1]就是arr;1
                cur.addConstValue(tmpName.toString(), exp);
                tmpOffset++;
            }
            return new ReturnValue(curArray);//其实好像不需要return啥
        }
    }

    private ReturnValue ConstExp(ConstExpNode constExp) {
        //ConstExp → AddExp
        return AddExp(constExp.getAddExpNode(), true);
    }

    private ReturnValue AddExp(AddExpNode addExpNode, boolean isConst) {
        //AddExp -> MulExp [('+' | '-') AddExp]
        if (isConst) {
            int result = MulExp(addExpNode.getMulExpNode(), true).constValue;
            AddExpNode curAddExpNode = addExpNode;
            while (curAddExpNode.getAddExpNode() != null) {
                TokenType op = curAddExpNode.getOpType();
                curAddExpNode = curAddExpNode.getAddExpNode();
                int another = MulExp(curAddExpNode.getMulExpNode(), true).constValue;
                if (op == TokenType.PLUS) {
                    result = result + another;
                } else {
                    result = result - another;
                }
            }
            return new ReturnValue(result);
        } else {
            Value leftValue = MulExp(addExpNode.getMulExpNode(), false).value;
            AddExpNode curAddExpNode = addExpNode;
            while (curAddExpNode.getAddExpNode() != null) {
                TokenType op = curAddExpNode.getOpType();
                curAddExpNode = curAddExpNode.getAddExpNode();
                Value rightValue = MulExp(curAddExpNode.getMulExpNode(), false).value;
                if (op == TokenType.PLUS) {
                    leftValue = buildFactory.buildBinary(curBlock, Operator.Add, leftValue, rightValue);
                } else {
                    leftValue = buildFactory.buildBinary(curBlock, Operator.Sub, leftValue, rightValue);
                }
            }
            return new ReturnValue(leftValue);
        }
    }

    private ReturnValue MulExp(MulExpNode mulExpNode, boolean isConst) {
        //MulExp → UnaryExp [('*' | '/' | '%') MulExp]
        if (isConst) {
            int result = UnaryExp(mulExpNode.getUnaryExpNode(), true).constValue;
            MulExpNode curMulExpNode = mulExpNode;
            while (curMulExpNode.getMulExpNode() != null) {
                TokenType op = curMulExpNode.getOpType();
                curMulExpNode = curMulExpNode.getMulExpNode();
                int another = UnaryExp(curMulExpNode.getUnaryExpNode(), true).constValue;
                if (op == TokenType.MULT) {
                    result = result * another;
                } else if (op == TokenType.DIV) {
                    result = result / another;
                } else {
                    result = result % another;
                }
            }
            return new ReturnValue(result);
        } else {
            Value leftValue = UnaryExp(mulExpNode.getUnaryExpNode(), false).value;
            MulExpNode curMulExpNode = mulExpNode;
            while (curMulExpNode.getMulExpNode() != null) {
                TokenType op = curMulExpNode.getOpType();
                curMulExpNode = curMulExpNode.getMulExpNode();
                Value rightValue = UnaryExp(curMulExpNode.getUnaryExpNode(), false).value;
                if (op == TokenType.MULT) {
                    leftValue = buildFactory.buildBinary(curBlock, Operator.Mul, leftValue, rightValue);
                } else if (op == TokenType.DIV) {
                    leftValue = buildFactory.buildBinary(curBlock, Operator.Div, leftValue, rightValue);
                } else {
                    leftValue = buildFactory.buildBinary(curBlock, Operator.Mod, leftValue, rightValue);
                }
            }
            return new ReturnValue(leftValue);
        }
    }

    private ReturnValue UnaryExp(UnaryExpNode unaryExpNode, boolean isConst) {
        // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        if (unaryExpNode.getPrimaryExpNode() != null) {
            return PrimaryExp(unaryExpNode.getPrimaryExpNode(), isConst);
        } else if (unaryExpNode.getIdent() != null) {
            String name = unaryExpNode.getIdent().getContent();
            Function function = (Function) cur.getValueDeep(name);
            List<Value> argList = new ArrayList<>();
            if (unaryExpNode.getFuncRParamsNode() != null) {
                FuncRParams(unaryExpNode.getFuncRParamsNode(), argList);
            }
            Value tmpValue = buildFactory.buildCall(curBlock, function, argList);
            return new ReturnValue(tmpValue);
        } else {
            TokenType op = unaryExpNode.getUnaryOpType();
            if (op == TokenType.PLUS) {
                return UnaryExp(unaryExpNode.getUnaryExpNode(), isConst);
            } else if (op == TokenType.MINU) {
                ReturnValue returnValue = UnaryExp(unaryExpNode.getUnaryExpNode(), isConst);
                if (isConst) {
                    returnValue.constValue = -returnValue.constValue;
                } else {
                    returnValue.value = buildFactory.buildBinary(curBlock, Operator.Sub, ConstInt.ZERO, returnValue.value);
                }
                return returnValue;
            } else {
                ReturnValue returnValue = UnaryExp(unaryExpNode.getUnaryExpNode(), isConst);
                returnValue.value = buildFactory.buildNot(curBlock, returnValue.value);
                return returnValue;
            }
        }
    }

    private void FuncRParams(FuncRParamsNode funcRParamsNode, List<Value> argList) {
        // FuncRParams → Exp { ',' Exp }
        for (ExpNode expNode : funcRParamsNode.getExpNodes()) {
            ReturnValue returnValue = Exp(expNode, false);
            argList.add(returnValue.value);
        }
    }

    private ReturnValue PrimaryExp(PrimaryExpNode primaryExpNode, boolean isConst) {
        // PrimaryExp → '(' Exp ')' | LVal | Number | Character
        if (primaryExpNode.getExpNode() != null) {
            return Exp(primaryExpNode.getExpNode(), isConst);
        } else if (primaryExpNode.getlValNode() != null) {
            ReturnValue returnValue = LVal(primaryExpNode.getlValNode(), isConst);
            if (returnValue.needLoad) {
                returnValue.value = buildFactory.buildLoad(curBlock, returnValue.value);
            }
            return returnValue;
        } else if (primaryExpNode.getNumberNode() != null) {
            return Number(primaryExpNode.getNumberNode(), isConst);
        } else {
            return Character(primaryExpNode.getCharacterNode(), isConst);
        }
    }

    private ReturnValue LVal(LValNode lValNode, boolean isConst) {
        // LVal → Ident ['[' Exp ']']
        //除了常量统一返回地址
        if (isConst) {
            StringBuilder name = new StringBuilder(lValNode.getIdent().getContent());
            if (lValNode.getExpNode() != null) {
                int exp = Exp(lValNode.getExpNode(), true).constValue;
                name.append(";").append(exp);
            }
            ReturnValue returnValue = new ReturnValue(cur.getConstDeep(name.toString()));
            returnValue.needLoad = false;
            return returnValue;
        } else {
            String name = lValNode.getIdent().getContent();
            Value addr = cur.getValueDeep(name);
            if (lValNode.getExpNode() != null) {
                ReturnValue returnValue = new ReturnValue(Exp(lValNode.getExpNode(), false).value);
                if (addr.getType() instanceof PointerType) {
                    Type type = ((PointerType) addr.getType()).getTargetType();
                    if (type instanceof ArrayType) {
                        //对于类型是[i8 x 8]*这种的
                        returnValue.value = buildFactory.buildGEP(curBlock, addr, returnValue.value);
                    } else {
                        //对于长度不知道的数组，类型是i8**或i32**这样的
                        Value firstAddr = buildFactory.buildLoad(curBlock, addr);
                        returnValue.value = buildFactory.buildGEP(curBlock, firstAddr, returnValue.value);
                    }
                }
                returnValue.needLoad = true;
                return returnValue;
            } else {
                Type type = ((PointerType) addr.getType()).getTargetType();
                Value tmpValue = null;
                boolean needLoad = true;
                if (type instanceof ArrayType) {
                    tmpValue = buildFactory.buildGEP(curBlock, addr, 0);
                    needLoad = false;
                } else {
                    tmpValue = addr;
                }
                ReturnValue returnValue = new ReturnValue(tmpValue);
                returnValue.needLoad = needLoad;
                return returnValue;
            }
        }
    }

    private ReturnValue Number(NumberNode numberNode, boolean isConst) {
        if (isConst) {
            return new ReturnValue(numberNode.getNumber());
        } else {
            return new ReturnValue(buildFactory.buildConstInt(numberNode.getNumber()));
        }
    }

    private ReturnValue Character(CharacterNode characterNode, boolean isConst) {
        if (isConst) {
            return new ReturnValue(characterNode.getChar() - '\0');
        } else {
            return new ReturnValue(buildFactory.buildConstChar(characterNode.getChar()));
        }
    }

    private Type BType(BTypeNode bTypeNode) {
        if (bTypeNode.getToken().getType() == TokenType.INTTK) {
            return IntegerType.i32;
        } else {
            return IntegerType.i8;
        }
    }
}
