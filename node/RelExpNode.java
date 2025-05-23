package node;

import token.Token;
import token.TokenType;
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
        IOUtils.writeSymbol(typeToString());
        if (relExpNode != null) {
            IOUtils.writeSymbol(op.toString());
            relExpNode.print();
        }
    }

    public RelExpNode getRelExpNode() {
        return relExpNode;
    }

    public AddExpNode getAddExpNode() {
        return addExpNode;
    }

    public TokenType getOpType(){
        return op.getType();
    }
}
