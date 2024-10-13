package node;

import error.ErrorHandler;
import error.ErrorType;
import frontend.Parser;
import symbol.Symbol;
import symbol.SymbolTable;
import token.Token;
import token.TokenType;
import utils.IOUtils;

// LVal → Ident ['[' Exp ']']
public class LValNode extends Node {
    private Token ident;
    private Token lBrackToken;
    private Token rBrackToken;
    private ExpNode expNode;

    public LValNode(Token ident, Token lBrackToken, Token rBrackToken, ExpNode expNode) {
        this.ident = ident;
        this.lBrackToken = lBrackToken;
        this.rBrackToken = rBrackToken;
        this.expNode = expNode;
        this.type = NodeType.LVal;
    }

    @Override
    public void print() {
        IOUtils.write(ident.toString());
        if (expNode != null) {
            IOUtils.write(lBrackToken.toString());
            expNode.print();
            IOUtils.write(rBrackToken.toString());
        }
        IOUtils.write(typeToString());
    }

    public void fill(SymbolTable table,boolean isAssign) {
        Symbol symbol = table.getSymbolDeep(ident.getContent());
        if (symbol != null) {
            if(symbol.getSymbolType().toString().contains("Const") && isAssign){
                ErrorHandler.getInstance().addError(ErrorType.h,ident.getLineNum());
            }
        } else {
            ErrorHandler.getInstance().addError(ErrorType.c, ident.getLineNum());
        }
    }

    public String getType() {
        if (lBrackToken != null) return "var";
        else return ident.getContent();
    }
}
