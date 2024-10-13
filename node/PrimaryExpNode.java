package node;

import frontend.Parser;
import symbol.Symbol.SymbolType;
import symbol.SymbolTable;
import token.Token;
import token.TokenType;
import utils.IOUtils;

// PrimaryExp → '(' Exp ')' | LVal | Number | Character
public class PrimaryExpNode extends Node {
    private Token lParent = null;
    private Token rParent = null;
    private ExpNode expNode = null;
    private LValNode lValNode = null;
    private NumberNode numberNode = null;
    private CharacterNode characterNode = null;

    public PrimaryExpNode(Token lParent, Token rParent, ExpNode expNode) {
        this.lParent = lParent;
        this.rParent = rParent;
        this.expNode = expNode;
        this.type = NodeType.PrimaryExp;
    }

    public PrimaryExpNode(CharacterNode characterNode) {
        this.characterNode = characterNode;
        this.type = NodeType.PrimaryExp;
    }

    public PrimaryExpNode(NumberNode numberNode) {
        this.numberNode = numberNode;
        this.type = NodeType.PrimaryExp;
    }

    public PrimaryExpNode(LValNode lValNode) {
        this.lValNode = lValNode;
        this.type = NodeType.PrimaryExp;
    }

    @Override
    public void print() {
        if (expNode != null) {
            IOUtils.write(lParent.toString());
            expNode.print();
            IOUtils.write(rParent.toString());
        } else if (lValNode != null) {
            lValNode.print();
        } else if (numberNode != null) {
            numberNode.print();
        } else if (characterNode != null) {
            characterNode.print();
        }
        IOUtils.write(typeToString());
    }

    public String getType() {
        if (lParent != null || numberNode != null || characterNode != null) {
            return "var";
        }else {
            return lValNode.getType();
        }
    }

    public void fill(SymbolTable table){
        if(expNode != null){
            expNode.fill(table);
        }else if(lValNode != null){
            lValNode.fill(table,false);
        }else if(numberNode != null){
            numberNode.fill(table);
        }else if(characterNode != null){
            characterNode.fill(table);
        }
    }
}
