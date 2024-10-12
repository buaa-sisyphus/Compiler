package node;


import error.ErrorHandler;
import error.ErrorType;
import frontend.Parser;
import symbol.ArraySymbol;
import symbol.SymbolTable;
import token.Token;
import utils.IOUtils;

import java.util.List;

// ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
public class ConstDeclNode extends Node {
    private Token constToken;
    private BTypeNode bTypeNode;
    private List<ConstDefNode> constDefNodes;
    private List<Token> commaTokens;
    private Token semicnToken;

    public ConstDeclNode(Token constToken, BTypeNode bTypeNode, List<ConstDefNode> constDefNodes, List<Token> commaTokens, Token semicnToken) {
        this.constToken = constToken;
        this.bTypeNode = bTypeNode;
        this.constDefNodes = constDefNodes;
        this.commaTokens = commaTokens;
        this.semicnToken = semicnToken;
        this.type = NodeType.ConstDecl;
    }

    public void print() {
        IOUtils.write(constToken.toString());
        bTypeNode.print();
        constDefNodes.get(0).print();
        for (int i = 1; i < constDefNodes.size(); i++) {
            IOUtils.write(commaTokens.get(i-1).toString());
            constDefNodes.get(i).print();
        }
        IOUtils.write(semicnToken.toString());
        IOUtils.write(typeToString());
    }

    public void fill(SymbolTable table){
        boolean isConst=true;
        boolean isInt=bTypeNode.isInt();
        for (int i = 0; i < constDefNodes.size(); i++) {
            ConstDefNode constDefNode = constDefNodes.get(i);
            Token ident=constDefNode.getIdent();
            if(table.findSymbol(ident.getContent())){
                ErrorHandler.getInstance().addError(ErrorType.b,ident.getLineNum());
            }else{
                ArraySymbol arraySymbol = new ArraySymbol();
                boolean isArray = constDefNode.isArray();
                arraySymbol.set(ident, table.getScopeNum(),isInt,isArray,isConst);
                table.addSymbol(arraySymbol.getName(),arraySymbol);
            }
        }
    }
}
