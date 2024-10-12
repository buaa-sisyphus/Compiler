package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

// UnaryOp → '+' | '−' | '!'
public class UnaryOpNode extends Node {
    private Token op;

    public UnaryOpNode(Token op) {
        this.op = op;
        this.type=NodeType.UnaryOp;
    }

    @Override
    public void print() {
        IOUtils.write(op.toString());
        IOUtils.write(typeToString());
    }

    public Token getOp() {
        return op;
    }
}
