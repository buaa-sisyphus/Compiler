package node;

import frontend.Parser;
import token.Token;
import token.TokenType;
import utils.IOUtils;

import java.util.List;

// VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
public class VarDefNode extends Node {
    private Token ident;
    private Token lBrackToken;
    private Token rBrackToken;
    private ConstExpNode constExpNode;
    private Token assignToken;
    private InitValNode initValNode;

    public VarDefNode(Token ident, Token lBrackToken,Token rBrackToken,ConstExpNode constExpNode, Token assignToken,InitValNode initValNode) {
        this.ident = ident;
        this.lBrackToken = lBrackToken;
        this.rBrackToken = rBrackToken;
        this.assignToken = assignToken;
        this.constExpNode = constExpNode;
        this.initValNode = initValNode;
        this.type=NodeType.VarDef;
    }

    @Override
    public void print() {
        IOUtils.write(ident.toString());
        if(lBrackToken!=null){
            IOUtils.write(lBrackToken.toString());
            constExpNode.print();
            IOUtils.write(rBrackToken.toString());
        }
        if(initValNode != null) {
            IOUtils.write(assignToken.toString());
            initValNode.print();
        }
        IOUtils.write(typeToString());
    }
}
