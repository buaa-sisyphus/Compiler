package node;

import token.Token;
import utils.IOUtils;

// Character → CharConst
public class CharacterNode extends Node {
    private Token charConst;

    public CharacterNode(Token charConst) {
        this.charConst = charConst;
        this.type=NodeType.Character;
    }

    @Override
    public void print() {
        IOUtils.writeSymbol(charConst.toString());
        IOUtils.writeSymbol(typeToString());
    }

    public char getChar(){
        return charConst.getContent().charAt(0);
    }

}
