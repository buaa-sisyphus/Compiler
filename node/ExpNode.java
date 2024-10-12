package node;

import symbol.Symbol.SymbolType;
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
        IOUtils.write(typeToString());
    }

    public void matchParam(SymbolType type) {
        addExpNode.matchParam(type);
    }
}
