package node;

import frontend.Parser;
import symbol.SymbolTable;
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
        IOUtils.write(typeToString());
    }

    public void fill(SymbolTable table) {
        lOrExpNode.fill(table);
    }
}
