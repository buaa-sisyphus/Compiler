package node;

import token.Token;
import utils.IOUtils;

// VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
public class VarDefNode extends Node {
    private Token ident;
    private Token lBrackToken;
    private Token rBrackToken;
    private ConstExpNode constExpNode;
    private Token assignToken;
    private InitValNode initValNode;

    public VarDefNode(Token ident, Token lBrackToken, Token rBrackToken, ConstExpNode constExpNode, Token assignToken, InitValNode initValNode) {
        this.ident = ident;
        this.lBrackToken = lBrackToken;
        this.rBrackToken = rBrackToken;
        this.assignToken = assignToken;
        this.constExpNode = constExpNode;
        this.initValNode = initValNode;
        this.type = NodeType.VarDef;
    }

    @Override
    public void print() {
        IOUtils.writeSymbol(ident.toString());
        if (lBrackToken != null) {
            IOUtils.writeSymbol(lBrackToken.toString());
            constExpNode.print();
            IOUtils.writeSymbol(rBrackToken.toString());
        }
        if (initValNode != null) {
            IOUtils.writeSymbol(assignToken.toString());
            initValNode.print();
        }
        IOUtils.writeSymbol(typeToString());
    }

    public Token getIdent() {
        return ident;
    }

    public ConstExpNode getConstExpNode() {
        return constExpNode;
    }

    public InitValNode getInitValNode() {
        return initValNode;
    }
}
