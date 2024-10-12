package node;

import error.ErrorHandler;
import error.ErrorType;
import frontend.Parser;
import symbol.ArraySymbol;
import symbol.SymbolTable;
import token.Token;
import utils.IOUtils;

import java.util.List;

// VarDecl → BType VarDef { ',' VarDef } ';'
public class VarDeclNode extends Node {
    private BTypeNode bTypeNode;
    private List<VarDefNode> varDefNodes;
    private List<Token> commaTokens;
    private Token semicnToken;

    public VarDeclNode(BTypeNode bTypeNode, List<VarDefNode> varDefNodes, List<Token> commaTokens, Token semicnToken) {
        this.bTypeNode = bTypeNode;
        this.varDefNodes = varDefNodes;
        this.commaTokens = commaTokens;
        this.semicnToken = semicnToken;
        this.type = NodeType.VarDecl;
    }

    public void print() {
        bTypeNode.print();
        varDefNodes.get(0).print();
        for (int i = 1; i < varDefNodes.size(); i++) {
            IOUtils.write(commaTokens.get(i-1).toString());
            varDefNodes.get(i).print();
        }
        IOUtils.write(semicnToken.toString());
        IOUtils.write(typeToString());
    }

    public void fill(SymbolTable table){
        boolean isConst=false;
        boolean isInt=bTypeNode.isInt();
        for (int i = 0; i < varDefNodes.size(); i++) {
            VarDefNode varDefNode = varDefNodes.get(i);
            Token ident=varDefNode.getIdent();
            if(table.findSymbol(ident.getContent())){
                ErrorHandler.getInstance().addError(ErrorType.b,ident.getLineNum());
            }else{
                ArraySymbol arraySymbol = new ArraySymbol();
                boolean isArray = varDefNode.isArray();
                arraySymbol.set(ident, table.getScopeNum(),isInt,isArray,isConst);
                table.addSymbol(arraySymbol.getName(),arraySymbol);
            }
        }
    }
}
