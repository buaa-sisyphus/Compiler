package node;

import symbol.FuncParam;
import symbol.SymbolTable;
import token.Token;
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
            IOUtils.writeSymbol(commaTokens.get(i - 1).toString());
            expNodes.get(i).print();
        }
        IOUtils.writeSymbol(typeToString());
    }

    public List<ExpNode> getExpNodes() {
        return expNodes;
    }
}
