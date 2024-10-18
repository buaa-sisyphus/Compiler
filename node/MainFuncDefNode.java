package node;

import token.Token;
import utils.IOUtils;

// MainFuncDef → 'int' 'main' '(' ')' Block
public class MainFuncDefNode extends Node{
    private Token intToken;
    private Token mainToken;
    private Token lParent;
    private Token rParent;
    private BlockNode block;

    public MainFuncDefNode(Token intToken, Token mainToken, Token lParent, Token rParent, BlockNode block) {
        this.intToken = intToken;
        this.mainToken = mainToken;
        this.lParent = lParent;
        this.rParent = rParent;
        this.block = block;
        this.type=NodeType.MainFuncDef;
    }

    public void print(){
        IOUtils.writeSymbol(intToken.toString());
        IOUtils.writeSymbol(mainToken.toString());
        IOUtils.writeSymbol(lParent.toString());
        IOUtils.writeSymbol(rParent.toString());
        block.print();
        IOUtils.writeSymbol(typeToString());
    }

    public BlockNode getBlock() {
        return block;
    }
}
