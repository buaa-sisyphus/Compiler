import error.ErrorHandler;
import frontend.Lexer;
import frontend.Parser;
import utils.IOUtils;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        IOUtils.empty("parser.txt");
        IOUtils.empty("lexer.txt");
        IOUtils.empty("error.txt");
        IOUtils.empty("symbol.txt");
        Lexer lexer = Lexer.getLexer();
        ErrorHandler handler=ErrorHandler.getInstance();
//        IOUtils.writeTokens(lexer.getTokens());
        lexer.analyze();
        Parser parser = new Parser(lexer.getTokens());
        parser.analyze();
//        parser.print();
        parser.fill();
        if(handler.getErrors().isEmpty()) IOUtils.write(parser.getSymbolTable());
//        else IOUtils.writeErrors(handler.getErrors());
    }
}
