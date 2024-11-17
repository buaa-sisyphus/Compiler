package node;

import token.Token;
import token.TokenType;
import utils.IOUtils;

/* AddExp → MulExp | AddExp ('+' | '−') MulExp
 * ↓
 * AddExp -> MulExp [('+' | '-') AddExp]
 */
public class AddExpNode extends Node {
    private MulExpNode mulExpNode;
    private AddExpNode addExpNode;
    private Token op;

    public AddExpNode(MulExpNode mulExpNode, AddExpNode addExpNode, Token op) {
        this.mulExpNode = mulExpNode;
        this.addExpNode = addExpNode;
        this.op = op;
        this.type = NodeType.AddExp;
    }

    public void print() {
        mulExpNode.print();
        IOUtils.writeSymbol(typeToString());
        if (addExpNode != null) {
            IOUtils.writeSymbol(op.toString());
            addExpNode.print();
        }
    }

    public String getType() {
        if (op != null) {
            return "0";
        } else {
            return mulExpNode.getType();
        }
    }

    public TokenType getOpType() {
        if (op != null) {
            return op.getType();
        }else return null;
    }

    public AddExpNode getAddExpNode() {
        return addExpNode;
    }

    public MulExpNode getMulExpNode() {
        return mulExpNode;
    }
}
