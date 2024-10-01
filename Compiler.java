import frontend.Lexer;
import frontend.Parser;
import utils.IOUtils;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        IOUtils.empty("parser.txt");
        IOUtils.empty("lexer.txt");
        Lexer lexer = Lexer.getLexer();
        lexer.analyze();
//        if(lexer.getErrors().isEmpty()) IOUtils.writeTokens(lexer.getTokens());
//        else IOUtils.writeErrors(lexer.getErrors());
        Parser parser = new Parser(lexer.getTokens());
        parser.analyze();
        parser.print();
    }
}
