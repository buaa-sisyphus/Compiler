package node;

import frontend.Parser;
import token.Token;
import token.TokenType;
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
}
