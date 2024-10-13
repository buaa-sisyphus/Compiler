package node;

// Decl → ConstDecl | VarDecl
public class DeclNode extends Node {
    private ConstDeclNode constDeclNode;
    private VarDeclNode varDeclNode;

    public DeclNode(ConstDeclNode constDeclNode, VarDeclNode varDeclNode) {
        this.constDeclNode = constDeclNode;
        this.varDeclNode = varDeclNode;
        this.type = NodeType.Decl;
    }

    public void print() {
        if (constDeclNode != null) {
            constDeclNode.print();
        } else {
            varDeclNode.print();
        }
    }

    public ConstDeclNode getConstDeclNode() {
        return constDeclNode;
    }

    public VarDeclNode getVarDeclNode() {
        return varDeclNode;
    }
}
