package node;

import token.Token;
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
        this.type = NodeType.FuncFParam;
    }

    @Override
    public void print() {
        bTypeNode.print();
        IOUtils.writeSymbol(ident.toString());
        if (lBrackToken != null) {
            IOUtils.writeSymbol(lBrackToken.toString());
            IOUtils.writeSymbol(rBrackToken.toString());
        }
        IOUtils.writeSymbol(typeToString());
    }

    public BTypeNode getbTypeNode() {
        return bTypeNode;
    }

    public Token getIdent() {
        return ident;
    }

    public int isArray() {
        if (lBrackToken != null) {
            return 1;
        }
        return 0;
    }
}
