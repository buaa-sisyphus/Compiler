package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

// FuncType → 'void' | 'int' | 'char'
public class FuncTypeNode extends Node {
    private Token token;

    public FuncTypeNode(Token token) {
        this.token = token;
        this.type = NodeType.FuncType;
    }

    @Override
    public void print() {
        IOUtils.write(token.toString());
        IOUtils.write(Parser.nodeType.get(type));
    }
}
