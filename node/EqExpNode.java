package node;

import token.Token;
import utils.IOUtils;

/* EqExp → RelExp | EqExp ('==' | '!=') RelExp
 * ↓
 * EqExp → RelExp [('==' | '!=') EqExp]
 */
public class EqExpNode extends Node {
    private RelExpNode relExpNode;
    private EqExpNode eqExpNode;
    private Token op;

    public EqExpNode(RelExpNode relExpNode, EqExpNode eqExpNode, Token op) {
        this.relExpNode = relExpNode;
        this.eqExpNode = eqExpNode;
        this.op = op;
        this.type = NodeType.EqExp;
    }

    @Override
    public void print() {
        relExpNode.print();
        IOUtils.write(typeToString());
        if (eqExpNode != null) {
            IOUtils.write(op.toString());
            eqExpNode.print();
        }
    }

    public EqExpNode getEqExpNode() {
        return eqExpNode;
    }

    public RelExpNode getRelExpNode() {
        return relExpNode;
    }
}
