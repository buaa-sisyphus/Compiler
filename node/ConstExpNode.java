package node;

import utils.IOUtils;

//ConstExp → AddExp
public class ConstExpNode extends Node {
    private AddExpNode addExpNode;

    public ConstExpNode(AddExpNode addExpNode) {
        this.addExpNode = addExpNode;
        this.type=NodeType.ConstExp;
    }

    @Override
    public void print() {
        addExpNode.print();
        IOUtils.writeSymbol(typeToString());
    }

    public AddExpNode getAddExpNode() {
        return addExpNode;
    }
}
