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

    private Integer saveValue = null;
    private Value tmpValue = null;
    private BasicBlock forEndBlock = null;
    private BasicBlock continueBlock = null;
    private BasicBlock curTrueBlock = null;
    private BasicBlock curFalseBlock = null;
    private boolean needLoad = true;

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
            tmpString = buildFactory.getConstString(value);
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
            add(buildFactory.getPointerType(IntegerType.i8));
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
        Block(mainFuncDefNode.getBlock(),function);
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
            FuncFParams(funcDefNode.getFuncFParamsNode(), typeList,function);
        }
        curBlock = buildFactory.buildBasicBlock(function);
        if (funcDefNode.getFuncFParamsNode() != null) {
            //加入curBlock
            FuncFParams(funcDefNode.getFuncFParamsNode(), null,function);
        }
        Block(funcDefNode.getBlockNode(),function);
        if (((FunctionType) function.getType()).getReturnType() == VoidType.voidType) {
            //在addInstruction中会检查curBlock的末尾语句，这里不需要检查。
            //防止没有return的情况
            buildFactory.buildRet(curBlock);
        }
        popTable();
    }

    private void Block(BlockNode blockNode,Function curFunction) {
        // Block → '{' { BlockItem } '}'
        for (BlockItemNode blockItemNode : blockNode.getBlockItemNodes()) {
            BlockItem(blockItemNode,curFunction);
        }
    }

    private void BlockItem(BlockItemNode blockItemNode,Function curFunction) {
        // BlockItem → Decl | Stmt
        if (blockItemNode.getDeclNode() != null) {
            Decl(blockItemNode.getDeclNode(), false);
        } else {
            Stmt(blockItemNode.getStmtNode(),curFunction);
        }
    }

    private void Stmt(StmtNode stmtNode,Function curFunction) {
        StmtNode.StmtType stmtType = stmtNode.getStmtType();
        Value returnValue = null;
        Value addr = null;
        switch (stmtType) {
            case LVal:
                // Stmt → LVal '=' Exp ';'
                LVal(stmtNode.getlValNode(), false);
                Value pointer = tmpValue;
                Exp(stmtNode.getExpNode(), false);
                buildFactory.buildStore(curBlock, pointer, tmpValue);
                break;
            case Exp:
                // Stmt → [Exp] ';'
                if (stmtNode.getExpNode() != null) {
                    tmpValue = null;
                    Exp(stmtNode.getExpNode(), false);
                }
                break;
            case If:
                //Stmt → 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
                if (stmtNode.getElseToken() == null) {
                    BasicBlock basicBlock = curBlock;

                    BasicBlock trueBlock = buildFactory.buildBasicBlock(curFunction);
                    curBlock = trueBlock;
                    Stmt(stmtNode.getStmtNodes().get(0),curFunction);
                    BasicBlock finalBlock = buildFactory.buildBasicBlock(curFunction);
                    buildFactory.buildBr(curBlock, finalBlock);

                    curTrueBlock = trueBlock;
                    curFalseBlock = finalBlock;
                    curBlock = basicBlock;
                    Cond(stmtNode.getCondNode(),curFunction);

                    curBlock = finalBlock;
                } else {
                    //进入if前的基本块
                    BasicBlock basicBlock = curBlock;

                    BasicBlock trueBlock = buildFactory.buildBasicBlock(curFunction);
                    curBlock = trueBlock;
                    Stmt(stmtNode.getStmtNodes().get(0),curFunction);
                    BasicBlock trueEndBlock = curBlock;//不可少

                    BasicBlock falseBlock = buildFactory.buildBasicBlock(curFunction);
                    curBlock = falseBlock;
                    Stmt(stmtNode.getStmtNodes().get(1),curFunction);
                    BasicBlock falseEndBlock = curBlock;//不可少

                    curBlock = basicBlock;
                    curTrueBlock = trueBlock;
                    curFalseBlock = falseBlock;
                    Cond(stmtNode.getCondNode(),curFunction);

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
                Stmt(stmtNode.getStmtNode(),curFunction);

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
                    Cond(stmtNode.getCondNode(),curFunction);
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
                    tmpValue = null;
                    Exp(stmtNode.getExpNode(), false);
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
                    Exp(expNode, false);
                    exps.add(tmpValue);
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
                LVal(stmtNode.getlValNode(), false);
                addr = tmpValue;
                returnValue = buildFactory.buildCall(curBlock, (Function) cur.getValueDeep("getchar"), new ArrayList<>());//i32
                Value truncValue = buildFactory.buildTrunc(curBlock, returnValue);//截断成i8
                buildFactory.buildStore(curBlock, addr, returnValue);
                break;
            case GetInt:
                // Stmt → LVal '=' 'getint''('')'';'
                LVal(stmtNode.getlValNode(), false);
                addr = tmpValue;
                returnValue = buildFactory.buildCall(curBlock, (Function) cur.getValueDeep("getint"), new ArrayList<>());
                tmpValue = buildFactory.buildStore(curBlock, addr, returnValue);
                break;
            case Block:
                // Stmt → Block
                pushTable();
                Block(stmtNode.getBlockNode(),curFunction);
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
        tmpValue = null;
        Exp(forStmtNode.getExpNode(), false);
        buildFactory.buildStore(curBlock, value, tmpValue);
    }

    private void Cond(CondNode condNode,Function curFunction) {
        // Cond → LOrExp
        LOrExp(condNode.getlOrExpNode(),curFunction);
    }

    private void LOrExp(LOrExpNode lOrExpNode,Function curFunction) {
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
        LAndExp(lOrExpNode.getlAndExpNode(),curFunction);
        curTrueBlock = trueBlock;
        curFalseBlock = falseBlock;
        if (lOrExpNode.getlOrExpNode() != null) {
            curBlock = nextBlock;
            LOrExp(lOrExpNode.getlOrExpNode(),curFunction);
        }
    }

    private void LAndExp(LAndExpNode lAndExpNode,Function curFunction) {
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
        EqExp(lAndExpNode.getEqExpNode());
        tmpValue = buildFactory.buildNeZero(curBlock, tmpValue);
        buildFactory.buildBr(curBlock, curTrueBlock, curFalseBlock, tmpValue);
        curFalseBlock = falseBlock;
        curTrueBlock = trueBlock;
        if (lAndExpNode.getlAndExpNode() != null) {
            curBlock = nextBlock;
            LAndExp(lAndExpNode.getlAndExpNode(),curFunction);
        }
    }

    private void EqExp(EqExpNode eqExpNode) {
        //EqExp → RelExp [('==' | '!=') EqExp]
        tmpValue = null;
        RelExp(eqExpNode.getRelExpNode());
        Value leftValue = tmpValue;
        EqExpNode curEqExpNode = eqExpNode;
        while (curEqExpNode.getEqExpNode() != null) {
            TokenType op = curEqExpNode.getOpType();
            curEqExpNode = curEqExpNode.getEqExpNode();
            tmpValue = null;
            RelExp(curEqExpNode.getRelExpNode());
            Value rightValue = tmpValue;
            if (op == TokenType.EQL) {
                leftValue = buildFactory.buildBinary(curBlock, Operator.Eq, leftValue, rightValue);
            } else {
                leftValue = buildFactory.buildBinary(curBlock, Operator.Ne, leftValue, rightValue);
            }
        }
        tmpValue = leftValue;
    }

    private void RelExp(RelExpNode relExpNode) {
        //RelExp → AddExp [('<' | '>' | '<=' | '>=') RelExp]
        tmpValue = null;
        AddExp(relExpNode.getAddExpNode(), false);
        Value leftValue = tmpValue;
        RelExpNode curRelExpNode = relExpNode;
        while (curRelExpNode.getRelExpNode() != null) {
            TokenType op = curRelExpNode.getOpType();
            curRelExpNode = curRelExpNode.getRelExpNode();
            tmpValue = null;
            AddExp(curRelExpNode.getAddExpNode(), false);
            Value rightValue = tmpValue;
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
        tmpValue = leftValue;
    }

    private void FuncFParams(FuncFParamsNode funcFParamsNode, List<Type> typeList,Function curFunction) {
        // FuncFParams → FuncFParam { ',' FuncFParam }
        int index = 0;
        for (FuncFParamNode funcFParamNode : funcFParamsNode.getFuncFParamNodes()) {
            FuncFParam(funcFParamNode, index, typeList,curFunction);
            index++;
        }
    }

    private void FuncFParam(FuncFParamNode funcFParamNode, int index, List<Type> typeList,Function curFunction) {
        // FuncFParam → BType Ident ['[' ']']
        if (typeList != null) {
            Type type = BType(funcFParamNode.getbTypeNode());
            if (funcFParamNode.isArray() == 1) {
                //数组类型参数，实际上是指针类型
                type = buildFactory.getPointerType(type);
            }
            typeList.add(type);
            tmpValue = buildFactory.buildArgument(type, curFunction, index, false);
            String name = funcFParamNode.getIdent().getContent();
            cur.addValue(name, tmpValue);//先暂时加入表中
        } else {
            Type type = BType(funcFParamNode.getbTypeNode());
            if (funcFParamNode.isArray() == 1) {
                type = buildFactory.getPointerType(type);
            }
            String name = funcFParamNode.getIdent().getContent();
            tmpValue = cur.getValueDeep(name);
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
            ConstExp(varDefNode.getConstExpNode());
            Type arrayType = buildFactory.getArrayType(type, saveValue);
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
            tmpValue = null;
            if (isGlobal) {
                saveValue = null;
                isConst = true;
            }
            if (varDefNode.getInitValNode() != null) {
                InitVal(varDefNode.getInitValNode(), type, null, isGlobal, isConst);
            }
            if (isGlobal) {
                Value value;
                if (type == IntegerType.i32) {
                    value = buildFactory.getConstInt(saveValue == null ? 0 : saveValue);
                } else {
                    value = buildFactory.getConstChar(saveValue == null ? 0 : saveValue);
                }
                tmpValue = buildFactory.buildGlobalVar(name, type, value, false, false);
                cur.addValue(name, tmpValue);
            } else {
                tmpValue = buildFactory.buildVar(curBlock, tmpValue, false, type);
                cur.addValue(name, tmpValue);
            }
        }
    }

    private void InitVal(InitValNode initValNode, Type type, Value curArray, boolean isGlobal, boolean isConst) {
        // InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst
        if (initValNode.getExpNode() != null) {
            Exp(initValNode.getExpNode(), isConst);
        } else if (initValNode.getStringConst() != null) {
            //去除原来保存的双引号，给末尾加上\0
            String str = initValNode.getStringContent() + "\0";
            char[] chars = str.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                tmpValue = buildFactory.getConstChar(chars[i]);
                if (isGlobal) {
                    buildFactory.buildInitArray(curArray, i, tmpValue);
                } else {
                    buildFactory.buildStore(curBlock, buildFactory.buildGEP(curBlock, curArray, i), tmpValue);
                }
            }
        } else {
            if (isGlobal) isConst = true;//如果是全局变量，那么数组元素有初始值或者为0
            int offset = 0;
            for (ExpNode expNode : initValNode.getExpNodes()) {
                tmpValue = null;
                saveValue = null;
                Exp(expNode, isConst);
                if (isGlobal) {
                    if (type == IntegerType.i32 || (type instanceof ArrayType && ((ArrayType) type).getElementType() == IntegerType.i32)) {
                        tmpValue = buildFactory.getConstInt(saveValue);
                    } else {
                        tmpValue = buildFactory.getConstChar(saveValue);
                    }
                    buildFactory.buildInitArray(curArray, offset, tmpValue);
                } else {
                    //计算数组元素地址，保存值
                    buildFactory.buildStore(curBlock, buildFactory.buildGEP(curBlock, curArray, offset), tmpValue);
                }
                offset++;
            }
        }
    }

    private void Exp(ExpNode expNode, boolean isConst) {
        // Exp → AddExp
        AddExp(expNode.getAddExpNode(), isConst);
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
            ConstInitVal(constDefNode.getConstInitValNode(), type, null, null, isGlobal);//将saveValue赋值
            if (type == IntegerType.i32) {
                tmpValue = buildFactory.getConstInt(saveValue == null ? 0 : saveValue);//将常量包装成constInt
            } else {
                tmpValue = buildFactory.getConstChar(saveValue == null ? '\0' : saveValue);//将常量包装成constChar
            }
            cur.addConstValue(name, saveValue);
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
            ConstExp(constDefNode.getConstExpNode());//给saveValue赋值
            Type tmpType = buildFactory.getArrayType(type, saveValue);
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

    private void ConstInitVal(ConstInitValNode constInitValNode, Type type, String name, Value curArray, boolean isGlobal) {
        // ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
        if (constInitValNode.getConstExp() != null) {
            ConstExp(constInitValNode.getConstExp());
        } else if (constInitValNode.getStringConst() != null) {
            //去除原来保存的双引号
            String str = constInitValNode.getStringConst().getContent().replace("\"", "");
            char[] chars = str.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                tmpValue = buildFactory.getConstChar(chars[i]);
                if (isGlobal) {
                    buildFactory.buildInitArray(curArray, i, tmpValue);
                } else {
                    buildFactory.buildStore(curBlock, buildFactory.buildGEP(curBlock, curArray, i), tmpValue);
                }
                StringBuilder tmpName = new StringBuilder(name);
                tmpName.append(";").append(i);//比如arr[1]就是arr;1
                cur.addConstValue(tmpName.toString(), saveValue);
            }
        } else {
            int tmpOffset = 0;
            for (ConstExpNode constExpNode : constInitValNode.getConstExpNodes()) {
                tmpValue = null;
                ConstExp(constExpNode);//返回值在saveValue中
                tmpValue = type == IntegerType.i32 ? buildFactory.getConstInt(saveValue) : buildFactory.getConstChar(saveValue);
                if (isGlobal) {
                    //如果是全局数组，直接保存值
                    buildFactory.buildInitArray(curArray, tmpOffset, tmpValue);
                } else {
                    //计算数组元素地址，保存值
                    buildFactory.buildStore(curBlock, buildFactory.buildGEP(curBlock, curArray, tmpOffset), tmpValue);
                }
                StringBuilder tmpName = new StringBuilder(name);
                tmpName.append(";").append(tmpOffset);//比如arr[1]就是arr;1
                cur.addConstValue(tmpName.toString(), saveValue);
                tmpOffset++;
            }
        }
    }

    private void ConstExp(ConstExpNode constExp) {
        //ConstExp → AddExp
        saveValue = null;
        AddExp(constExp.getAddExpNode(), true);
    }

    private void AddExp(AddExpNode addExpNode, boolean isConst) {
        //AddExp -> MulExp [('+' | '-') AddExp]
        if (isConst) {
            saveValue = null;
            MulExp(addExpNode.getMulExpNode(), true);
            Integer result = saveValue;
            AddExpNode curAddExpNode = addExpNode;
            while (curAddExpNode.getAddExpNode() != null) {
                TokenType op = curAddExpNode.getOpType();
                curAddExpNode = curAddExpNode.getAddExpNode();
                saveValue = null;
                MulExp(curAddExpNode.getMulExpNode(), true);
                if (op == TokenType.PLUS) {
                    result = result + saveValue;
                } else {
                    result = result - saveValue;
                }
            }
            saveValue = result;
        } else {
            tmpValue = null;
            MulExp(addExpNode.getMulExpNode(), false);
            Value leftValue = tmpValue;
            AddExpNode curAddExpNode = addExpNode;
            while (curAddExpNode.getAddExpNode() != null) {
                TokenType op = curAddExpNode.getOpType();
                curAddExpNode = curAddExpNode.getAddExpNode();
                tmpValue = null;
                MulExp(curAddExpNode.getMulExpNode(), false);
                Value rightValue = tmpValue;
                if (op == TokenType.PLUS) {
                    leftValue = buildFactory.buildBinary(curBlock, Operator.Add, leftValue, rightValue);
                } else {
                    leftValue = buildFactory.buildBinary(curBlock, Operator.Sub, leftValue, rightValue);
                }
            }
            tmpValue = leftValue;
        }
    }

    private void MulExp(MulExpNode mulExpNode, boolean isConst) {
        //MulExp → UnaryExp [('*' | '/' | '%') MulExp]
        if (isConst) {
            saveValue = null;
            UnaryExp(mulExpNode.getUnaryExpNode(), true);
            Integer result = saveValue;
            MulExpNode curMulExpNode = mulExpNode;
            while (curMulExpNode.getMulExpNode() != null) {
                TokenType op = curMulExpNode.getOpType();
                curMulExpNode = curMulExpNode.getMulExpNode();
                saveValue = null;
                UnaryExp(curMulExpNode.getUnaryExpNode(), true);
                if (op == TokenType.MULT) {
                    result = result * saveValue;
                } else if (op == TokenType.DIV) {
                    result = result / saveValue;
                } else {
                    result = result % saveValue;
                }
            }
            saveValue = result;
        } else {
            tmpValue = null;
            UnaryExp(mulExpNode.getUnaryExpNode(), false);
            Value leftValue = tmpValue;
            MulExpNode curMulExpNode = mulExpNode;
            while (curMulExpNode.getMulExpNode() != null) {
                TokenType op = curMulExpNode.getOpType();
                curMulExpNode = curMulExpNode.getMulExpNode();
                tmpValue = null;
                UnaryExp(curMulExpNode.getUnaryExpNode(), false);
                Value rightValue = tmpValue;
                if (op == TokenType.MULT) {
                    leftValue = buildFactory.buildBinary(curBlock, Operator.Mul, leftValue, rightValue);
                } else if (op == TokenType.DIV) {
                    leftValue = buildFactory.buildBinary(curBlock, Operator.Div, leftValue, rightValue);
                } else {
                    leftValue = buildFactory.buildBinary(curBlock, Operator.Mod, leftValue, rightValue);
                }
            }
            tmpValue = leftValue;
        }
    }

    private void UnaryExp(UnaryExpNode unaryExpNode, boolean isConst) {
        // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        if (unaryExpNode.getPrimaryExpNode() != null) {
            PrimaryExp(unaryExpNode.getPrimaryExpNode(), isConst);
        } else if (unaryExpNode.getIdent() != null) {
            String name = unaryExpNode.getIdent().getContent();
            Function function = (Function) cur.getValueDeep(name);
            List<Value> argList = new ArrayList<>();
            if (unaryExpNode.getFuncRParamsNode() != null) {
                FuncRParams(unaryExpNode.getFuncRParamsNode(), argList);
            }
            tmpValue = buildFactory.buildCall(curBlock, function, argList);
        } else {
            TokenType op = unaryExpNode.getUnaryOpType();
            if (op == TokenType.PLUS) {
                UnaryExp(unaryExpNode.getUnaryExpNode(), isConst);
            } else if (op == TokenType.MINU) {
                UnaryExp(unaryExpNode.getUnaryExpNode(), isConst);
                if (isConst) {
                    saveValue = -saveValue;
                } else {
                    tmpValue = buildFactory.buildBinary(curBlock, Operator.Sub, ConstInt.ZERO, tmpValue);
                }
            } else {
                UnaryExp(unaryExpNode.getUnaryExpNode(), isConst);
                tmpValue = buildFactory.buildNot(curBlock, tmpValue);
            }
        }
    }

    private void FuncRParams(FuncRParamsNode funcRParamsNode, List<Value> argList) {
        // FuncRParams → Exp { ',' Exp }
        for (ExpNode expNode : funcRParamsNode.getExpNodes()) {
            tmpValue = null;
            Exp(expNode, false);
            argList.add(tmpValue);
        }
    }

    private void PrimaryExp(PrimaryExpNode primaryExpNode, boolean isConst) {
        // PrimaryExp → '(' Exp ')' | LVal | Number | Character
        if (primaryExpNode.getExpNode() != null) {
            Exp(primaryExpNode.getExpNode(), isConst);
        } else if (primaryExpNode.getlValNode() != null) {
            needLoad = true;
            LVal(primaryExpNode.getlValNode(), isConst);
            if (needLoad) {
                tmpValue = buildFactory.buildLoad(curBlock, tmpValue);
            }
        } else if (primaryExpNode.getNumberNode() != null) {
            Number(primaryExpNode.getNumberNode(), isConst);
        } else {
            Character(primaryExpNode.getCharacterNode(), isConst);
        }
    }

    private void LVal(LValNode lValNode, boolean isConst) {
        // LVal → Ident ['[' Exp ']']
        //除了常量统一返回地址
        if (isConst) {
            StringBuilder name = new StringBuilder(lValNode.getIdent().getContent());
            if (lValNode.getExpNode() != null) {
                saveValue = null;
                Exp(lValNode.getExpNode(), true);
                name.append(";").append(saveValue == null ? 0 : saveValue);
            }
            saveValue = cur.getConstDeep(name.toString());
            needLoad = false;
        } else {
            String name = lValNode.getIdent().getContent();
            Value addr = cur.getValueDeep(name);
            if (lValNode.getExpNode() != null) {
                tmpValue = null;
                Exp(lValNode.getExpNode(), false);
                if (addr.getType() instanceof PointerType) {
                    Type type = ((PointerType) addr.getType()).getTargetType();
                    if (type instanceof ArrayType) {
                        //对于类型是[i8 x 8]*这种的
                        tmpValue = buildFactory.buildGEP(curBlock, addr, tmpValue);
                    } else {
                        //对于长度不知道的数组，类型是i8**或i32**这样的
                        Value firstAddr = buildFactory.buildLoad(curBlock, addr);
                        tmpValue = buildFactory.buildGEP(curBlock, firstAddr, tmpValue);
                    }
                }
            } else {
                Type type = ((PointerType) addr.getType()).getTargetType();
                if (type instanceof ArrayType) {
                    tmpValue = buildFactory.buildGEP(curBlock, addr, 0);
                    needLoad = false;
                } else {
                    tmpValue = addr;
                }
            }
        }
    }

    private void Number(NumberNode numberNode, boolean isConst) {
        if (isConst) {
            saveValue = numberNode.getNumber();
        } else {
            tmpValue = buildFactory.getConstInt(numberNode.getNumber());
        }
    }

    private void Character(CharacterNode characterNode, boolean isConst) {
        if (isConst) {
            saveValue = characterNode.getChar() - '\0';
        } else {
            tmpValue = buildFactory.getConstChar(characterNode.getChar());
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
