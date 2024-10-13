package node;

import frontend.Parser;
import symbol.SymbolTable;
import token.Token;
import utils.IOUtils;

// Number → IntConst
public class NumberNode extends Node {
    private Token intConst;

    public NumberNode(Token intConst) {
        this.intConst = intConst;
        this.type=NodeType.Number;
    }

    @Override
    public void print() {
        IOUtils.write(intConst.toString());
        IOUtils.write(typeToString());
    }

    public int getNumber() {
        return Integer.parseInt(intConst.getContent());
    }

    public void fill(SymbolTable table) {

    }
}
