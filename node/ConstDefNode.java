package node;

import frontend.Parser;
import token.Token;
import token.TokenType;
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
        this.type=NodeType.ConstDef;
    }

    @Override
    public void print() {
        IOUtils.write(ident.toString());
        if(constExpNode != null){
            IOUtils.write(lBrackToken.toString());
            constExpNode.print();
            IOUtils.write(rBrackToken.toString());
        }
        IOUtils.write(assignToken.toString());
        constInitValNode.print();
        IOUtils.write(typeToString());
    }
}
