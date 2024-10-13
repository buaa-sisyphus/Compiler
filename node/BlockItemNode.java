package node;

import error.ErrorHandler;
import error.ErrorType;
import symbol.SymbolTable;

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

    public void fill(SymbolTable table) {
        if (declNode != null) {
            declNode.fill(table);
        } else {
            stmtNode.fill(table);
        }
    }

    public void fill(SymbolTable table, int endLine, boolean needReturn) {
        if (needReturn && (stmtNode == null || stmtNode.getStmtType() != StmtNode.StmtType.Return)) {
            ErrorHandler.getInstance().addError(ErrorType.g, endLine);
        }
        if (!needReturn && stmtNode != null) {
            stmtNode.handleReturn(table);
            return;
        }
        fill(table);
    }

}
