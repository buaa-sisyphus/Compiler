package node;

import frontend.Parser;
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
        IOUtils.write(Parser.nodeType.get(type));
        if(lOrExpNode != null){
            IOUtils.write(op.toString());
            lOrExpNode.print();
        }
    }
}
