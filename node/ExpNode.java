package node;

import symbol.FuncParam;
import symbol.Symbol.SymbolType;
import symbol.SymbolTable;
import utils.IOUtils;

// Exp → AddExp
public class ExpNode extends Node {
    private AddExpNode addExpNode;

    public ExpNode(AddExpNode addExpNode) {
        this.addExpNode = addExpNode;
        this.type = NodeType.Exp;
    }

    @Override
    public void print() {
        addExpNode.print();
        IOUtils.write(typeToString());
    }

    public String getType() {
        return addExpNode.getType();
    }

    public void fill(SymbolTable table) {
        addExpNode.fill(table);
    }
}
