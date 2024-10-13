package node;

// BlockItem → Decl | Stmt
public class BlockItemNode extends Node {
    private DeclNode declNode;
    private StmtNode stmtNode;

    public BlockItemNode(DeclNode declNode, StmtNode stmtNode) {
        this.declNode = declNode;
        this.stmtNode = stmtNode;
        this.type = NodeType.BlockItem;
    }

    @Override
    public void print() {
        if (declNode != null) {
            declNode.print();
        } else {
            stmtNode.print();
        }
    }

    public DeclNode getDeclNode() {
        return declNode;
    }

    public StmtNode getStmtNode() {
        return stmtNode;
    }
}
