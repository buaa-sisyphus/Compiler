package node;

import frontend.Parser;
import symbol.FuncParam;
import symbol.Symbol;
import symbol.Symbol.SymbolType;
import symbol.SymbolTable;
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
        return give == expNodes.size();
    }

    public boolean matchParams(List<FuncParam> params, SymbolTable table) {
        for (int i = 0; i < params.size(); i++) {
            String paramType = params.get(i).getType().toString();
            ExpNode expNode = expNodes.get(i);
            String tmp = expNode.getType();
            if (tmp.equals("var")) {
                if (paramType.contains("Array")) {
                    return false;
                }
            } else {
                String symbolType = table.getSymbolDeep(tmp).getSymbolType().toString();
                if(symbolType.contains("Func")){
                    if(symbolType.contains("Void")){
                        return false;
                    }else{
                        if(paramType.contains("Array")){
                            return false;
                        }
                    }
                }else{
                    if (symbolType.contains("Array")) {
                        if (!paramType.contains("Array")) {
                            return false;
                        }
                        if (symbolType.contains("Char") && paramType.contains("Int")) {
                            return false;
                        }
                        if (symbolType.contains("Int") && paramType.contains("Char")) {
                            return false;
                        }
                    } else {
                        if (paramType.contains("Array")) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
