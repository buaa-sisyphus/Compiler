package node;

import frontend.Parser;
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
        IOUtils.write(charConst.toString());
        IOUtils.write(typeToString());
    }
}
