package node;

import error.ErrorHandler;
import error.ErrorType;
import frontend.Parser;
import symbol.ArraySymbol;
import symbol.SymbolTable;
import token.Token;
import token.TokenType;
import utils.IOUtils;

// FuncFParam → BType Ident ['[' ']']
public class FuncFParamNode extends Node {
    private BTypeNode bTypeNode;
    private Token ident;
    private Token lBrackToken;
    private Token rBrackToken;

    public FuncFParamNode(BTypeNode bTypeNode, Token ident, Token lBrackToken, Token rBrackToken) {
        this.bTypeNode = bTypeNode;
        this.ident = ident;
        this.lBrackToken = lBrackToken;
        this.rBrackToken = rBrackToken;
        this.type=NodeType.FuncFParam;
    }

    @Override
    public void print() {
        bTypeNode.print();
        IOUtils.write(ident.toString());
        if(lBrackToken != null) {
            IOUtils.write(lBrackToken.toString());
            IOUtils.write(rBrackToken.toString());
        }
        IOUtils.write(typeToString());
    }

    public void fill(SymbolTable table) {
        if(table.findSymbol(ident.getContent())){
            ErrorHandler.getInstance().addError(ErrorType.b,ident.getLineNum());
        }else{
            boolean isConst=false;
            boolean isInt= bTypeNode.isInt();
            boolean isArray=false;
            if (rBrackToken != null && lBrackToken != null) {
                isArray=true;
            }
            ArraySymbol arraySymbol = new ArraySymbol();
            arraySymbol.set(ident,table.getScopeNum(),isInt,isArray,isConst);
            table.addSymbol(arraySymbol.getName(),arraySymbol);
        }
    }
}
