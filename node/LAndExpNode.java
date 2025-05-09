package node;

import token.Token;
import utils.IOUtils;

/* LAndExp → EqExp | LAndExp '&&' EqExp
 * ↓
 * LAndExp → EqExp ['&&' LAndExp]
 */
public class LAndExpNode extends Node{
    private EqExpNode eqExpNode;
    private LAndExpNode lAndExpNode;
    private Token op;

    public LAndExpNode(EqExpNode eqExpNode, LAndExpNode lAndExpNode, Token op) {
        this.eqExpNode = eqExpNode;
        this.lAndExpNode = lAndExpNode;
        this.op = op;
        this.type=NodeType.LAndExp;
    }

    @Override
    public void print() {
        eqExpNode.print();
        IOUtils.writeSymbol(typeToString());
        if (lAndExpNode != null) {
            IOUtils.writeSymbol(op.toString());
            lAndExpNode.print();
        }
    }

    public LAndExpNode getlAndExpNode() {
        return lAndExpNode;
    }

    public EqExpNode getEqExpNode() {
        return eqExpNode;
    }
}
