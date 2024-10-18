package node;

import utils.IOUtils;

// Cond → LOrExp
public class CondNode extends Node {
    private LOrExpNode lOrExpNode;

    public CondNode(LOrExpNode lOrExpNode) {
        this.lOrExpNode = lOrExpNode;
        this.type=NodeType.Cond;
    }

    @Override
    public void print() {
        lOrExpNode.print();
        IOUtils.writeSymbol(typeToString());
    }

    public LOrExpNode getlOrExpNode() {
        return lOrExpNode;
    }
}
