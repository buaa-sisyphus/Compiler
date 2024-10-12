package node;

import symbol.Symbol.SymbolType;
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
        IOUtils.write(typeToString());
        if (addExpNode != null) {
            IOUtils.write(op.toString());
            addExpNode.print();
        }
    }

    public void matchParam(SymbolType type) {
        mulExpNode.matchParam(type);
    }
}
