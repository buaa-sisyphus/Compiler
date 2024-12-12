package frontend;

import error.ErrorHandler;
import error.ErrorType;
import node.*;
import symbol.*;
import token.Token;
import token.TokenType;
import utils.CalUtils;

import java.util.List;

public class Visitor {
    private static final Visitor instance = new Visitor();
    public static int scope = 0;
    public static int loop = 0;//记录循环
    private SymbolScope root;
    private SymbolScope cur;

    private Visitor() {

    }

    public static Visitor getInstance() {
        return instance;
    }

    public SymbolScope getRootTable() {
        return root;
    }

    public void build(CompUnitNode compUnitNode) {
        CompUnit(compUnitNode);
    }

    private void pushTable(boolean needReturn, boolean isFunc) {
        scope++;
        SymbolScope newTable = new SymbolScope();
        newTable.setScopeNum(scope);
        newTable.setParent(cur);
        newTable.setFunc(isFunc);
        newTable.setNeedReturn(needReturn);
        cur.addChild(newTable);
        cur = newTable;
    }

    private void popTable() {
        cur = cur.getParent();
    }

    private void initTable() {
        loop = 0;
        scope = 1;
        root = new SymbolScope();
        root.setScopeNum(scope);
        root.setNeedReturn(false);
        root.setFunc(false);
        cur = root;
    }

    private boolean matchParams(List<FuncParam> params, List<ExpNode> expNodes, SymbolScope table) {
        for (int i = 0; i < params.size(); i++) {
            String paramType = params.get(i).toType();
            ExpNode expNode = expNodes.get(i);
            String tmp = expNode.getType();
            if (tmp.equals("0")) {
                //立即数
                if (paramType.contains("Array")) {
                    return false;
                }
            } else {
                String symbolType = table.getSymbolDeep(tmp).toType();
                if (symbolType.contains("Array") && paramType.contains("Array")) {
                    if ((symbolType.contains("Int") && paramType.contains("Char")) ||
                            (symbolType.contains("Char") && paramType.contains("Int"))) {
                        return false;
                    }
                } else if ((symbolType.contains("Array") && !paramType.contains("Array"))
                        || (!symbolType.contains("Array") && paramType.contains("Array"))) {
                    return false;
                }
            }
        }
        return true;
    }

    private void Block(BlockNode blockNode) {
        // Block → '{' { BlockItem } '}'
        List<BlockItemNode> blockItemNodes = blockNode.getBlockItemNodes();
        for (BlockItemNode blockItemNode : blockItemNodes) {
            BlockItem(blockItemNode);
        }
        if (cur.isFunc() && cur.getNeedReturn()) {
            if (blockItemNodes.isEmpty()) {
                ErrorHandler.getInstance().addError(ErrorType.g, blockNode.getrBraceToken().getLineNum());
                return;
            }
            BlockItemNode last = blockItemNodes.get(blockItemNodes.size() - 1);
            if (last.getDeclNode() != null) {
                ErrorHandler.getInstance().addError(ErrorType.g, blockNode.getrBraceToken().getLineNum());
                return;
            }
            if (last.getStmtNode().getStmtType() != StmtNode.StmtType.Return) {
                ErrorHandler.getInstance().addError(ErrorType.g, blockNode.getrBraceToken().getLineNum());
                return;
            }
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
                LVal(stmtNode.getlValNode(), true);
                Exp(stmtNode.getExpNode());
                break;
            case Exp:
                // Stmt → [Exp] ';'
                if (stmtNode.getExpNode() != null) Exp(stmtNode.getExpNode());
                break;
            case If:
                //Stmt → 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
                Cond(stmtNode.getCondNode());
                List<StmtNode> stmtNodes = stmtNode.getStmtNodes();
                Stmt(stmtNodes.get(0));
                if (stmtNodes.size() == 2) {
                    Stmt(stmtNodes.get(1));
                }
                break;
            case For:
                //Stmt → 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
                if (stmtNode.getForStmtNodeFir() != null) {
                    ForStmt(stmtNode.getForStmtNodeFir());
                }
                if (stmtNode.getCondNode() != null) {
                    Cond(stmtNode.getCondNode());
                }
                if (stmtNode.getForStmtNodeSec() != null) {
                    ForStmt(stmtNode.getForStmtNodeSec());
                }
                loop++;
                Stmt(stmtNode.getStmtNode());
                loop--;
                break;
            case Break:
            case Continue:
                // Stmt → 'break' ';' | 'continue' ';'
                if (loop <= 0) {
                    ErrorHandler.getInstance().addError(ErrorType.m, stmtNode.getBreakToken().getLineNum());
                }
                break;
            case Return:
                // Stmt → 'return' [Exp] ';'
                ExpNode expNode = stmtNode.getExpNode();
                SymbolScope tmp = null;
                boolean flag = false;
                if (expNode != null) {
                    //有返回值
                    for (tmp = cur; tmp != null; tmp = tmp.getParent()) {
                        //一直往上找，找到所属的函数
                        if (tmp.isFunc()) {
                            if (!tmp.getNeedReturn()) flag = true;
                            break;
                        }
                    }
                    if (flag) {
                        ErrorHandler.getInstance().addError(ErrorType.f, stmtNode.getReturnToken().getLineNum());
                    }
                    Exp(stmtNode.getExpNode());
                }
                break;
            case Printf:
                // Stmt → 'printf''('StringConst {','Exp}')'';'
                List<ExpNode> expNodes = stmtNode.getExpNodes();
                int cnt = CalUtils.calFormatSpecifiers(stmtNode.getStringToken().getContent());
                if (expNodes == null) {
                    if (cnt != 0)
                        ErrorHandler.getInstance().addError(ErrorType.l, stmtNode.getPrintfToken().getLineNum());
                } else if (cnt != expNodes.size()) {
                    ErrorHandler.getInstance().addError(ErrorType.l, stmtNode.getPrintfToken().getLineNum());
                }
                for (ExpNode expNode1 : expNodes) {
                    Exp(expNode1);
                }
                break;
            case GetChar:
            case GetInt:
                // Stmt → LVal '=' 'getint''('')'';' | LVal '=' 'getchar''('')'';'
                LVal(stmtNode.getlValNode(), true);
                break;
            case Block:
                // Stmt → Block
                boolean needReturn = false;
                boolean isFunc = false;
                pushTable(needReturn, isFunc);
                Block(stmtNode.getBlockNode());
                popTable();
                break;
            default:
                System.out.println("StmtType Error");
                break;
        }
    }

    private int BType(BTypeNode bTypeNode) {
        if (bTypeNode.getToken().getType() == TokenType.INTTK) {
            return 0;
        } else {
            return 1;
        }
    }

    private void Cond(CondNode condNode) {
        // Cond → LOrExp
        LOrExp(condNode.getlOrExpNode());
    }

    private void LOrExp(LOrExpNode lOrExpNode) {
        //LOrExp → LAndExp ['||' LOrExp]
        LAndExp(lOrExpNode.getlAndExpNode());
        LOrExpNode lorExpNode1 = lOrExpNode.getlOrExpNode();
        if (lorExpNode1 != null) {
            LOrExp(lorExpNode1);
        }
    }

    private void LAndExp(LAndExpNode lAndExpNode) {
        //LAndExp → EqExp ['&&' LAndExp]
        EqExp(lAndExpNode.getEqExpNode());
        LAndExpNode lAndExpNode1 = lAndExpNode.getlAndExpNode();
        if (lAndExpNode1 != null) {
            LAndExp(lAndExpNode1);
        }
    }

    private void EqExp(EqExpNode eqExpNode) {
        //EqExp → RelExp [('==' | '!=') EqExp]
        RelExp(eqExpNode.getRelExpNode());
        EqExpNode eqExpNode1 = eqExpNode.getEqExpNode();
        if (eqExpNode1 != null) {
            EqExp(eqExpNode1);
        }
    }

    private void RelExp(RelExpNode relExpNode) {
        //RelExp → AddExp [('<' | '>' | '<=' | '>=') RelExp]
        AddExp(relExpNode.getAddExpNode());
        RelExpNode relExpNode1 = relExpNode.getRelExpNode();
        if (relExpNode1 != null) {
            RelExp(relExpNode1);
        }
    }

    private void CompUnit(CompUnitNode compUnitNode) {
        // CompUnit → {Decl} {FuncDef} MainFuncDef
        initTable();
        for (DeclNode declNode : compUnitNode.getDeclNodes()) {
            Decl(declNode);
        }
        for (FuncDefNode funcDefNode : compUnitNode.getFuncDefNodes()) {
            FuncDef(funcDefNode);
        }
        MainFuncDef(compUnitNode.getMainFuncDefNode());
    }

    private void ConstDecl(ConstDeclNode constDeclNode) {
        // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        int btype = BType(constDeclNode.getbTypeNode());
        int con = 1;
        for (ConstDefNode constDefNode : constDeclNode.getConstDefNodes()) {
            ConstDef(constDefNode, btype, con);
        }
    }

    private void ConstDef(ConstDefNode constDefNode, int btype, int con) {
        // ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal
        Token ident = constDefNode.getIdent();
        Symbol symbol = cur.getSymbol(ident.getContent());
        if (symbol != null) {
            ErrorHandler.getInstance().addError(ErrorType.b, ident.getLineNum());
            return;
        }
        VarSymbol varSymbol = new VarSymbol();
        int type = 0;
        if (constDefNode.getConstExpNode() != null) {
            type = 1;
            ConstExp(constDefNode.getConstExpNode());
        }
        ConstInitVal(constDefNode.getConstInitValNode());
        varSymbol.set(ident, cur.getScopeNum(), type, btype, con);
        cur.addSymbol(ident.getContent(), varSymbol);
    }

    private void ConstInitVal(ConstInitValNode constInitValNode) {
        // ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
        if (constInitValNode.getConstExp() != null) {
            ConstExp(constInitValNode.getConstExp());
        } else if (constInitValNode.getStringConst() != null) {

        } else if (constInitValNode.getConstExpNodes() != null) {
            List<ConstExpNode> constExpNodes = constInitValNode.getConstExpNodes();
            for (ConstExpNode constExpNode : constExpNodes) {
                ConstExp(constExpNode);
            }
        }
    }

    private void ConstExp(ConstExpNode constExpNode) {
        //ConstExp → AddExp
        AddExp(constExpNode.getAddExpNode());
    }

    private void AddExp(AddExpNode addExpNode) {
        //AddExp -> MulExp [('+' | '-') AddExp]
        MulExp(addExpNode.getMulExpNode());
        if (addExpNode.getAddExpNode() != null) {
            AddExp(addExpNode.getAddExpNode());
        }
    }

    private void MulExp(MulExpNode mulExpNode) {
        //MulExp → UnaryExp [('*' | '/' | '%') MulExp]
        UnaryExp(mulExpNode.getUnaryExpNode());
        if (mulExpNode.getMulExpNode() != null) {
            MulExp(mulExpNode.getMulExpNode());
        }
    }

    private void UnaryExp(UnaryExpNode unaryExpNode) {
        // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        if (unaryExpNode.getPrimaryExpNode() != null) {
            PrimaryExp(unaryExpNode.getPrimaryExpNode());
        } else if (unaryExpNode.getIdent() != null) {
            Token ident = unaryExpNode.getIdent();
            Symbol symbol = cur.getSymbolDeep(ident.getContent());
            if (symbol == null) {
                ErrorHandler.getInstance().addError(ErrorType.c, ident.getLineNum());
                return;
            }
            if (symbol instanceof FuncSymbol) {
                FuncSymbol funcSymbol = (FuncSymbol) symbol;
                int give = funcSymbol.getParamsCount();
                FuncRParamsNode funcRParamsNode = unaryExpNode.getFuncRParamsNode();
                List<FuncParam> params = funcSymbol.getParams();
                if (funcRParamsNode != null) {
                    List<ExpNode> expNodes = funcRParamsNode.getExpNodes();
                    if (give != expNodes.size()) {
                        // 数量不匹配
                        ErrorHandler.getInstance().addError(ErrorType.d, ident.getLineNum());
                        return;
                    } else if (!matchParams(params, expNodes, cur)) {
                        // 类型不匹配
                        ErrorHandler.getInstance().addError(ErrorType.e, ident.getLineNum());
                        return;
                    }
                    FuncRParams(funcRParamsNode);
                } else {
                    if (give != 0) {
                        // 数量不匹配
                        ErrorHandler.getInstance().addError(ErrorType.d, ident.getLineNum());
                        return;
                    }
                }
            } else {
                //是什么类型错误？
                ErrorHandler.getInstance().addError(ErrorType.e, ident.getLineNum());
            }
        } else {
            UnaryExp(unaryExpNode.getUnaryExpNode());
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

    private void Exp(ExpNode expNode) {
        // Exp → AddExp
        AddExp(expNode.getAddExpNode());
    }

    private void FuncDef(FuncDefNode funcDefNode) {
        // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        int btype = FuncType(funcDefNode.getFuncTypeNode());
        boolean needReturn = false;
        boolean isFunc = true;
        if (btype != 2) needReturn = true;
        int type = 2;
        int con = 0;
        Token ident = funcDefNode.getIdent();
        Symbol symbol = cur.getSymbol(ident.getContent());
        FuncSymbol funcSymbol = null;
        if (symbol != null) {
            //如果发现一个重命名的，需要立即返回吗？
            ErrorHandler.getInstance().addError(ErrorType.b, ident.getLineNum());
//            return;
        } else {
            funcSymbol = new FuncSymbol();
            funcSymbol.set(ident, cur.getScopeNum(), type, btype, con);
            cur.addSymbol(ident.getContent(), funcSymbol);
        }
        //新建表
        pushTable(needReturn, isFunc);
        if (funcDefNode.getFuncFParamsNode() != null) {
            FuncFParams(funcDefNode.getFuncFParamsNode(), funcSymbol);
        }
        Block(funcDefNode.getBlockNode());
        popTable();
    }

    private void FuncFParams(FuncFParamsNode funcFParamsNode, FuncSymbol funcSymbol) {
        // FuncFParams → FuncFParam { ',' FuncFParam }
        for (FuncFParamNode funcFParamNode : funcFParamsNode.getFuncFParamNodes()) {
            FuncFParam(funcFParamNode, funcSymbol);
        }
    }

    private void FuncFParam(FuncFParamNode funcFParamNode, FuncSymbol funcSymbol) {
        // FuncFParam → BType Ident ['[' ']']
        int btype = BType(funcFParamNode.getbTypeNode());
        Token ident = funcFParamNode.getIdent();
        int type = funcFParamNode.isArray();
        Symbol symbol = cur.getSymbol(ident.getContent());
        if (symbol != null) {
            ErrorHandler.getInstance().addError(ErrorType.b, ident.getLineNum());
            return;
        }
        VarSymbol varSymbol = new VarSymbol();
        varSymbol.set(ident, cur.getScopeNum(), type, btype, 0);
        if (funcSymbol != null) funcSymbol.addParam(new FuncParam(varSymbol.getName(), btype, type));
        cur.addSymbol(ident.getContent(), varSymbol);
    }

    private void FuncRParams(FuncRParamsNode funcRParamsNode) {
        // FuncRParams → Exp { ',' Exp }
        for (ExpNode expNode : funcRParamsNode.getExpNodes()) {
            Exp(expNode);
        }
    }

    private void ForStmt(ForStmtNode forStmtNode) {
        // ForStmt → LVal '=' Exp
        LVal(forStmtNode.getlValNode(), true);
        Exp(forStmtNode.getExpNode());
    }

    private int FuncType(FuncTypeNode funcTypeNode) {
        if (funcTypeNode.getToken().getType() == TokenType.INTTK) {
            return 0;
        } else if (funcTypeNode.getToken().getType() == TokenType.CHARTK) {
            return 1;
        } else {
            return 2;
        }
    }

    private void InitVal(InitValNode initValNode) {
        // InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst
        if (initValNode.getExpNode() != null) {
            Exp(initValNode.getExpNode());
        } else if (initValNode.getStringConst() != null) {

        } else if (initValNode.getExpNodes() != null) {
            for (ExpNode expNode : initValNode.getExpNodes()) {
                Exp(expNode);
            }
        }
    }

    private void LVal(LValNode lValNode, boolean isAssign) {
        // LVal → Ident ['[' Exp ']']
        Token ident = lValNode.getIdent();
        Symbol symbol = cur.getSymbolDeep(ident.getContent());
        if (symbol == null) {
            ErrorHandler.getInstance().addError(ErrorType.c, ident.getLineNum());
            return;
        } else if (symbol.getCon() == 1 && isAssign) {
            ErrorHandler.getInstance().addError(ErrorType.h, ident.getLineNum());
            return;
        }
        if (lValNode.getExpNode() != null) {
            Exp(lValNode.getExpNode());
        }
    }

    private void MainFuncDef(MainFuncDefNode mainFuncDefNode) {
        // MainFuncDef → 'int' 'main' '(' ')' Block
        boolean needReturn = true;
        boolean isFunc = true;
        pushTable(needReturn, isFunc);
        Block(mainFuncDefNode.getBlock());
        popTable();
    }

    private void PrimaryExp(PrimaryExpNode primaryExpNode) {
        // PrimaryExp → '(' Exp ')' | LVal | Number | Character
        if (primaryExpNode.getExpNode() != null) {
            Exp(primaryExpNode.getExpNode());
        } else if (primaryExpNode.getlValNode() != null) {
            LVal(primaryExpNode.getlValNode(), false);
        } else if (primaryExpNode.getNumberNode() != null) {

        } else if (primaryExpNode.getCharacterNode() != null) {

        }
    }

    private void VarDecl(VarDeclNode varDeclNode) {
        // VarDecl → BType VarDef { ',' VarDef } ';'
        int btype = BType(varDeclNode.getbTypeNode());
        int con = 0;
        for (VarDefNode varDefNode : varDeclNode.getVarDefNodes()) {
            VarDef(varDefNode, btype, con);
        }
    }

    private void VarDef(VarDefNode varDefNode, int btype, int con) {
        // VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
        Token ident = varDefNode.getIdent();
        Symbol symbol = cur.getSymbol(ident.getContent());
        if (symbol != null) {
            ErrorHandler.getInstance().addError(ErrorType.b, ident.getLineNum());
            return;
        }
        VarSymbol varSymbol = new VarSymbol();
        int type = 0;
        if (varDefNode.getConstExpNode() != null) {
            type = 1;
            ConstExp(varDefNode.getConstExpNode());
        }
        varSymbol.set(ident, cur.getScopeNum(), type, btype, con);
        cur.addSymbol(ident.getContent(), varSymbol);
        if (varDefNode.getInitValNode() != null) {
            InitVal(varDefNode.getInitValNode());
        }
    }
}