import backend.MIPSGenerator;
import error.ErrorHandler;
import frontend.Visitor;
import frontend.Lexer;
import frontend.Parser;
import llvm.IRGenerator;
import llvm.IRModule;
import utils.IOUtils;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        IOUtils.empty("parser.txt");
        IOUtils.empty("lexer.txt");
        IOUtils.empty("error.txt");
        IOUtils.empty("symbol.txt");
        IOUtils.empty("llvm_ir.txt");
        IOUtils.empty("mips.txt");
        ErrorHandler handler = ErrorHandler.getInstance();
        Lexer lexer = Lexer.getLexer();
        lexer.analyze();
        Parser parser = new Parser(lexer.getTokens());
        parser.analyze();
        Visitor visitor = Visitor.getInstance();
        visitor.build(parser.getCompUnitNode());
        if (handler.getErrors().isEmpty()) {
            IRGenerator irGenerator = IRGenerator.getInstance();
            irGenerator.CompUnit(parser.getCompUnitNode());
            IRModule irModule = IRModule.getInstance();
//            IOUtils.writeLLVM(irModule.toString());
            MIPSGenerator mipsGenerator = MIPSGenerator.getInstance();
            IOUtils.writeMIPS(mipsGenerator.generateMIPS(irModule));
        } else IOUtils.writeErrors(handler.getErrors());
    }
}
