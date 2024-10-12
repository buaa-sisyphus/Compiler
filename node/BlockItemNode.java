package node;

import symbol.SymbolTable;

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

    public void fill(SymbolTable table){
        if(declNode != null) {
            declNode.fill(table);
        }else{
            stmtNode.fill(table);
        }
    }
}
