package node;

import frontend.Parser;
import token.Token;
import token.TokenType;
import utils.IOUtils;

import java.util.List;

// ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
public class ConstInitValNode extends Node {
    private ConstExpNode constExp;
    private List<ConstExpNode> constExpNodes;
    private Token stringConst;
    private Token lBraceToken;
    private Token rBraceToken;
    private List<Token> commaTokens;

    public ConstInitValNode(ConstExpNode constExp) {
        this.constExp = constExp;
        this.type=NodeType.ConstInitVal;
    }

    public ConstInitValNode(Token stringConst) {
        this.stringConst = stringConst;
        this.type=NodeType.ConstInitVal;
    }

    public ConstInitValNode(List<ConstExpNode> constExpNodes, Token lBraceToken, Token rBraceToken, List<Token> commaTokens) {
        this.constExpNodes = constExpNodes;
        this.lBraceToken = lBraceToken;
        this.rBraceToken = rBraceToken;
        this.commaTokens = commaTokens;
        this.type=NodeType.ConstInitVal;
    }

    @Override
    public void print() {
        if(stringConst != null) {
            IOUtils.write(stringConst.toString());
        }else if(constExp != null) {
            constExp.print();
        }else{
            IOUtils.write(lBraceToken.toString());
            if(!constExpNodes.isEmpty()){
                constExpNodes.get(0).print();
                for(int i = 1; i < constExpNodes.size(); i++){
                    IOUtils.write(commaTokens.get(i-1).toString());
                    constExpNodes.get(i).print();
                }
            }
            IOUtils.write(rBraceToken.toString());
        }
        IOUtils.write(typeToString());
    }
}
