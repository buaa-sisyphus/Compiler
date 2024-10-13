package node;

import token.Token;
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

    public String getType() {
        if (lBrackToken != null) return "0";
        else return ident.getContent();
    }

    public ExpNode getExpNode() {
        return expNode;
    }

    public Token getIdent() {
        return ident;
    }
}
