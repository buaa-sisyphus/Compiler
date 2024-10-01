package node;

import frontend.Parser;
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
        IOUtils.write(Parser.nodeType.get(type));
    }
}