package node;

import token.Token;
import utils.IOUtils;

// ForStmt → LVal '=' Exp
public class ForStmtNode extends Node{
    private LValNode lValNode;
    private ExpNode expNode;
    private Token assignToken;

    public ForStmtNode(LValNode lValNode, ExpNode expNode, Token assignToken) {
        this.lValNode = lValNode;
        this.expNode = expNode;
        this.assignToken = assignToken;
        this.type=NodeType.ForStmt;
    }

    @Override
    public void print() {
        lValNode.print();
        IOUtils.write(assignToken.toString());
        expNode.print();
        IOUtils.write(typeToString());
    }

    public ExpNode getExpNode() {
        return expNode;
    }

    public LValNode getlValNode() {
        return lValNode;
    }
}
