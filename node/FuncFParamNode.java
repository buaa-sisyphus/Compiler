package node;

import frontend.Parser;
import token.Token;
import token.TokenType;
import utils.IOUtils;

// FuncFParam → BType Ident ['[' ']']
public class FuncFParamNode extends Node {
    private BTypeNode bTypeNode;
    private Token ident;
    private Token lBrackToken;
    private Token rBrackToken;

    public FuncFParamNode(BTypeNode bTypeNode, Token ident, Token lBrackToken, Token rBrackToken) {
        this.bTypeNode = bTypeNode;
        this.ident = ident;
        this.lBrackToken = lBrackToken;
        this.rBrackToken = rBrackToken;
        this.type=NodeType.FuncFParam;
    }

    @Override
    public void print() {
        bTypeNode.print();
        IOUtils.write(ident.toString());
        if(lBrackToken != null) {
            IOUtils.write(lBrackToken.toString());
            IOUtils.write(rBrackToken.toString());
        }
        IOUtils.write(Parser.nodeType.get(type));
    }
}
