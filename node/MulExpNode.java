package node;

import token.Token;
import utils.IOUtils;

/* MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
 * ↓
 * MulExp → UnaryExp [('*' | '/' | '%') MulExp]
 */
public class MulExpNode extends Node {
    private UnaryExpNode unaryExpNode;
    private MulExpNode mulExpNode;
    private Token op;

    public MulExpNode(UnaryExpNode unaryExpNode, MulExpNode mulExpNode, Token op) {
        this.unaryExpNode = unaryExpNode;
        this.mulExpNode = mulExpNode;
        this.op = op;
        this.type = NodeType.MulExp;
    }

    @Override
    public void print() {
        unaryExpNode.print();
        IOUtils.writeSymbol(typeToString());
        if (mulExpNode != null) {
            IOUtils.writeSymbol(op.toString());
            mulExpNode.print();
        }
    }

    public String getType() {
        if (op != null) {
            return "0";
        } else {
            return unaryExpNode.getType();
        }
    }

    public MulExpNode getMulExpNode() {
        return mulExpNode;
    }

    public UnaryExpNode getUnaryExpNode() {
        return unaryExpNode;
    }
}
