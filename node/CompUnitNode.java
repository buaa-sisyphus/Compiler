package node;

import frontend.Parser;
import symbol.SymbolTable;
import utils.IOUtils;

import java.util.List;

// CompUnit → {Decl} {FuncDef} MainFuncDef
public class CompUnitNode extends Node{
    private List<DeclNode> declNodes;
    private List<FuncDefNode> funcDefNodes;
    private MainFuncDefNode mainFuncDefNode;

    public CompUnitNode(List<DeclNode> declNodes, List<FuncDefNode> funcDefNodes, MainFuncDefNode mainFuncDefNode) {
        this.declNodes = declNodes;
        this.funcDefNodes = funcDefNodes;
        this.mainFuncDefNode = mainFuncDefNode;
        this.type=NodeType.CompUnit;
    }

    public void print(){
        for(DeclNode declNode : declNodes){
            declNode.print();
        }
        for(FuncDefNode funcDefNode : funcDefNodes){
            funcDefNode.print();
        }
        mainFuncDefNode.print();
        IOUtils.write(typeToString());
    }

    public void fill(SymbolTable table){
        Parser.scope++;
        table.setScopeNum(Parser.scope);
        for(DeclNode declNode : declNodes){
            declNode.fill(table);
        }
        for(FuncDefNode funcDefNode : funcDefNodes){
            funcDefNode.fill(table);
        }
        mainFuncDefNode.fill(table);
    }
}
