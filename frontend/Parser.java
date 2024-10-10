package frontend;

import error.ErrorType;
import node.*;
import token.Token;
import token.TokenType;
import error.Error;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class Parser {

    private List<Token> tokens;
    private List<Error> errors;
    private CompUnitNode compUnitNode;
    private int index = 0;

    public Parser(List<Token> tokens, List<Error> errors) {
        this.tokens = tokens;
        this.errors = errors;
    }

    private Token match(TokenType type) {
        if (tokens.get(index).getType() == type) {
            Token token = tokens.get(index);
            index++;
            return token;
        } else if (type == TokenType.SEMICN) {
            Token token = tokens.get(index - 1);
            errors.add(new Error(ErrorType.i, token.getLineNum()));
            return new Token(TokenType.SEMICN,token.getLineNum(),";");
        } else if (type == TokenType.RPARENT) {
            Token token = tokens.get(index - 1);
            errors.add(new Error(ErrorType.j, token.getLineNum()));
            return new Token(TokenType.RPARENT,token.getLineNum(),")");
        } else if (type == TokenType.RBRACK) {
            Token token = tokens.get(index - 1);
            errors.add(new Error(ErrorType.k, token.getLineNum()));
            return new Token(TokenType.RBRACK,token.getLineNum(),"]");
        }
        return null;
    }

    public void analyze() {
        compUnitNode = CompUnit();
    }

    public void print() {
        if (compUnitNode != null) compUnitNode.print();
    }

    private AddExpNode AddExp() {
        // AddExp -> MulExp [('+' | '-') AddExp]
        MulExpNode mulExpNode = MulExp();
        Token op = null;
        AddExpNode addExpNode = null;
        if (tokens.get(index).getType() == TokenType.PLUS) {
            op = match(TokenType.PLUS);
            addExpNode = AddExp();
        } else if (tokens.get(index).getType() == TokenType.MINU) {
            op = match(TokenType.MINU);
            addExpNode = AddExp();
        }
        return new AddExpNode(mulExpNode, addExpNode, op);
    }

    private BlockItemNode BlockItem() {
        // BlockItem → Decl | Stmt
        DeclNode declNode = null;
        StmtNode stmtNode = null;
        if (tokens.get(index).getType() == TokenType.INTTK || tokens.get(index).getType() == TokenType.CHARTK || tokens.get(index).getType() == TokenType.CONSTTK) {
            declNode = Decl();
        } else {
            stmtNode = Stmt();
        }
        return new BlockItemNode(declNode, stmtNode);
    }

    private BlockNode Block() {
        // Block → '{' { BlockItem } '}'
        Token lBrace = match(TokenType.LBRACE);
        List<BlockItemNode> blockItemNodes = new ArrayList<>();
        while (tokens.get(index).getType() != TokenType.RBRACE) {
            blockItemNodes.add(BlockItem());
        }
        Token rBrace = match(TokenType.RBRACE);
        return new BlockNode(lBrace, blockItemNodes, rBrace);
    }

    private BTypeNode BType() {
        // BType → 'int' | 'char'
        Token token = null;
        if (tokens.get(index).getType() == TokenType.INTTK) {
            token = match(TokenType.INTTK);
        } else if (tokens.get(index).getType() == TokenType.CHARTK) {
            token = match(TokenType.CHARTK);
        }
        return new BTypeNode(token);
    }

    private CharacterNode Character() {
        // Character → CharConst
        Token token = match(TokenType.CHRCON);
        return new CharacterNode(token);
    }

    private CompUnitNode CompUnit() {
        // CompUnit → {Decl} {FuncDef} MainFuncDef
        List<DeclNode> declNodes = new ArrayList<>();
        List<FuncDefNode> funcDefNodes = new ArrayList<>();
        while (tokens.get(index + 1).getType() != TokenType.MAINTK && tokens.get(index + 2).getType() != TokenType.LPARENT) {
            DeclNode declNode = Decl();
            declNodes.add(declNode);
        }
        while (tokens.get(index + 1).getType() != TokenType.MAINTK) {
            FuncDefNode funcDefNode = FuncDef();
            funcDefNodes.add(funcDefNode);
        }
        MainFuncDefNode mainFuncDefNode = MainFuncDef();
        return new CompUnitNode(declNodes, funcDefNodes, mainFuncDefNode);
    }

    private CondNode Cond() {
        // Cond → LOrExp
        return new CondNode(LOrExp());
    }

    private ConstDeclNode ConstDecl() {
        // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        Token constToken = match(TokenType.CONSTTK);
        BTypeNode bTypeNode = BType();
        List<ConstDefNode> constDefNodes = new ArrayList<>();
        List<Token> commaTokens = new ArrayList<>();
        constDefNodes.add(ConstDef());
        while (tokens.get(index).getType() == TokenType.COMMA) {
            commaTokens.add(match(TokenType.COMMA));
            constDefNodes.add(ConstDef());
        }
        Token semicnToken = match(TokenType.SEMICN);
        return new ConstDeclNode(constToken, bTypeNode, constDefNodes, commaTokens, semicnToken);
    }

    private ConstDefNode ConstDef() {
        // ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal
        Token ident = match(TokenType.IDENFR);
        Token lBrackToken = null;
        Token rBrackToken = null;
        ConstExpNode constExpNode = null;
        if (tokens.get(index).getType() == TokenType.LBRACK) {
            lBrackToken = match(TokenType.LBRACK);
            constExpNode = ConstExp();
            rBrackToken = match(TokenType.RBRACK);
        }
        Token assignToken = match(TokenType.ASSIGN);
        ConstInitValNode constInitValNode = ConstInitVal();
        return new ConstDefNode(ident, lBrackToken, constExpNode, rBrackToken, assignToken, constInitValNode);
    }

    private ConstExpNode ConstExp() {
        //ConstExp → AddExp
        AddExpNode addExpNode = AddExp();
        return new ConstExpNode(addExpNode);
    }

    private ConstInitValNode ConstInitVal() {
        // ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
        if (tokens.get(index).getType() == TokenType.LBRACE) {
            Token lBraceToken = match(TokenType.LBRACE);
            List<Token> commaTokens = new ArrayList<>();
            List<ConstExpNode> constExpNodes = new ArrayList<>();
            if (tokens.get(index).getType() != TokenType.RBRACE) {
                constExpNodes.add(ConstExp());
                while (tokens.get(index).getType() == TokenType.COMMA) {
                    commaTokens.add(match(TokenType.COMMA));
                    constExpNodes.add(ConstExp());
                }
            }
            Token rBraceToken = match(TokenType.RBRACE);
            return new ConstInitValNode(constExpNodes, lBraceToken, rBraceToken, commaTokens);
        } else if (tokens.get(index).getType() == TokenType.STRCON) {
            Token stringConst = match(TokenType.STRCON);
            return new ConstInitValNode(stringConst);
        } else {
            ConstExpNode constExpNode = ConstExp();
            return new ConstInitValNode(constExpNode);
        }
    }

    private DeclNode Decl() {
        // Decl → ConstDecl | VarDecl
        ConstDeclNode constDeclNode = null;
        VarDeclNode varDeclNode = null;
        if (tokens.get(index).getType() == TokenType.CONSTTK) {
            constDeclNode = ConstDecl();
        } else {
            varDeclNode = VarDecl();
        }
        return new DeclNode(constDeclNode, varDeclNode);
    }

    private EqExpNode EqExp() {
        // EqExp → RelExp [('==' | '!=') EqExp]
        RelExpNode relExpNode = RelExp();
        Token op = null;
        EqExpNode eqExpNode = null;
        if (tokens.get(index).getType() == TokenType.EQL) {
            op = match(TokenType.EQL);
            eqExpNode = EqExp();
        } else if (tokens.get(index).getType() == TokenType.NEQ) {
            op = match(TokenType.NEQ);
            eqExpNode = EqExp();
        }
        return new EqExpNode(relExpNode, eqExpNode, op);
    }

    private ExpNode Exp() {
        // Exp → AddExp
        return new ExpNode(AddExp());
    }

    private ForStmtNode ForStmt() {
        // ForStmt → LVal '=' Exp
        LValNode lValNode = LVal();
        Token assignToken = match(TokenType.ASSIGN);
        ExpNode expNode = Exp();
        return new ForStmtNode(lValNode, expNode, assignToken);
    }

    private FuncDefNode FuncDef() {
        // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        FuncTypeNode funcTypeNode = FuncType();
        Token ident = match(TokenType.IDENFR);
        Token lParentToken = match(TokenType.LPARENT);
        FuncFParamsNode funcFParamsNode = null;
        if (tokens.get(index).getType() != TokenType.RPARENT) {
            funcFParamsNode = FuncFParams();
        }
        Token rParentToken = match(TokenType.RPARENT);
        BlockNode blockNode = Block();
        return new FuncDefNode(funcTypeNode, ident, lParentToken, funcFParamsNode, rParentToken, blockNode);
    }

    private FuncFParamNode FuncFParam() {
        // FuncFParam → BType Ident ['[' ']']
        BTypeNode bTypeNode = BType();
        Token ident = match(TokenType.IDENFR);
        Token lBrackToken = null;
        Token rBrackToken = null;
        if (tokens.get(index).getType() == TokenType.LBRACK) {
            lBrackToken = match(TokenType.LBRACK);
            rBrackToken = match(TokenType.RBRACK);
        }
        return new FuncFParamNode(bTypeNode, ident, lBrackToken, rBrackToken);
    }

    private FuncFParamsNode FuncFParams() {
        // FuncFParams → FuncFParam { ',' FuncFParam }
        List<FuncFParamNode> funcFParamNodes = new ArrayList<>();
        List<Token> commaTokens = new ArrayList<>();
        funcFParamNodes.add(FuncFParam());
        while (tokens.get(index).getType() == TokenType.COMMA) {
            commaTokens.add(match(TokenType.COMMA));
            funcFParamNodes.add(FuncFParam());
        }
        return new FuncFParamsNode(funcFParamNodes, commaTokens);
    }

    private FuncRParamsNode FuncRParams() {
        // FuncRParams → Exp { ',' Exp }
        List<ExpNode> expNodes = new ArrayList<>();
        List<Token> commaTokens = new ArrayList<>();
        expNodes.add(Exp());
        while (tokens.get(index).getType() == TokenType.COMMA) {
            commaTokens.add(match(TokenType.COMMA));
            expNodes.add(Exp());
        }
        return new FuncRParamsNode(expNodes, commaTokens);
    }

    private FuncTypeNode FuncType() {
        // FuncType → 'void' | 'int' | 'char'
        Token token = null;
        if (tokens.get(index).getType() == TokenType.VOIDTK) {
            token = match(TokenType.VOIDTK);
        } else if (tokens.get(index).getType() == TokenType.INTTK) {
            token = match(TokenType.INTTK);
        } else {
            token = match(TokenType.CHARTK);
        }
        return new FuncTypeNode(token);
    }

    private InitValNode InitVal() {
        // InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst
        if (tokens.get(index).getType() == TokenType.LBRACE) {
            Token lBraceToken = match(TokenType.LBRACE);
            List<Token> commaTokens = new ArrayList<>();
            List<ExpNode> expNodes = new ArrayList<>();
            if (tokens.get(index).getType() != TokenType.RBRACE) {
                expNodes.add(Exp());
                while (tokens.get(index).getType() == TokenType.COMMA) {
                    commaTokens.add(match(TokenType.COMMA));
                    expNodes.add(Exp());
                }
            }
            Token rBraceToken = match(TokenType.RBRACE);
            return new InitValNode(expNodes, lBraceToken, rBraceToken, commaTokens);
        }else if(tokens.get(index).getType() == TokenType.STRCON){
            Token stringConst = match(TokenType.STRCON);
            return new InitValNode(stringConst);
        }else{
            ExpNode expNode = Exp();
            return new InitValNode(expNode);
        }
    }

    private LAndExpNode LAndExp() {
        // LAndExp → EqExp ['&&' LAndExp]
        EqExpNode eqExpNode = EqExp();
        Token op = null;
        LAndExpNode lAndExpNode = null;
        if (tokens.get(index).getType() == TokenType.AND) {
            op = match(TokenType.AND);
            lAndExpNode = LAndExp();
        }
        return new LAndExpNode(eqExpNode, lAndExpNode, op);
    }

    private LOrExpNode LOrExp() {
        // LOrExp → LAndExp ['||' LOrExp]
        LAndExpNode lAndExpNode = LAndExp();
        Token op = null;
        LOrExpNode lOrExpNode = null;
        if (tokens.get(index).getType() == TokenType.OR) {
            op = match(TokenType.OR);
            lOrExpNode = LOrExp();
        }
        return new LOrExpNode(lAndExpNode, lOrExpNode, op);
    }

    private LValNode LVal() {
        // LVal → Ident ['[' Exp ']']
        Token ident = match(TokenType.IDENFR);
        Token lBrackToken = null;
        Token rBrackToken = null;
        ExpNode expNode = null;
        if (tokens.get(index).getType() == TokenType.LBRACK) {
            lBrackToken = match(TokenType.LBRACK);
            expNode = Exp();
            rBrackToken = match(TokenType.RBRACK);
        }
        return new LValNode(ident, lBrackToken, rBrackToken, expNode);
    }

    private MainFuncDefNode MainFuncDef() {
        // MainFuncDef → 'int' 'main' '(' ')' Block
        Token intToken = match(TokenType.INTTK);
        Token mainToken = match(TokenType.MAINTK);
        Token lParentToken = match(TokenType.LPARENT);
        Token rParentToken = match(TokenType.RPARENT);
        BlockNode blockNode = Block();
        return new MainFuncDefNode(intToken, mainToken, lParentToken, rParentToken, blockNode);
    }

    private MulExpNode MulExp() {
        // MulExp → UnaryExp [('*' | '/' | '%') MulExp]
        UnaryExpNode unaryExpNode = UnaryExp();
        Token op = null;
        MulExpNode mulExpNode = null;
        if (tokens.get(index).getType() == TokenType.MULT) {
            op = match(TokenType.MULT);
            mulExpNode = MulExp();
        } else if (tokens.get(index).getType() == TokenType.DIV) {
            op = match(TokenType.DIV);
            mulExpNode = MulExp();
        } else if (tokens.get(index).getType() == TokenType.MOD) {
            op = match(TokenType.MOD);
            mulExpNode = MulExp();
        }
        return new MulExpNode(unaryExpNode, mulExpNode, op);
    }

    private NumberNode Number() {
        // Number → IntConst
        Token token = match(TokenType.INTCON);
        return new NumberNode(token);
    }

    private PrimaryExpNode PrimaryExp() {
        // PrimaryExp → '(' Exp ')' | LVal | Number | Character
        if (tokens.get(index).getType() == TokenType.LPARENT) {
            Token lParenToken = match(TokenType.LPARENT);
            ExpNode expNode = Exp();
            Token rParenToken = match(TokenType.RPARENT);
            return new PrimaryExpNode(lParenToken, rParenToken, expNode);
        } else if (tokens.get(index).getType() == TokenType.INTCON) {
            NumberNode numberNode = Number();
            return new PrimaryExpNode(numberNode);
        } else if (tokens.get(index).getType() == TokenType.CHRCON) {
            CharacterNode charNode = Character();
            return new PrimaryExpNode(charNode);
        } else if (tokens.get(index).getType() == TokenType.IDENFR) {
            LValNode lValNode = LVal();
            return new PrimaryExpNode(lValNode);
        } else {
            //todo error
        }
        return null;
    }

    private RelExpNode RelExp() {
        // RelExp → AddExp [('<' | '>' | '<=' | '>=') RelExp]
        AddExpNode addExpNode = AddExp();
        Token op = null;
        RelExpNode relExpNode = null;
        if (tokens.get(index).getType() == TokenType.GRE) {
            op = match(TokenType.GRE);
            relExpNode = RelExp();
        } else if (tokens.get(index).getType() == TokenType.LSS) {
            op = match(TokenType.LSS);
            relExpNode = RelExp();
        } else if (tokens.get(index).getType() == TokenType.LEQ) {
            op = match(TokenType.LEQ);
            relExpNode = RelExp();
        } else if (tokens.get(index).getType() == TokenType.GEQ) {
            op = match(TokenType.GEQ);
            relExpNode = RelExp();
        }
        return new RelExpNode(addExpNode, relExpNode, op);
    }

    private StmtNode Stmt() {
        if (tokens.get(index).getType() == TokenType.IFTK) {
            //Stmt → 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
            Token ifToken = match(TokenType.IFTK);
            Token lParenToken = match(TokenType.LPARENT);
            CondNode condNode = Cond();
            Token rparenToken = match(TokenType.RPARENT);
            List<StmtNode> stmtNodes = new ArrayList<>();
            stmtNodes.add(Stmt());
            Token elseToken = null;
            if (tokens.get(index).getType() == TokenType.ELSETK) {
                elseToken = match(TokenType.ELSETK);
                stmtNodes.add(Stmt());
            }
            return new StmtNode(StmtNode.StmtType.If, ifToken, lParenToken, condNode, rparenToken, stmtNodes, elseToken);
        } else if (tokens.get(index).getType() == TokenType.FORTK) {
            //Stmt → 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
            Token forToken = match(TokenType.FORTK);
            Token lParenToken = match(TokenType.LPARENT);
            List<Token> semicnTokens = new ArrayList<>();
            ForStmtNode forStmtNodeFir = null;
            ForStmtNode forStmtNodeSec = null;
            CondNode condNode = null;
            if (tokens.get(index).getType() == TokenType.IDENFR) {
                forStmtNodeFir = ForStmt();
            }
            semicnTokens.add(match(TokenType.SEMICN));
            if (tokens.get(index).getType() != TokenType.SEMICN) {
                condNode = Cond();
            }
            semicnTokens.add(match(TokenType.SEMICN));
            if (tokens.get(index).getType() == TokenType.IDENFR) {
                forStmtNodeSec = ForStmt();
            }
            Token rParenToken = match(TokenType.RPARENT);
            StmtNode stmtNode = Stmt();
            return new StmtNode(StmtNode.StmtType.For, forToken, lParenToken, forStmtNodeFir, condNode, forStmtNodeSec, semicnTokens, rParenToken, stmtNode);
        } else if (tokens.get(index).getType() == TokenType.BREAKTK) {
            // Stmt → 'break' ';'
            Token breakToken = match(TokenType.BREAKTK);
            Token semicnToken = match(TokenType.SEMICN);
            return new StmtNode(StmtNode.StmtType.Break, breakToken, semicnToken);
        } else if (tokens.get(index).getType() == TokenType.CONTINUETK) {
            // Stmt → 'continue' ';'
            Token breakToken = match(TokenType.CONTINUETK);
            Token semicnToken = match(TokenType.SEMICN);
            return new StmtNode(StmtNode.StmtType.Continue, breakToken, semicnToken);
        } else if (tokens.get(index).getType() == TokenType.RETURNTK) {
            // Stmt → 'return' [Exp] ';'
            Token returnToken = match(TokenType.RETURNTK);
            ExpNode expNode = null;
            if (tokens.get(index).getType() != TokenType.SEMICN) expNode = Exp();
            Token semicnToken = match(TokenType.SEMICN);
            return new StmtNode(StmtNode.StmtType.Return, returnToken, semicnToken, expNode);
        } else if (tokens.get(index).getType() == TokenType.PRINTFTK) {
            // Stmt → 'printf''('StringConst {','Exp}')'';'
            Token printToken = match(TokenType.PRINTFTK);
            Token lParenToken = match(TokenType.LPARENT);
            Token stringToken = match(TokenType.STRCON);
            List<Token> commaTokens = new ArrayList<>();
            List<ExpNode> expNodes = new ArrayList<>();
            while (tokens.get(index).getType() == TokenType.COMMA) {
                commaTokens.add(match(TokenType.COMMA));
                expNodes.add(Exp());
            }
            Token rParenToken = match(TokenType.RPARENT);
            Token semicnToken = match(TokenType.SEMICN);
            return new StmtNode(StmtNode.StmtType.Printf, printToken, lParenToken, stringToken, commaTokens, expNodes, rParenToken, semicnToken);
        } else if (tokens.get(index).getType() == TokenType.LBRACE) {
            // Stmt → Block
            BlockNode blockNode = Block();
            return new StmtNode(StmtNode.StmtType.Block, blockNode);
        } else {
            boolean flag = false;
            for (int i = index; i < tokens.size(); i++) {
                if (tokens.get(i).getType() == TokenType.SEMICN) {
                    break;
                }
                if (tokens.get(i).getType() == TokenType.ASSIGN) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                // Stmt → LVal '=' Exp ';'
                // Stmt → LVal '=' 'getint''('')'';' | LVal '=' 'getchar''('')'';'
                LValNode lValNode = LVal();
                Token assignToken = match(TokenType.ASSIGN);
                if (tokens.get(index).getType() == TokenType.GETINTTK) {
                    // Stmt → LVal '=' 'getint''('')'';'
                    Token getIntToken = match(TokenType.GETINTTK);
                    Token lParenToken = match(TokenType.LPARENT);
                    Token rParenToken = match(TokenType.RPARENT);
                    Token semicnToken = match(TokenType.SEMICN);
                    return new StmtNode(StmtNode.StmtType.GetInt, lValNode, assignToken, getIntToken, lParenToken, rParenToken, semicnToken);
                } else if (tokens.get(index).getType() == TokenType.GETCHARTK) {
                    // Stmt → LVal '=' 'getchar''('')'';'
                    Token getIntToken = match(TokenType.GETCHARTK);
                    Token lParenToken = match(TokenType.LPARENT);
                    Token rParenToken = match(TokenType.RPARENT);
                    Token semicnToken = match(TokenType.SEMICN);
                    return new StmtNode(StmtNode.StmtType.GetChar, lValNode, assignToken, getIntToken, lParenToken, rParenToken, semicnToken);
                } else {
                    // Stmt → LVal '=' Exp ';'
                    ExpNode expNode = Exp();
                    Token semicnToken = match(TokenType.SEMICN);
                    return new StmtNode(StmtNode.StmtType.LVal, lValNode, assignToken, expNode, semicnToken);
                }
            } else {
                // Stmt → [Exp] ';'
                ExpNode expNode = null;
                if (tokens.get(index).getType() != TokenType.SEMICN) {
                    expNode = Exp();
                }
                Token semicnToken = match(TokenType.SEMICN);
                return new StmtNode(StmtNode.StmtType.Exp, expNode, semicnToken);
            }
        }

    }

    private UnaryExpNode UnaryExp() {
        // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        if (tokens.get(index).getType() == TokenType.NOT || tokens.get(index).getType() == TokenType.PLUS || tokens.get(index).getType() == TokenType.MINU) {
            UnaryOpNode unaryOpNode = UnaryOp();
            UnaryExpNode unaryExpNode = UnaryExp();
            return new UnaryExpNode(unaryOpNode, unaryExpNode);
        } else if (tokens.get(index).getType() == TokenType.IDENFR && tokens.get(index + 1).getType() == TokenType.LPARENT) {
            Token identToken = match(TokenType.IDENFR);
            Token lParentToken = match(TokenType.LPARENT);
            FuncRParamsNode funcRParamsNode = null;
            if (tokens.get(index).getType() != TokenType.RPARENT) {
                funcRParamsNode = FuncRParams();
            }
            Token rParentToken = match(TokenType.RPARENT);
            return new UnaryExpNode(identToken, lParentToken, rParentToken, funcRParamsNode);
        } else {
            PrimaryExpNode primaryExpNode = PrimaryExp();
            return new UnaryExpNode(primaryExpNode);
        }
    }

    private UnaryOpNode UnaryOp() {
        // UnaryOp → '+' | '−' | '!'
        Token op = null;
        if (tokens.get(index).getType() == TokenType.NOT) {
            op = match(TokenType.NOT);
        } else if (tokens.get(index).getType() == TokenType.PLUS) {
            op = match(TokenType.PLUS);
        } else {
            op = match(TokenType.MINU);
        }
        return new UnaryOpNode(op);
    }

    private VarDeclNode VarDecl() {
        // VarDecl → BType VarDef { ',' VarDef } ';'
        BTypeNode bTypeNode = BType();
        List<VarDefNode> varDefNodes = new ArrayList<>();
        List<Token> commaTokens = new ArrayList<>();
        varDefNodes.add(VarDef());
        while (tokens.get(index).getType() == TokenType.COMMA) {
            commaTokens.add(match(TokenType.COMMA));
            varDefNodes.add(VarDef());
        }
        Token semicnToken = match(TokenType.SEMICN);
        return new VarDeclNode(bTypeNode, varDefNodes, commaTokens, semicnToken);
    }

    private VarDefNode VarDef() {
        // VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
        Token ident = match(TokenType.IDENFR);
        Token lBrackToken = null;
        ConstExpNode constExpNode = null;
        Token rBrackToken = null;
        if (tokens.get(index).getType() == TokenType.LBRACK) {
            lBrackToken = match(TokenType.LBRACK);
            constExpNode = ConstExp();
            rBrackToken = match(TokenType.RBRACK);
        }
        Token assignToken = null;
        InitValNode initValNode = null;
        if (tokens.get(index).getType() == TokenType.ASSIGN) {
            assignToken = match(TokenType.ASSIGN);
            initValNode = InitVal();
        }
        return new VarDefNode(ident, lBrackToken, rBrackToken, constExpNode, assignToken, initValNode);
    }

    public List<Error> getErrors() {
        errors.sort(Comparator.comparingInt(Error::getLineNum));
        return errors;
    }
}
