package node;

import frontend.Parser;
import token.Token;
import token.TokenType;
import utils.IOUtils;

import java.util.List;

// FuncRParams → Exp { ',' Exp }
public class FuncRParamsNode extends Node {
    private List<ExpNode> expNodes;
    private List<Token> commaTokens;
    public FuncRParamsNode(List<ExpNode> expNodes,List<Token> commaTokens) {
        this.expNodes = expNodes;
        this.commaTokens = commaTokens;
        this.type=NodeType.FuncRParams;
    }

    @Override
    public void print() {
        expNodes.get(0).print();
        for (int i = 1; i < expNodes.size(); i++) {
            IOUtils.write(commaTokens.get(i-1).toString());
            expNodes.get(i).print();
        }
        IOUtils.write(Parser.nodeType.get(type));
    }
}
