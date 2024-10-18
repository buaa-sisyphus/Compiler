package node;

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
        IOUtils.writeSymbol(constToken.toString());
        bTypeNode.print();
        constDefNodes.get(0).print();
        for (int i = 1; i < constDefNodes.size(); i++) {
            IOUtils.writeSymbol(commaTokens.get(i - 1).toString());
            constDefNodes.get(i).print();
        }
        IOUtils.writeSymbol(semicnToken.toString());
        IOUtils.writeSymbol(typeToString());
    }

    public List<ConstDefNode> getConstDefNodes() {
        return constDefNodes;
    }

    public BTypeNode getbTypeNode() {
        return bTypeNode;
    }
}
