package node;

import token.Token;
import utils.IOUtils;

// FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
public class FuncDefNode extends Node {
    private FuncTypeNode funcTypeNode;
    private Token ident;
    private Token lParent;
    private FuncFParamsNode funcFParamsNode;
    private Token rParent;
    private BlockNode blockNode;

    public FuncDefNode(FuncTypeNode funcTypeNode, Token ident, Token lParent, FuncFParamsNode funcFParamsNode, Token rParent, BlockNode blockNode) {
        this.funcTypeNode = funcTypeNode;
        this.ident = ident;
        this.lParent = lParent;
        this.funcFParamsNode = funcFParamsNode;
        this.rParent = rParent;
        this.blockNode = blockNode;
        this.type = NodeType.FuncDef;
    }

    public void print() {
        funcTypeNode.print();
        IOUtils.write(ident.toString());
        IOUtils.write(lParent.toString());
        if (funcFParamsNode != null) {
            funcFParamsNode.print();
        }
        IOUtils.write(rParent.toString());
        blockNode.print();
        IOUtils.write(typeToString());
    }

    public Token getIdent() {
        return ident;
    }

    public BlockNode getBlockNode() {
        return blockNode;
    }

    public FuncFParamsNode getFuncFParamsNode() {
        return funcFParamsNode;
    }

    public FuncTypeNode getFuncTypeNode() {
        return funcTypeNode;
    }
}
