package node;

import frontend.Parser;
import symbol.FuncParam;
import token.Token;
import token.TokenType;
import utils.IOUtils;

import java.util.List;

// FuncRParams → Exp { ',' Exp }
public class FuncRParamsNode extends Node {
    private List<ExpNode> expNodes;
    private List<Token> commaTokens;

    public FuncRParamsNode(List<ExpNode> expNodes, List<Token> commaTokens) {
        this.expNodes = expNodes;
        this.commaTokens = commaTokens;
        this.type = NodeType.FuncRParams;
    }

    @Override
    public void print() {
        expNodes.get(0).print();
        for (int i = 1; i < expNodes.size(); i++) {
            IOUtils.write(commaTokens.get(i - 1).toString());
            expNodes.get(i).print();
        }
        IOUtils.write(typeToString());
    }

    public boolean matchParamsCount(int give) {
        return give==expNodes.size();
    }

    public boolean matchParams(List<FuncParam> params) {
        for (int i = 0; i < params.size(); i++) {
            FuncParam param = params.get(i);
            ExpNode expNode = expNodes.get(i);
            expNode.matchParam(param.getType());
        }
        return false;
    }
}
