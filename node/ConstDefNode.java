package node;

import error.ErrorHandler;
import error.ErrorType;
import frontend.Parser;
import symbol.ArraySymbol;
import symbol.Symbol;
import symbol.SymbolTable;
import token.Token;
import token.TokenType;
import utils.IOUtils;

// ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal
public class ConstDefNode extends Node {
    private Token ident;
    private Token lBrackToken;
    private ConstExpNode constExpNode;
    private Token rBrackToken;
    private Token assignToken;
    private ConstInitValNode constInitValNode;

    public ConstDefNode(Token ident, Token lBrackToken, ConstExpNode constExpNode, Token rBrackToken, Token assignToken, ConstInitValNode constInitValNode) {
        this.ident = ident;
        this.lBrackToken = lBrackToken;
        this.constExpNode = constExpNode;
        this.rBrackToken = rBrackToken;
        this.assignToken = assignToken;
        this.constInitValNode = constInitValNode;
        this.type = NodeType.ConstDef;
    }

    @Override
    public void print() {
        IOUtils.write(ident.toString());
        if (constExpNode != null) {
            IOUtils.write(lBrackToken.toString());
            constExpNode.print();
            IOUtils.write(rBrackToken.toString());
        }
        IOUtils.write(assignToken.toString());
        constInitValNode.print();
        IOUtils.write(typeToString());
    }

    public void fill(SymbolTable table, boolean isInt) {
        Symbol symbol = table.getSymbol(ident.getContent());
        if (symbol != null) {
            ErrorHandler.getInstance().addError(ErrorType.b, ident.getLineNum());
        } else {
            ArraySymbol arraySymbol = new ArraySymbol();
            boolean isArray = (lBrackToken != null);
            arraySymbol.set(ident, table.getScopeNum(), isInt, isArray, true);
            table.addSymbol(arraySymbol.getName(), arraySymbol);
        }
        constInitValNode.fill(table);
    }

    public Token getIdent() {
        return ident;
    }
}
