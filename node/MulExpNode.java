package node;

import frontend.Parser;
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
        IOUtils.write(Parser.nodeType.get(type));
        if (mulExpNode != null) {
            IOUtils.write(op.toString());
            mulExpNode.print();
        }
    }
}
