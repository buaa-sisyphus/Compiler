package node;

import frontend.Parser;
import token.Token;
import token.TokenType;
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

    public Token getToken() {
        return token;
    }

    public Boolean isInt(){
        if (token.getType()== TokenType.INTTK){
            return true;
        }
        return false;
    }

}
