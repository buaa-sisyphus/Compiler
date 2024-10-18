package node;

import token.Token;
import utils.IOUtils;

// BType → 'int' | 'char'
public class BTypeNode extends Node{
    private Token token;

    public BTypeNode(Token token) {
        this.token = token;
        this.type=NodeType.BType;
    }

    public void print() {
        IOUtils.writeSymbol(token.toString());
    }

    public Token getToken() {
        return token;
    }

}
