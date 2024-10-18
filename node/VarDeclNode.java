package node;

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
            IOUtils.writeSymbol(commaTokens.get(i - 1).toString());
            varDefNodes.get(i).print();
        }
        IOUtils.writeSymbol(semicnToken.toString());
        IOUtils.writeSymbol(typeToString());
    }

    public BTypeNode getbTypeNode() {
        return bTypeNode;
    }

    public List<Token> getCommaTokens() {
        return commaTokens;
    }

    public List<VarDefNode> getVarDefNodes() {
        return varDefNodes;
    }
}
