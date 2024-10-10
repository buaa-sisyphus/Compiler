package node;


import frontend.Parser;
import token.Token;
import token.TokenType;
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
}
