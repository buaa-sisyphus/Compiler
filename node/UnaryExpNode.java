package node;

import frontend.Parser;
import token.Token;
import token.TokenType;
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
            IOUtils.write(ident.toString());
            IOUtils.write(lParent.toString());
            if (funcRParamsNode != null) {
                funcRParamsNode.print();
            }
            IOUtils.write(rParent.toString());
        } else {
            unaryOpNode.print();
            unaryExpNode.print();
        }
        IOUtils.write(Parser.nodeType.get(type));
    }
}
