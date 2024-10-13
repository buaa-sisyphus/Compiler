package node;

import error.ErrorHandler;
import error.ErrorType;
import symbol.SymbolTable;
import token.Token;
import utils.IOUtils;

import java.util.List;

// Block → '{' { BlockItem } '}'
public class BlockNode extends Node {
    private Token lBraceToken;
    private List<BlockItemNode> blockItemNodes;
    private Token rBraceToken;

    public BlockNode(Token lBraceToken, List<BlockItemNode> blockItemNodes, Token rBraceToken) {
        this.lBraceToken = lBraceToken;
        this.blockItemNodes = blockItemNodes;
        this.rBraceToken = rBraceToken;
        this.type = NodeType.Block;
    }

    @Override
    public void print() {
        IOUtils.write(lBraceToken.toString());
        for (BlockItemNode blockItemNode : blockItemNodes) {
            blockItemNode.print();
        }
        IOUtils.write(rBraceToken.toString());
        IOUtils.write(typeToString());
    }

    public void fill(SymbolTable table) {
        for (BlockItemNode blockItemNode : blockItemNodes) {
            blockItemNode.fill(table);
        }
    }

    public void fill(SymbolTable table, boolean needReturn) {
        int endLine = rBraceToken.getLineNum();
        if (needReturn && (blockItemNodes == null || blockItemNodes.isEmpty())) {
            ErrorHandler.getInstance().addError(ErrorType.g, endLine);
            return;
        }
        int size = blockItemNodes.size();
        for (int i = 0; i < size; i++) {
            if (i == size - 1) {
                //最后一句
                blockItemNodes.get(i).fill(table, endLine, needReturn);
            } else {
                blockItemNodes.get(i).fill(table);
            }
        }
    }
}
