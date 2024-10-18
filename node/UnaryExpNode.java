package node;

import token.Token;
import utils.IOUtils;

// UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
public class UnaryExpNode extends Node {
    private PrimaryExpNode primaryExpNode = null;
    private Token ident = null;
    private Token lParent = null;
    private Token rParent = null;
    private FuncRParamsNode funcRParamsNode = null;
    private UnaryOpNode unaryOpNode = null;
    private UnaryExpNode unaryExpNode = null;

    public UnaryExpNode(PrimaryExpNode primaryExpNode) {
        this.primaryExpNode = primaryExpNode;
        this.type = NodeType.UnaryExp;
    }

    public UnaryExpNode(Token ident, Token lParent, Token rParent, FuncRParamsNode funcRParamsNode) {
        this.ident = ident;
        this.lParent = lParent;
        this.rParent = rParent;
        this.funcRParamsNode = funcRParamsNode;
        this.type = NodeType.UnaryExp;
    }

    public UnaryExpNode(UnaryOpNode unaryOpNode, UnaryExpNode unaryExpNode) {
        this.unaryOpNode = unaryOpNode;
        this.unaryExpNode = unaryExpNode;
        this.type = NodeType.UnaryExp;
    }

    @Override
    public void print() {
        if (primaryExpNode != null) {
            primaryExpNode.print();
        } else if (ident != null) {
            IOUtils.writeSymbol(ident.toString());
            IOUtils.writeSymbol(lParent.toString());
            if (funcRParamsNode != null) {
                funcRParamsNode.print();
            }
            IOUtils.writeSymbol(rParent.toString());
        } else {
            unaryOpNode.print();
            unaryExpNode.print();
        }
        IOUtils.writeSymbol(typeToString());
    }

    public String getType() {
        if (primaryExpNode != null) {
            return primaryExpNode.getType();
        } else {
            return "0";
        }
    }

    public UnaryExpNode getUnaryExpNode() {
        return unaryExpNode;
    }

    public Token getIdent() {
        return ident;
    }

    public FuncRParamsNode getFuncRParamsNode() {
        return funcRParamsNode;
    }

    public PrimaryExpNode getPrimaryExpNode() {
        return primaryExpNode;
    }

    public UnaryOpNode getUnaryOpNode() {
        return unaryOpNode;
    }

}
