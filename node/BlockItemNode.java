package node;

import frontend.Parser;
import utils.IOUtils;

// BlockItem → Decl | Stmt
public class BlockItemNode extends Node {
    private DeclNode declNode;
    private StmtNode stmtNode;

    public BlockItemNode(DeclNode declNode, StmtNode stmtNode) {
        this.declNode = declNode;
        this.stmtNode = stmtNode;
        this.type=NodeType.BlockItem;
    }

    @Override
    public void print() {
        if(declNode != null) {
            declNode.print();
        }else{
            stmtNode.print();
        }
    }
}
