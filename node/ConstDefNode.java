package node;

import token.Token;
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
        IOUtils.writeSymbol(ident.toString());
        if (constExpNode != null) {
            IOUtils.writeSymbol(lBrackToken.toString());
            constExpNode.print();
            IOUtils.writeSymbol(rBrackToken.toString());
        }
        IOUtils.writeSymbol(assignToken.toString());
        constInitValNode.print();
        IOUtils.writeSymbol(typeToString());
    }

    public Token getIdent() {
        return ident;
    }

    public ConstExpNode getConstExpNode() {
        return constExpNode;
    }

    public ConstInitValNode getConstInitValNode() {
        return constInitValNode;
    }
}
