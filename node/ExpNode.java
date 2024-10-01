package node;

import frontend.Parser;
import utils.IOUtils;

// Exp → AddExp
public class ExpNode extends Node {
    private AddExpNode addExpNode;

    public ExpNode(AddExpNode addExpNode) {
        this.addExpNode = addExpNode;
        this.type=NodeType.Exp;
    }

    @Override
    public void print() {
        addExpNode.print();
        IOUtils.write(Parser.nodeType.get(type));
    }
}
