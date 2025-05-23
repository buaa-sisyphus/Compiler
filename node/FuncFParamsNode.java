package node;

import token.Token;
import utils.IOUtils;

import java.util.List;

// FuncFParams → FuncFParam { ',' FuncFParam }
public class FuncFParamsNode extends Node {
    private List<FuncFParamNode> funcFParamNodes;
    private List<Token> commaTokens;

    public FuncFParamsNode(List<FuncFParamNode> funcFParamNodes, List<Token> commaTokens) {
        this.funcFParamNodes = funcFParamNodes;
        this.commaTokens = commaTokens;
        this.type = NodeType.FuncFParams;
    }

    @Override
    public void print() {
        funcFParamNodes.get(0).print();
        for (int i = 1; i < funcFParamNodes.size(); i++) {
            IOUtils.writeSymbol(commaTokens.get(i-1).toString());
            funcFParamNodes.get(i).print();
        }
        IOUtils.writeSymbol(typeToString());
    }

    public List<FuncFParamNode> getFuncFParamNodes() {
        return funcFParamNodes;
    }
}
