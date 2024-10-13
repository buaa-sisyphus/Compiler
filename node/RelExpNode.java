package node;

import symbol.SymbolTable;
import token.Token;
import utils.IOUtils;

/* RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
 *  ↓
 *  RelExp → AddExp [('<' | '>' | '<=' | '>=') RelExp]
 */
public class RelExpNode extends Node{
    private AddExpNode addExpNode;
    private RelExpNode relExpNode;
    private Token op;

    public RelExpNode(AddExpNode addExpNode, RelExpNode relExpNode, Token op) {
        this.addExpNode = addExpNode;
        this.relExpNode = relExpNode;
        this.op = op;
        this.type=NodeType.RelExp;
    }

    @Override
    public void print() {
        addExpNode.print();
        IOUtils.write(typeToString());
        if (relExpNode != null) {
            IOUtils.write(op.toString());
            relExpNode.print();
        }
    }

    public void fill(SymbolTable table) {
        addExpNode.fill(table);
        if (relExpNode != null) {
            relExpNode.fill(table);
        }
    }
}
