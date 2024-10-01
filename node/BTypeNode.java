package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

// BType → 'int' | 'char'
public class BTypeNode extends Node{
    private Token token;

    public BTypeNode(Token token) {
        this.token = token;
        this.type=NodeType.BType;
    }

    public void print() {
        IOUtils.write(token.toString());
    }
}
