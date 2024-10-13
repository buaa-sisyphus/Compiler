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
            IOUtils.write(commaTokens.get(i - 1).toString());
            expNodes.get(i).print();
        }
        IOUtils.write(typeToString());
    }

    public boolean matchParamsCount(int give) {
        return give == expNodes.size();
    }

    public boolean matchParams(List<FuncParam> params, SymbolTable table) {
        for (int i = 0; i < params.size(); i++) {
            String paramType = params.get(i).toString();
            ExpNode expNode = expNodes.get(i);
            String tmp = expNode.getType();
            if (tmp.equals("0")) {
                //立即数
                if (paramType.contains("Array")) {
                    return false;
                }
            } else {
                String symbolType = table.getSymbolDeep(tmp).toType();
                if (symbolType.contains("Array") && paramType.contains("Array")) {
                    if ((symbolType.contains("Int") && paramType.contains("Char")) || (symbolType.contains("Char") && paramType.contains("Int"))) {
                        return false;
                    }
                } else if ((symbolType.contains("Array") && !paramType.contains("Array")) || (!symbolType.contains("Array") && paramType.contains("Array"))) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<ExpNode> getExpNodes() {
        return expNodes;
    }
}
