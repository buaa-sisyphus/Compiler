package node;

import error.Error;
import error.ErrorHandler;
import error.ErrorType;
import frontend.Parser;
import symbol.FuncSymbol;
import symbol.SymbolTable;
import token.Token;
import utils.IOUtils;

// FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
public class FuncDefNode extends Node {
    private FuncTypeNode funcTypeNode;
    private Token ident;
    private Token lParent;
    private FuncFParamsNode funcFParamsNode;
    private Token rParent;
    private BlockNode blockNode;

    public FuncDefNode(FuncTypeNode funcTypeNode, Token ident, Token lParent, FuncFParamsNode funcFParamsNode, Token rParent, BlockNode blockNode) {
        this.funcTypeNode = funcTypeNode;
        this.ident = ident;
        this.lParent = lParent;
        this.funcFParamsNode = funcFParamsNode;
        this.rParent = rParent;
        this.blockNode = blockNode;
        this.type = NodeType.FuncDef;
    }

    public void print() {
        funcTypeNode.print();
        IOUtils.write(ident.toString());
        IOUtils.write(lParent.toString());
        if (funcFParamsNode != null) {
            funcFParamsNode.print();
        }
        IOUtils.write(rParent.toString());
        blockNode.print();
        IOUtils.write(typeToString());
    }

    public void fill(SymbolTable table) {
        if (table.findSymbol(ident.getContent())) {
            ErrorHandler.getInstance().addError(ErrorType.b, ident.getLineNum());
        } else {
            FuncSymbol funcSymbol = new FuncSymbol();
            funcSymbol.set(ident, table.getScopeNum(), funcTypeNode.getToken().getType());
            table.addSymbol(funcSymbol.getName(), funcSymbol);
            SymbolTable newTable = new SymbolTable();
            newTable.setParentTable(table);
            table.addChild(newTable);
            Parser.scope++;
            newTable.setScopeNum(Parser.scope);
            if (funcFParamsNode != null) {
                funcFParamsNode.fill(newTable, funcSymbol);
            }
            boolean needReturn = true;
            if (funcTypeNode.getToken().getContent().equals("void")) needReturn = false;
            blockNode.fill(newTable,needReturn);
        }
    }
}
