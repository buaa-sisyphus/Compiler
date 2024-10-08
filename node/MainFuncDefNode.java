package node;

import frontend.Parser;
import token.Token;
import token.TokenType;
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
        IOUtils.write(intToken.toString());
        IOUtils.write(mainToken.toString());
        IOUtils.write(lParent.toString());
        IOUtils.write(rParent.toString());
        block.print();
        IOUtils.write(typeToString());
    }
}
