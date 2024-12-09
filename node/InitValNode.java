package node;

import token.Token;
import utils.IOUtils;

import java.util.List;
import java.util.Map;

import static node.CharacterNode.ESCAPE_CHAR_MAP;

// InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst
public class InitValNode extends Node {
    private ExpNode expNode;
    private List<ExpNode> expNodes;
    private Token stringConst;
    private Token lBrackToken;
    private List<Token> commaTokens;
    private Token rBrackToken;

    public InitValNode(List<ExpNode> expNodes, Token lBrackToken, Token rBrackToken, List<Token> commaTokens) {
        this.expNodes = expNodes;
        this.lBrackToken = lBrackToken;
        this.rBrackToken = rBrackToken;
        this.commaTokens = commaTokens;
        this.type = NodeType.InitVal;
    }

    public InitValNode(Token stringConst) {
        this.stringConst = stringConst;
        this.type = NodeType.InitVal;
    }

    public InitValNode(ExpNode expNode) {
        this.expNode = expNode;
        this.type = NodeType.InitVal;
    }

    @Override
    public void print() {
        if (stringConst != null) {
            IOUtils.writeSymbol(stringConst.toString());
        } else if (expNode != null) {
            expNode.print();
        } else {
            IOUtils.writeSymbol(lBrackToken.toString());
            if (!expNodes.isEmpty()) {
                expNodes.get(0).print();
                for (int i = 1; i < expNodes.size(); i++) {
                    IOUtils.writeSymbol(commaTokens.get(i - 1).toString());
                    expNodes.get(i).print();
                }
            }
            IOUtils.writeSymbol(rBrackToken.toString());
        }
        IOUtils.writeSymbol(typeToString());
    }

    public ExpNode getExpNode() {
        return expNode;
    }

    public Token getStringConst() {
        return stringConst;
    }

    public String getStringContent() {
        String tmp = stringConst.getContent().replace("\"", "");
        for (Map.Entry<String, Character> entry : ESCAPE_CHAR_MAP.entrySet()) {
            tmp = tmp.replace(entry.getKey(), entry.getValue().toString());
        }
        return tmp;
    }

    public List<ExpNode> getExpNodes() {
        return expNodes;
    }
}
