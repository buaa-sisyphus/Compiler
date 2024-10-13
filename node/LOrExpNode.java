package node;

import token.Token;
import utils.IOUtils;

/* LOrExp → LAndExp | LOrExp '||' LAndExp
 * ↓
 * LOrExp → LAndExp ['||' LOrExp]
 */
public class LOrExpNode extends Node {
    private LAndExpNode lAndExpNode;
    private LOrExpNode lOrExpNode;
    private Token op;

    public LOrExpNode(LAndExpNode lAndExpNode, LOrExpNode lOrExpNode, Token op) {
        this.lAndExpNode = lAndExpNode;
        this.lOrExpNode = lOrExpNode;
        this.op = op;
        this.type=NodeType.LOrExp;
    }

    @Override
    public void print() {
        lAndExpNode.print();
        IOUtils.write(typeToString());
        if(lOrExpNode != null){
            IOUtils.write(op.toString());
            lOrExpNode.print();
        }
    }

    public LOrExpNode getlOrExpNode() {
        return lOrExpNode;
    }

    public LAndExpNode getlAndExpNode() {
        return lAndExpNode;
    }
}
