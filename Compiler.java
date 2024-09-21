import frontend.Lexer;
import utils.IOUtils;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        Lexer lexer =Lexer.getLexer();
        lexer.analyze();
        if(lexer.getErrors().isEmpty()) IOUtils.writeTokens(lexer.getTokens());
        else IOUtils.wirteErrors(lexer.getErrors());
    }
}
