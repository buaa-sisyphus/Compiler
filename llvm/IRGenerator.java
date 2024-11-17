package llvm;

import llvm.types.FunctionType;
import llvm.types.IntegerType;
import llvm.types.Type;
import llvm.types.VoidType;
import llvm.values.*;
import llvm.values.instructions.Operator;
import node.*;
import token.TokenType;

import java.util.ArrayList;
import java.util.Collections;
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
    private BuildFactory buildFactory;

    private boolean isGlobal = false;
    private boolean isConst = false;
    private Type tmpType = null;
    private Integer saveValue = null;
    private Value tmpValue = null;
    private BasicBlock curBlock = null;
    private Value curArray = null;
    private String tmpName = null;
    private List<Type> tmpTypeList = null;
    private Function curFunction = null;
    private int tmpIndex;
    private BasicBlock forEndBlock = null;
    private BasicBlock continueBlock = null;
    private boolean isSecond = false;
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
        isGlobal = true;
        for (DeclNode declNode : compUnitNode.getDeclNodes()) {
            Decl(declNode);
        }
        for (FuncDefNode funcDefNode : compUnitNode.getFuncDefNodes()) {
            FuncDef(funcDefNode);
        }
        MainFuncDef(compUnitNode.getMainFuncDefNode());
    }

    private void MainFuncDef(MainFuncDefNode mainFuncDefNode) {
        // MainFuncDef → 'int' 'main' '(' ')' Block
        isGlobal = false;
        Function function = buildFactory.buildFunction("main", IntegerType.i32, new ArrayList<Type>());
        curFunction = function;
        cur.addValue("main", function);
        pushTable();
        cur.addValue("main", function);
        curBlock = buildFactory.buildBasicBlock(function);
        Block(mainFuncDefNode.getBlock());
        isGlobal = true;
        popTable();
    }

    private void FuncDef(FuncDefNode funcDefNode) {
        // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        isGlobal = false;
        FuncType(funcDefNode.getFuncTypeNode());
        String name = funcDefNode.getIdent().getContent();
        tmpTypeList = new ArrayList<>();
        Function function = buildFactory.buildFunction(name, tmpType, tmpTypeList);
        //更新curFunction
        Function oldFunc = curFunction;
        curFunction = function;
        cur.addValue(name, function);
        pushTable();
        cur.addValue(name, function);
        if (funcDefNode.getFuncFParamsNode() != null) {
            //给tmpTypeList添加元素
            isSecond = false;
            FuncFParams(funcDefNode.getFuncFParamsNode());
        }
        curBlock = buildFactory.buildBasicBlock(curFunction);
        if (funcDefNode.getFuncFParamsNode() != null) {
            //加入curBlock
            isSecond = true;
            FuncFParams(funcDefNode.getFuncFParamsNode());
            isSecond = false;
        }
        Block(funcDefNode.getBlockNode());
        popTable();
        //更新curFunction
        curFunction = oldFunc;
        isGlobal = true;
    }

    private void Block(BlockNode blockNode) {
        // Block → '{' { BlockItem } '}'
        for (BlockItemNode blockItemNode : blockNode.getBlockItemNodes()) {
            BlockItem(blockItemNode);
        }
    }

    private void BlockItem(BlockItemNode blockItemNode) {
        // BlockItem → Decl | Stmt
        if (blockItemNode.getDeclNode() != null) {
            Decl(blockItemNode.getDeclNode());
        } else {
            Stmt(blockItemNode.getStmtNode());
        }
    }

    private void Stmt(StmtNode stmtNode) {
        StmtNode.StmtType stmtType = stmtNode.getStmtType();
        switch (stmtType) {
            case LVal:
                // Stmt → LVal '=' Exp ';'
                if (stmtNode.getlValNode().getExpNode() == null) {
                    //非数组
                    LValNode lValNode = stmtNode.getlValNode();
                    String name = lValNode.getIdent().getContent();
                    Value pointer = cur.getValueDeep(name);
                    tmpValue = null;
                    Exp(stmtNode.getExpNode());
                    tmpValue = buildFactory.buildStore(curBlock, pointer, tmpValue);
                } else {
                    //数组
                    LValNode lValNode = stmtNode.getlValNode();
                    String name = lValNode.getIdent().getContent();
                    Value addr = cur.getValueDeep(name);
                    tmpValue = null;
                    Exp(lValNode.getExpNode());
                    addr = buildFactory.buildGEP(curBlock, addr, tmpValue);
                    tmpValue = null;
                    Exp(stmtNode.getExpNode());
                    tmpValue = buildFactory.buildStore(curBlock, addr, tmpValue);
                }
                break;
            case Exp:
                // Stmt → [Exp] ';'
                if (stmtNode.getExpNode() != null) {
                    tmpValue = null;
                    Exp(stmtNode.getExpNode());
                }
                break;
            case If:
                //Stmt → 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
                if (stmtNode.getElseToken() == null) {
                    BasicBlock basicBlock = curBlock;

                    BasicBlock trueBlock = buildFactory.buildBasicBlock(curFunction);
                    curBlock = trueBlock;
                    Stmt(stmtNode.getStmtNodes().get(0));
                    BasicBlock finalBlock = buildFactory.buildBasicBlock(curFunction);
                    buildFactory.buildBr(curBlock, finalBlock);

                    curTrueBlock = trueBlock;
                    curFalseBlock = finalBlock;
                    curBlock = basicBlock;
                    Cond(stmtNode.getCondNode());

                    curBlock = finalBlock;
                } else {

                }
                break;
            case For:
                //Stmt → 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt

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
                    Exp(stmtNode.getExpNode());
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
                    Exp(expNode);
                    exps.add(tmpValue);
                }
                for (int i = 0; i < stringConst.length(); i++) {
                    if (stringConst.charAt(i) == '%' && stringConst.charAt(i + 1) == 'd') {
                        List<Value> args = new ArrayList<>();
                        args.add(exps.get(0));
                        exps.remove(0);
                        buildFactory.buildCall(curBlock, (Function) cur.getValueDeep("putint"), args);
                        i++;
                    } else if (stringConst.charAt(i) == '%' && stringConst.charAt(i + 1) == 'c') {
                        List<Value> args = new ArrayList<>();
                        args.add(exps.get(0));
                        exps.remove(0);
                        buildFactory.buildCall(curBlock, (Function) cur.getValueDeep("putchar"), args);
                        i++;
                    } else {
                        List<Value> args = new ArrayList<>();
                        args.add(buildFactory.getConstChar(stringConst.charAt(i)));
                        buildFactory.buildCall(curBlock, (Function) cur.getValueDeep("putchar"), args);
                    }
                }
                break;
            case GetChar:
                // Stmt → LVal '=' 'getchar''('')'';'
                if (stmtNode.getlValNode().getExpNode() == null) {
                    //非数组元素
                    LValNode lValNode = stmtNode.getlValNode();
                    Value value = cur.getValueDeep(lValNode.getIdent().getContent());//一定是i8类型
                    Value returnValue = buildFactory.buildCall(curBlock, (Function) cur.getValueDeep("getchar"), new ArrayList<>());//i32
                    Value truncValue = buildFactory.buildTrunc(curBlock, returnValue);//截断成i8
                    tmpValue = buildFactory.buildStore(curBlock, value, truncValue);
                } else {
                    //给数组元素赋值
                    LValNode lValNode = stmtNode.getlValNode();
                    Value addr = cur.getValueDeep(lValNode.getIdent().getContent());
                    tmpValue = null;
                    Exp(lValNode.getExpNode());
                    addr = buildFactory.buildGEP(curBlock, addr, tmpValue);//一定是i8*
                    Value returnValue = buildFactory.buildCall(curBlock, (Function) cur.getValueDeep("getchar"), new ArrayList<>());
                    Value truncValue = buildFactory.buildTrunc(curBlock, returnValue);
                    tmpValue = buildFactory.buildStore(curBlock, addr, truncValue);
                }
                break;
            case GetInt:
                // Stmt → LVal '=' 'getint''('')'';'
                if (stmtNode.getlValNode().getExpNode() == null) {
                    //非数组元素
                    LValNode lValNode = stmtNode.getlValNode();
                    Value value = cur.getValueDeep(lValNode.getIdent().getContent());
                    Value returnValue = buildFactory.buildCall(curBlock, (Function) cur.getValueDeep("getint"), new ArrayList<>());
                    tmpValue = buildFactory.buildStore(curBlock, value, returnValue);
                } else {
                    //给数组元素赋值
                    LValNode lValNode = stmtNode.getlValNode();
                    Value addr = cur.getValueDeep(lValNode.getIdent().getContent());
                    tmpValue = null;
                    Exp(lValNode.getExpNode());
                    addr = buildFactory.buildGEP(curBlock, addr, tmpValue);
                    Value returnValue = buildFactory.buildCall(curBlock, (Function) cur.getValueDeep("getint"), new ArrayList<>());
                    tmpValue = buildFactory.buildStore(curBlock, addr, returnValue);
                }
                break;
            case Block:
                // Stmt → Block
                pushTable();
                Block(stmtNode.getBlockNode());
                popTable();
                break;
            default:
                System.out.println("StmtType Error");
                break;
        }
    }

    private void Cond(CondNode condNode) {
        // Cond → LOrExp
        LOrExp(condNode.getlOrExpNode());
    }

    private void LOrExp(LOrExpNode lOrExpNode) {
        //LOrExp → LAndExp ['||' LOrExp]
        BasicBlock trueBlock = curTrueBlock;
        BasicBlock falseBlock = curFalseBlock;
        BasicBlock tmpFalseBlock = curFalseBlock;
        BasicBlock thenBlock = null;
        if (lOrExpNode.getlOrExpNode() != null) {
            thenBlock = buildFactory.buildBasicBlock(curFunction);
            tmpFalseBlock = thenBlock;
        }
        curFalseBlock = tmpFalseBlock;
        LAndExp(lOrExpNode.getlAndExpNode());
        curTrueBlock = trueBlock;
        curFalseBlock = falseBlock;
        if (lOrExpNode.getlOrExpNode() != null) {
            curBlock = thenBlock;
            LOrExp(lOrExpNode.getlOrExpNode());
        }
    }

    private void LAndExp(LAndExpNode lAndExpNode) {

    }

    private void FuncFParams(FuncFParamsNode funcFParamsNode) {
        // FuncFParams → FuncFParam { ',' FuncFParam }
        tmpIndex = 0;
        for (FuncFParamNode funcFParamNode : funcFParamsNode.getFuncFParamNodes()) {
            FuncFParam(funcFParamNode);
            tmpIndex++;
        }
    }

    private void FuncFParam(FuncFParamNode funcFParamNode) {
        // FuncFParam → BType Ident ['[' ']']
        if (!isSecond) {
            tmpType = null;
            BType(funcFParamNode.getbTypeNode());
            if (funcFParamNode.isArray() == 1) {
                //数组类型参数，实际上是指针类型
                tmpType = buildFactory.getPointerType(tmpType);
            }
            tmpTypeList.add(tmpType);
            tmpValue = buildFactory.buildArgument(tmpType, curFunction, tmpIndex, false);
            String name = funcFParamNode.getIdent().getContent();
            cur.addValue(name, tmpValue);//先暂时加入表中
        } else {
            String name = funcFParamNode.getIdent().getContent();
            tmpValue = cur.getValueDeep(name);
            Value value = buildFactory.buildVar(curBlock, tmpValue, false, tmpType);
            cur.addValue(name, value);
        }
    }

    private void FuncType(FuncTypeNode funcTypeNode) {
        if (funcTypeNode.getToken().getType() == TokenType.INTTK) {
            tmpType = IntegerType.i32;
        } else if (funcTypeNode.getToken().getType() == TokenType.CHARTK) {
            tmpType = IntegerType.i8;
        } else {
            tmpType = VoidType.voidType;
        }
    }

    private void Decl(DeclNode declNode) {
        // Decl → ConstDecl | VarDecl
        ConstDeclNode constDeclNode = declNode.getConstDeclNode();
        if (constDeclNode != null) {
            ConstDecl(constDeclNode);
        } else {
            VarDeclNode varDeclNode = declNode.getVarDeclNode();
            VarDecl(varDeclNode);
        }
    }

    private void VarDecl(VarDeclNode varDeclNode) {
        // VarDecl → BType VarDef { ',' VarDef } ';'
        BType(varDeclNode.getbTypeNode());
        for (VarDefNode varDefNode : varDeclNode.getVarDefNodes()) {
            VarDef(varDefNode);
        }
    }

    private void VarDef(VarDefNode varDefNode) {
        // VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
        String name = varDefNode.getIdent().getContent();
        if (varDefNode.getConstExpNode() != null) {
            //数组
            isConst = true;
            ConstExp(varDefNode.getConstExpNode());
            tmpType = buildFactory.getArrayType(tmpType, saveValue);
            isConst = false;
            if (isGlobal) {
                tmpValue = buildFactory.buildGlobalArray(name, tmpType, false);
                if (varDefNode.getInitValNode() != null) {
                    //??
                    ((ConstArray) ((GlobalVar) tmpValue).getValue()).setInit(true);
                }
            } else {
                tmpValue = buildFactory.buildArray(curBlock, false, tmpType);
            }
            cur.addValue(name, tmpValue);
            curArray = tmpValue;
            if (varDefNode.getInitValNode() != null) {
                tmpName = name;
                InitVal(varDefNode.getInitValNode());
            }
        } else {
            if (varDefNode.getInitValNode() != null) {
                tmpValue = null;
                if (isGlobal) {
                    saveValue = null;
                    isConst = true;
                }
                InitVal(varDefNode.getInitValNode());
                isConst = false;
            } else {
                tmpValue = null;
                if (isGlobal) {
                    saveValue = null;
                    isConst = true;
                }
                isConst = false;
            }
            if (isGlobal) {
                Value value;
                if (tmpType == IntegerType.i32) {
                    value = buildFactory.getConstInt(saveValue == null ? 0 : saveValue);
                } else {
                    value = buildFactory.getConstChar(saveValue == null ? 0 : saveValue);
                }
                tmpValue = buildFactory.buildGlobalVar(name, tmpType, false, value);
                cur.addValue(name, tmpValue);
            } else {
                tmpValue = buildFactory.buildVar(curBlock, tmpValue, false, tmpType);
                cur.addValue(name, tmpValue);
            }
        }
    }

    private void InitVal(InitValNode initValNode) {
        // InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst
        if (initValNode.getExpNode() != null) {
            Exp(initValNode.getExpNode());
        } else if (initValNode.getStringConst() != null) {
            //去除原来保存的双引号
            String str = initValNode.getStringConst().getContent().replace("\"", "");
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
            int tmpOffset = 0;
            for (ExpNode expNode : initValNode.getExpNodes()) {
                tmpValue = null;
                saveValue = null;
                Exp(expNode);
                if (isGlobal) {
                    tmpValue = buildFactory.getConstInt(saveValue);
                    buildFactory.buildInitArray(curArray, tmpOffset, tmpValue);
                } else {
                    //计算数组元素地址，保存值
                    buildFactory.buildStore(curBlock, buildFactory.buildGEP(curBlock, curArray, tmpOffset), tmpValue);
                }
                tmpOffset++;
            }
            isConst = false;
        }
    }

    private void Exp(ExpNode expNode) {
        // Exp → AddExp
        AddExp(expNode.getAddExpNode());
    }

    private void ConstDecl(ConstDeclNode constDeclNode) {
        // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        BType(constDeclNode.getbTypeNode());
        for (ConstDefNode constDefNode : constDeclNode.getConstDefNodes()) {
            ConstDef(constDefNode);
        }
    }

    private void ConstDef(ConstDefNode constDefNode) {
        // ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal
        String name = constDefNode.getIdent().getContent();
        if (constDefNode.getConstExpNode() == null) {
            //非数组
            ConstInitVal(constDefNode.getConstInitValNode());//将saveValue赋值
            if (tmpType == IntegerType.i32) {
                tmpValue = buildFactory.getConstInt(saveValue == null ? 0 : saveValue);//将常量包装成constInt
            } else {
                tmpValue = buildFactory.getConstChar(saveValue == null ? '\0' : saveValue);//将常量包装成constInt
            }
            cur.addConstValue(name, saveValue);
            if (isGlobal) {
                //包装成globalVar，加入表
                tmpValue = buildFactory.buildGlobalVar(name, tmpType, true, tmpValue);
                cur.addValue(name, tmpValue);
            } else {
                tmpValue = buildFactory.buildVar(curBlock, tmpValue, true, tmpType);
                cur.addValue(name, tmpValue);
            }
        } else {
            //数组
            ConstExp(constDefNode.getConstExpNode());//给saveValue赋值
            Type type = buildFactory.getArrayType(tmpType, saveValue);
            if (isGlobal) {
                tmpValue = buildFactory.buildGlobalArray(name, type, true);
                ((ConstArray) ((GlobalVar) tmpValue).getValue()).setInit(true);
            } else {
                tmpValue = buildFactory.buildArray(curBlock, true, type);
            }
            cur.addValue(name, tmpValue);
            curArray = tmpValue;
            tmpName = name;
            ConstInitVal(constDefNode.getConstInitValNode());
        }
    }

    private void ConstInitVal(ConstInitValNode constInitValNode) {
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
                StringBuilder name = new StringBuilder(tmpName);
                name.append(";").append(i);//比如arr[1]就是arr;1
                cur.addConstValue(name.toString(), saveValue);
            }
        } else {
            int tmpOffset = 0;
            for (ConstExpNode constExpNode : constInitValNode.getConstExpNodes()) {
                tmpValue = null;
                ConstExp(constExpNode);//返回值在saveValue中
                tmpValue = buildFactory.getConstInt(saveValue);
                if (isGlobal) {
                    //如果是全局数组，直接保存值
                    buildFactory.buildInitArray(curArray, tmpOffset, tmpValue);
                } else {
                    //计算数组元素地址，保存值
                    buildFactory.buildStore(curBlock, buildFactory.buildGEP(curBlock, curArray, tmpOffset), tmpValue);
                }
                StringBuilder name = new StringBuilder(tmpName);
                name.append(";").append(tmpOffset);//比如arr[1]就是arr;1
                cur.addConstValue(name.toString(), saveValue);
                tmpOffset++;
            }
        }
    }

    private void ConstExp(ConstExpNode constExp) {
        //ConstExp → AddExp
        isConst = true;
        saveValue = null;
        AddExp(constExp.getAddExpNode());
        isConst = false;
    }

    private void AddExp(AddExpNode addExpNode) {
        //AddExp -> MulExp [('+' | '-') AddExp]
        if (isConst) {
            saveValue = null;
            MulExp(addExpNode.getMulExpNode());
            Integer result = saveValue;
            AddExpNode curAddExpNode = addExpNode;
            while (curAddExpNode.getAddExpNode() != null) {
                TokenType op = curAddExpNode.getOpType();
                curAddExpNode = curAddExpNode.getAddExpNode();
                saveValue = null;
                MulExp(curAddExpNode.getMulExpNode());
                if (op == TokenType.PLUS) {
                    result = result + saveValue;
                } else {
                    result = result - saveValue;
                }
            }
            saveValue = result;
        } else {
            tmpValue = null;
            MulExp(addExpNode.getMulExpNode());
            Value leftValue = tmpValue;
            AddExpNode curAddExpNode = addExpNode;
            while (curAddExpNode.getAddExpNode() != null) {
                TokenType op = curAddExpNode.getOpType();
                curAddExpNode = curAddExpNode.getAddExpNode();
                tmpValue = null;
                MulExp(curAddExpNode.getMulExpNode());
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

    private void MulExp(MulExpNode mulExpNode) {
        //MulExp → UnaryExp [('*' | '/' | '%') MulExp]
        if (isConst) {
            saveValue = null;
            UnaryExp(mulExpNode.getUnaryExpNode());
            Integer result = saveValue;
            MulExpNode curMulExpNode = mulExpNode;
            while (curMulExpNode.getMulExpNode() != null) {
                TokenType op = curMulExpNode.getOpType();
                curMulExpNode = curMulExpNode.getMulExpNode();
                saveValue = null;
                UnaryExp(curMulExpNode.getUnaryExpNode());
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
            UnaryExp(mulExpNode.getUnaryExpNode());
            Value leftValue = tmpValue;
            MulExpNode curMulExpNode = mulExpNode;
            while (curMulExpNode.getMulExpNode() != null) {
                TokenType op = curMulExpNode.getOpType();
                curMulExpNode = curMulExpNode.getMulExpNode();
                tmpValue = null;
                UnaryExp(curMulExpNode.getUnaryExpNode());
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

    private void UnaryExp(UnaryExpNode unaryExpNode) {
        // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        if (unaryExpNode.getPrimaryExpNode() != null) {
            PrimaryExp(unaryExpNode.getPrimaryExpNode());
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
                UnaryExp(unaryExpNode.getUnaryExpNode());
            } else if (op == TokenType.MINU) {
                UnaryExp(unaryExpNode.getUnaryExpNode());
                if (isConst) {
                    saveValue = -saveValue;
                } else {
                    tmpValue = buildFactory.buildBinary(curBlock, Operator.Sub, ConstInt.ZERO, tmpValue);
                }
            } else {
                UnaryExp(unaryExpNode.getUnaryExpNode());
                tmpValue = buildFactory.buildNot(curBlock, tmpValue);
            }
        }
    }

    private void FuncRParams(FuncRParamsNode funcRParamsNode, List<Value> argList) {
        // FuncRParams → Exp { ',' Exp }
        for (ExpNode expNode : funcRParamsNode.getExpNodes()) {
            tmpValue = null;
            Exp(expNode);
            argList.add(tmpValue);
        }
    }

    private void PrimaryExp(PrimaryExpNode primaryExpNode) {
        // PrimaryExp → '(' Exp ')' | LVal | Number | Character
        if (primaryExpNode.getExpNode() != null) {
            Exp(primaryExpNode.getExpNode());
        } else if (primaryExpNode.getlValNode() != null) {
            LVal(primaryExpNode.getlValNode());
        } else if (primaryExpNode.getNumberNode() != null) {
            Number(primaryExpNode.getNumberNode());
        } else {
            Character(primaryExpNode.getCharacterNode());
        }
    }

    private void LVal(LValNode lValNode) {
        // LVal → Ident ['[' Exp ']']
        if (isConst) {
            StringBuilder name = new StringBuilder(lValNode.getIdent().getContent());
            if (lValNode.getExpNode() != null) {
                saveValue = null;
                Exp(lValNode.getExpNode());
                name.append(";").append(saveValue == null ? 0 : saveValue);
            }
            saveValue = cur.getConst(name.toString());
        } else {
            String name = lValNode.getIdent().getContent();
            Value addr = cur.getValueDeep(name);
            if (lValNode.getExpNode() != null) {
                tmpValue = null;
                Exp(lValNode.getExpNode());
                tmpValue = buildFactory.buildGEP(curBlock, addr, tmpValue);
            } else {
                tmpValue = buildFactory.buildLoad(curBlock, addr);
            }
        }
    }

    private void Number(NumberNode numberNode) {
        if (isConst) {
            saveValue = numberNode.getNumber();
        } else {
            tmpValue = buildFactory.getConstInt(numberNode.getNumber());
        }
    }

    private void Character(CharacterNode characterNode) {
        if (isConst) {
            saveValue = characterNode.getChar() - '\0';
        } else {
            tmpValue = buildFactory.getConstChar(characterNode.getChar());
        }
    }

    private void BType(BTypeNode bTypeNode) {
        if (bTypeNode.getToken().getType() == TokenType.INTTK) {
            tmpType = IntegerType.i32;
        } else {
            tmpType = IntegerType.i8;
        }
    }
}
