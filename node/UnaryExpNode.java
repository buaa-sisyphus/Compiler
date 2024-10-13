package node;

import error.ErrorHandler;
import error.ErrorType;
import frontend.Parser;
import symbol.*;
import symbol.Symbol.SymbolType;
import token.Token;
import token.TokenType;
import utils.IOUtils;

import java.util.List;

// UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
public class UnaryExpNode extends Node {
    private PrimaryExpNode primaryExpNode = null;
    private Token ident = null;
    private Token lParent = null;
    private Token rParent = null;
    private FuncRParamsNode funcRParamsNode = null;
    private UnaryOpNode unaryOpNode = null;
    private UnaryExpNode unaryExpNode = null;

    public UnaryExpNode(PrimaryExpNode primaryExpNode) {
        this.primaryExpNode = primaryExpNode;
        this.type = NodeType.UnaryExp;
    }

    public UnaryExpNode(Token ident, Token lParent, Token rParent, FuncRParamsNode funcRParamsNode) {
        this.ident = ident;
        this.lParent = lParent;
        this.rParent = rParent;
        this.funcRParamsNode = funcRParamsNode;
        this.type = NodeType.UnaryExp;
    }

    public UnaryExpNode(UnaryOpNode unaryOpNode, UnaryExpNode unaryExpNode) {
        this.unaryOpNode = unaryOpNode;
        this.unaryExpNode = unaryExpNode;
        this.type = NodeType.UnaryExp;
    }

    @Override
    public void print() {
        if (primaryExpNode != null) {
            primaryExpNode.print();
        } else if (ident != null) {
            IOUtils.write(ident.toString());
            IOUtils.write(lParent.toString());
            if (funcRParamsNode != null) {
                funcRParamsNode.print();
            }
            IOUtils.write(rParent.toString());
        } else {
            unaryOpNode.print();
            unaryExpNode.print();
        }
        IOUtils.write(typeToString());
    }

    public void fill(SymbolTable table) {
        if (primaryExpNode != null) {
            primaryExpNode.fill(table);
        } else if (ident != null) {
            Symbol symbol = table.getSymbolDeep(ident.getContent());
            if (symbol != null && symbol instanceof FuncSymbol) {
                FuncSymbol funcSymbol = (FuncSymbol) symbol;
                int give = funcSymbol.getParamsCount();
                List<FuncParam> params = funcSymbol.getParams();
                if (funcRParamsNode != null) {
                    if (!funcRParamsNode.matchParamsCount(give)) {
                        ErrorHandler.getInstance().addError(ErrorType.d, ident.getLineNum());
                    } else if (!funcRParamsNode.matchParams(params, table)) {
                        ErrorHandler.getInstance().addError(ErrorType.e, ident.getLineNum());
                    }
                } else {
                    if (give != 0) {
                        ErrorHandler.getInstance().addError(ErrorType.d, ident.getLineNum());
                    }
                }
            } else {
                ErrorHandler.getInstance().addError(ErrorType.c, ident.getLineNum());
            }
        } else {
            //todo unaryOp
            unaryExpNode.fill(table);
        }
    }

    public String getType() {
        if (unaryOpNode != null) {
            return "var";
        } else if (primaryExpNode != null) {
            return primaryExpNode.getType();
        } else {
            return ident.getContent();
        }
    }
}
