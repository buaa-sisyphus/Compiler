package node;

import error.ErrorHandler;
import error.ErrorType;
import frontend.Parser;
import symbol.SymbolTable;
import token.Token;
import utils.CalUtils;
import utils.IOUtils;

import java.util.List;

/**
 * Stmt → LVal '=' Exp ';'
 * | [Exp] ';'
 * | Block
 * | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
 * | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
 * | 'break' ';'
 * | 'continue' ';'
 * | 'return' [Exp] ';'
 * | LVal '=' 'getint''('')'';'
 * | LVal '=' 'getchar''('')'';'
 * | 'printf''('StringConst {','Exp}')'';'
 */
public class StmtNode extends Node {
    public static int loop = 0;
    private BlockNode blockNode;
    private CondNode condNode;
    private ExpNode expNode;
    private ForStmtNode forStmtNodeFir;
    private ForStmtNode forStmtNodeSec;
    private LValNode lValNode;
    private StmtType stmtType;
    private StmtNode stmtNode;

    private Token assignToken;
    private Token semicnToken;
    private Token lParenToken;
    private Token rParenToken;
    private Token ifToken;
    private Token elseToken;
    private Token forToken;
    private Token breakToken;
    private Token returnToken;
    private Token getToken;
    private Token printfToken;
    private Token stringToken;

    private List<StmtNode> stmtNodes;
    private List<Token> semicnTokens;
    private List<Token> commaTokens;
    private List<ExpNode> expNodes;

    public enum StmtType {
        LVal,
        Exp,
        If,
        For,
        Break,
        Continue,
        Return,
        GetInt,
        GetChar,
        Printf,
        Block,
    }

    public StmtNode(StmtType stmtType, Token printfToken, Token lParenToken, Token stringToken, List<Token> commaTokens, List<ExpNode> expNodes, Token rParenToken, Token semicnToken) {
        // Stmt → 'printf''('StringConst {','Exp}')'';'
        this.printfToken = printfToken;
        this.lParenToken = lParenToken;
        this.stringToken = stringToken;
        this.commaTokens = commaTokens;
        this.expNodes = expNodes;
        this.rParenToken = rParenToken;
        this.semicnToken = semicnToken;
        this.type = NodeType.Stmt;
        this.stmtType = stmtType;
    }

    public StmtNode(StmtType stmtType, LValNode lValNode, Token assignToken, Token getToken, Token lParenToken, Token rParenToken, Token semicnToken) {
        // Stmt → LVal '=' 'getint''('')'';' | LVal '=' 'getchar''('')'';'
        this.lValNode = lValNode;
        this.assignToken = assignToken;
        this.getToken = getToken;
        this.lParenToken = lParenToken;
        this.rParenToken = rParenToken;
        this.semicnToken = semicnToken;
        this.type = NodeType.Stmt;
        this.stmtType = stmtType;
    }

    public StmtNode(StmtType stmtType, Token breakToken, Token semicnToken) {
        // Stmt → 'break' ';' | 'continue' ';'
        this.semicnToken = semicnToken;
        this.breakToken = breakToken;
        this.type = NodeType.Stmt;
        this.stmtType = stmtType;
    }

    public StmtNode(StmtType stmtType, Token returnToken, Token semicnToken, ExpNode expNode) {
        // Stmt → 'return' [Exp] ';'
        this.semicnToken = semicnToken;
        this.returnToken = returnToken;
        this.expNode = expNode;
        this.type = NodeType.Stmt;
        this.stmtType = stmtType;
    }

    public StmtNode(StmtType stmtType, LValNode lValNode, Token assignToken, ExpNode expNode, Token semicnToken) {
        // Stmt → LVal '=' Exp ';'
        this.lValNode = lValNode;
        this.assignToken = assignToken;
        this.expNode = expNode;
        this.semicnToken = semicnToken;
        this.type = NodeType.Stmt;
        this.stmtType = stmtType;
    }

    public StmtNode(StmtType stmtType, ExpNode expNode, Token semicnToken) {
        // Stmt → [Exp] ';'
        this.expNode = expNode;
        this.semicnToken = semicnToken;
        this.type = NodeType.Stmt;
        this.stmtType = stmtType;
    }

    public StmtNode(StmtType stmtType, BlockNode blockNode) {
        // Stmt → Block
        this.blockNode = blockNode;
        this.type = NodeType.Stmt;
        this.stmtType = stmtType;
    }

    public StmtNode(StmtType stmtType, Token ifToken, Token lParenToken, CondNode condNode, Token rParenToken, List<StmtNode> stmtNodes, Token elseToken) {
        //Stmt → 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
        this.ifToken = ifToken;
        this.lParenToken = lParenToken;
        this.condNode = condNode;
        this.rParenToken = rParenToken;
        this.stmtNodes = stmtNodes;
        this.elseToken = elseToken;
        this.type = NodeType.Stmt;
        this.stmtType = stmtType;
    }

    public StmtNode(StmtType stmtType, Token forToken, Token lParenToken, ForStmtNode forStmtNodeFir, CondNode condNode, ForStmtNode forStmtNodeSec, List<Token> semicnTokens, Token rParenToken, StmtNode stmtNode) {
        //Stmt → 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
        this.forToken = forToken;
        this.lParenToken = lParenToken;
        this.condNode = condNode;
        this.forStmtNodeFir = forStmtNodeFir;
        this.forStmtNodeSec = forStmtNodeSec;
        this.semicnTokens = semicnTokens;
        this.rParenToken = rParenToken;
        this.stmtNode = stmtNode;
        this.type = NodeType.Stmt;
        this.stmtType = stmtType;
    }

    @Override
    public void print() {
        switch (stmtType) {
            case LVal:
                // Stmt → LVal '=' Exp ';'
                lValNode.print();
                IOUtils.write(assignToken.toString());
                expNode.print();
                IOUtils.write(semicnToken.toString());
                IOUtils.write(typeToString());
                break;
            case Exp:
                // Stmt → [Exp] ';'
                if (expNode != null) {
                    expNode.print();
                }
                IOUtils.write(semicnToken.toString());
                IOUtils.write(typeToString());
                break;
            case If:
                //Stmt → 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
                IOUtils.write(ifToken.toString());
                IOUtils.write(lParenToken.toString());
                condNode.print();
                IOUtils.write(rParenToken.toString());
                stmtNodes.get(0).print();
                if (elseToken != null) {
                    IOUtils.write(elseToken.toString());
                    stmtNodes.get(1).print();
                }
                IOUtils.write(typeToString());
                break;
            case For:
                //Stmt → 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
                IOUtils.write(forToken.toString());
                IOUtils.write(lParenToken.toString());
                if (forStmtNodeFir != null) forStmtNodeFir.print();
                IOUtils.write(semicnTokens.get(0).toString());
                if (condNode != null) condNode.print();
                IOUtils.write(semicnTokens.get(1).toString());
                if (forStmtNodeSec != null) forStmtNodeSec.print();
                IOUtils.write(rParenToken.toString());
                stmtNode.print();
                IOUtils.write(typeToString());
                break;
            case Break:
            case Continue:
                // Stmt → 'break' ';' | 'continue' ';'
                IOUtils.write(breakToken.toString());
                IOUtils.write(semicnToken.toString());
                IOUtils.write(typeToString());
                break;
            case Return:
                // Stmt → 'return' [Exp] ';'
                IOUtils.write(returnToken.toString());
                if (expNode != null) expNode.print();
                IOUtils.write(semicnToken.toString());
                IOUtils.write(typeToString());
                break;
            case Printf:
                // Stmt → 'printf''('StringConst {','Exp}')'';'
                IOUtils.write(printfToken.toString());
                IOUtils.write(lParenToken.toString());
                IOUtils.write(stringToken.toString());
                if (expNodes != null && !expNodes.isEmpty()) {
                    for (int i = 0; i < expNodes.size(); i++) {
                        IOUtils.write(commaTokens.get(i).toString());
                        expNodes.get(i).print();
                    }
                }
                IOUtils.write(rParenToken.toString());
                IOUtils.write(semicnToken.toString());
                IOUtils.write(typeToString());
                break;
            case GetChar:
            case GetInt:
                // Stmt → LVal '=' 'getint''('')'';' | LVal '=' 'getchar''('')'';'
                lValNode.print();
                IOUtils.write(assignToken.toString());
                IOUtils.write(getToken.toString());
                IOUtils.write(lParenToken.toString());
                IOUtils.write(rParenToken.toString());
                IOUtils.write(semicnToken.toString());
                IOUtils.write(typeToString());
                break;
            case Block:
                // Stmt → Block
                blockNode.print();
                IOUtils.write(typeToString());
                break;
            default:
                System.out.println("StmtType Error");
                break;
        }
    }

    public void fill(SymbolTable table) {
        switch (stmtType) {
            case LVal:
                // Stmt → LVal '=' Exp ';'
                lValNode.fill(table,true);
                break;
            case Exp:
                // Stmt → [Exp] ';'
                if (expNode != null) {
                    expNode.fill(table);
                }
                break;
            case If:
                //Stmt → 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
                stmtNodes.get(0).fill(table);
                if (elseToken != null) {
                    stmtNodes.get(1).fill(table);
                }
                break;
            case For:
                //Stmt → 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
                loop++;
                stmtNode.fill(table);
                loop--;
                break;
            case Break:
            case Continue:
                // Stmt → 'break' ';' | 'continue' ';'
                if (loop == 0) {
                    ErrorHandler.getInstance().addError(ErrorType.m, breakToken.getLineNum());
                }
                break;
            case Return:
                // Stmt → 'return' [Exp] ';'

                break;
            case Printf:
                // Stmt → 'printf''('StringConst {','Exp}')'';'
                int cnt = CalUtils.calFormatSpecifiers(stringToken.getContent());
                int size = expNodes.size();
                if (cnt != size) ErrorHandler.getInstance().addError(ErrorType.i, printfToken.getLineNum());
                break;
            case GetChar:
            case GetInt:
                // Stmt → LVal '=' 'getint''('')'';' | LVal '=' 'getchar''('')'';'
                lValNode.fill(table,true);
                break;
            case Block:
                // Stmt → Block
                SymbolTable newTable = new SymbolTable();
                table.addChild(newTable);
                Parser.scope++;
                newTable.setScopeNum(Parser.scope);
                newTable.setParentTable(table);
                blockNode.fill(newTable);
                break;
            default:
                System.out.println("StmtType Error");
                break;
        }
    }

    public void handleReturn(SymbolTable table) {
        switch (stmtType) {
            case Return:
                if (expNode != null) {
                    ErrorHandler.getInstance().addError(ErrorType.f, returnToken.getLineNum());
                }
                break;
            default:
                fill(table);
                break;
        }
    }

    public StmtType getStmtType() {
        return stmtType;
    }
}
