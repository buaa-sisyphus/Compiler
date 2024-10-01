package node;

import frontend.Parser;
import token.Token;
import token.TokenType;
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
        this.type=NodeType.LVal;
    }

    @Override
    public void print() {
        IOUtils.write(ident.toString());
        if (expNode != null) {
            IOUtils.write(lBrackToken.toString());
            expNode.print();
            IOUtils.write(rBrackToken.toString());
        }
        IOUtils.write(Parser.nodeType.get(type));
    }
}
