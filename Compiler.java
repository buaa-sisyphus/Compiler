import frontend.Lexer;
import frontend.Parser;
import utils.IOUtils;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        IOUtils.empty("parser.txt");
        IOUtils.empty("lexer.txt");
        IOUtils.empty("error.txt");
        Lexer lexer = Lexer.getLexer();
        lexer.analyze();
//        IOUtils.writeTokens(lexer.getTokens());
        Parser parser = new Parser(lexer.getTokens(), lexer.getErrors());
        parser.analyze();
        parser.print();
        IOUtils.writeErrors(parser.getErrors());
    }
}
