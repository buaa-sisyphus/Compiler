# Compiler

## 1. 介绍

使⽤Java编写的SysY语言（C语言子集）完整编译器，涵盖了从词法分析、语法分析、语义分析到中间代码生成、⽬标代码生成的完整编译流程，实现了从SysY到LLVM IR中间语言再到MIPS汇编指令的翻译过程。


## 2. 编译器总体设计

### 2.1. 总体结构

该编译器将分为主程序、词法分析、语法分析、语义分析、代码生成、错误处理六个模块。

### 2.2. 接口设计

#### 2.2.1 输出接口

在`/utils/IOUtils.java`中，定义了多种静态输出方法：

* `writeTokens(List<Token> tokens)`用于词法分析结果的输出
* `write(String str)`用于输出字符串，主要用在语法分析结果的输出
* `writeSymbol(SymbolTable symbolScope)`用于语义分析结果的输出
* `writeErrors(List<Error> errors)`用于错误信息的输出

#### 2.2.2 词法分析接口

* 调用`Lexer`类的静态方法`getLexer()`，可以得到一个`Lexer`类实例。

* 调用`analyze()`接口即可进行词法分析。
* 调用`getTokens()`接口即可获得词法分析结果，一个集合。

```java
Lexer lexer = Lexer.getLexer();
lexer.analyze();
List<Token> tokens = lexer.getTokens();
```

#### 2.2.3 语法分析接口

* 使用`new Parser(tokens)`，参数类型为`List<Token>`，即将词法分析的结果传入，即可获得一个`Paser`类实例。
* 调用`analyze()`接口可进行语法分析，构建抽象语法树。
* 调用`print()`接口可输出语法分析结果。
* 调用`getCompUnitNode()`接口可获得抽象语法树的根节点。

```java
Parser parser = new Parser(lexer.getTokens());
parser.analyze();
paeser.print();
```

### 2.3. 文件组织

```
.
|   Compiler.java
|   config.json
|
+---backend
|       MIPSGenerator.java
|       StackSlot.java
|
+---error
|       Error.java
|       ErrorHandler.java
|       ErrorType.java
|
+---frontend
|       Lexer.java
|       Parser.java
|       Visitor.java
|
+---llvm
|   |   IRGenerator.java
|   |   IRModule.java
|   |
|   +---types
|   |       ArrayType.java
|   |       FunctionType.java
|   |       IntegerType.java
|   |       LabelType.java
|   |       PointerType.java
|   |       Type.java
|   |       VoidType.java
|   |
|   \---values
|       |   Argument.java
|       |   BasicBlock.java
|       |   BuildFactory.java
|       |   Const.java
|       |   ConstArray.java
|       |   ConstChar.java
|       |   ConstInt.java
|       |   ConstString.java
|       |   Function.java
|       |   GlobalVar.java
|       |   ReturnValue.java
|       |   User.java
|       |   Value.java
|       |   ValueScope.java
|       |
|       \---instructions
|               AllocaInst.java
|               BinaryInst.java
|               BrInst.java
|               CallInst.java
|               GEPInst.java
|               Instruction.java
|               LoadInst.java
|               Operator.java
|               RetInst.java
|               StoreInst.java
|               TruncInst.java
|               ZextInst.java
|
+---node
|       AddExpNode.java
|       BlockItemNode.java
|       BlockNode.java
|       BTypeNode.java
|       CharacterNode.java
|       CompUnitNode.java
|       CondNode.java
|       ConstDeclNode.java
|       ConstDefNode.java
|       ConstExpNode.java
|       ConstInitValNode.java
|       DeclNode.java
|       EqExpNode.java
|       ExpNode.java
|       ForStmtNode.java
|       FuncDefNode.java
|       FuncFParamNode.java
|       FuncFParamsNode.java
|       FuncRParamsNode.java
|       FuncTypeNode.java
|       InitValNode.java
|       LAndExpNode.java
|       LOrExpNode.java
|       LValNode.java
|       MainFuncDefNode.java
|       MulExpNode.java
|       Node.java
|       NodeType.java
|       NumberNode.java
|       PrimaryExpNode.java
|       RelExpNode.java
|       StmtNode.java
|       UnaryExpNode.java
|       UnaryOpNode.java
|       VarDeclNode.java
|       VarDefNode.java
|
+---symbol
|       FuncParam.java
|       FuncSymbol.java
|       Symbol.java
|       SymbolScope.java
|       VarSymbol.java
|
+---token
|       Token.java
|       TokenType.java
|
\---utils
        CalUtils.java
        IOUtils.java

```

## 3. 错误处理设计

首先定义`ErrorType`类，表示错误类别码：

```java
public enum ErrorType {
    a,
    b,
    ...
}
```

定义`Error`类，有错误码和行号两个属性，用于表示具体的错误：

```java
public class Error {
    private ErrorType errorType;
    private int lineNum;
	
    ...
        
    @Override
    public String toString() {
        return lineNum+" "+errorType.toString()+"\n";
    }
}
```

定义`ErrorHandler`类，采用单例模式：

* `errors`集合用于记录错误，

* `addError`方法用于向表中添加错误，

* `getErrors`则是将列表中错误按行号进行排序后，返回集合

```java
public class ErrorHandler {
    List<Error> errors=new ArrayList<>();
    private static final ErrorHandler instance=new ErrorHandler();
    private ErrorHandler(){}
    public static ErrorHandler getInstance(){
        return instance;
    }

    public void addError(ErrorType errorType,int lineNum){
        errors.add(new Error(errorType,lineNum));
    }

    public List<Error> getErrors(){
        errors.sort(Comparator.comparingInt(Error::getLineNum));
        return errors;
    }
}
```

## 4. 词法分析设计

### 4.1 词法单元

定义枚举类`TokenType`，表示单词类别码：

```java
public enum TokenType {
    IDENFR,
    INTCON,
    STRCON,
    CHRCON,
    MAINTK,
    ...
}
```

定义`Token`类，有种类、行号、内容三个属性：

```java
public class Token {
    private TokenType type;
    private int lineNum;
    private String content;
    ...
}
```

### 4.2 词法分析器实现

定义`Lexer`类，即词法分析器：

```java
public class Lexer {
    
    private static final Lexer instance = new Lexer();// 单例
    private List<Token> tokens = new ArrayList<>();// 识别到的所有单词

    public static Lexer getLexer() {
        return instance;
    }

    public List<Token> getTokens() {
        return tokens;
    }
    
    public void analyze(){
        ...
    }
}

```

在此类中定义一个保留字表，便于后续的查找：

```java
private static final Map<String, TokenType> keywords = new HashMap<>() {{ // 关键字
        put("main", TokenType.MAINTK);
        put("const", TokenType.CONSTTK);
        put("int", TokenType.INTTK);
        ...
    }};
```

在`analyze`函数中进行词法分析，大致的程序流程图如下：

<img src="D:\2221\大三\编译\词法分析流程图.png" alt="词法分析流程图" style="zoom:150%;" />

## 5. 语法分析设计

### 5.1. 抽象语法树节点

新建文件夹`node`，先规定抽象语法树节点的类别，于是定义一个枚举类`NodeType`，包含了文法中每一种非终结符：

```java
public enum NodeType {
    AddExp,
    BlockItem,
    Block,
    BType,
    Character,
   ...
}
```

定义抽象父类`Node`（后来发现实际没什么用），有 类别 这一种属性，`print()`是需要实现的输出方法：

```java
public abstract class Node {
    public NodeType type;

    public abstract void print();

    public String typeToString() {
        return "<" + type.toString() + ">" + "\n";
    }
}
```

为文法中每一种非终结符都创建一个节点类，以非终结符名称+`Node`命名，比如为`<CompUnit>`创建了`CompUnitpNode`类。根据对应的文法规则，为其添加属性，使用`ConstDecl`为例子：

```java
// ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
public class ConstDeclNode extends Node {
    private Token constToken;
    private BTypeNode bTypeNode;
    private List<ConstDefNode> constDefNodes;
    private List<Token> commaTokens;
    private Token semicnToken;

    public ConstDeclNode(Token constToken, BTypeNode bTypeNode, List<ConstDefNode> constDefNodes, List<Token> commaTokens, Token semicnToken) {
        this.constToken = constToken;
        this.bTypeNode = bTypeNode;
        this.constDefNodes = constDefNodes;
        this.commaTokens = commaTokens;
        this.semicnToken = semicnToken;
        this.type = NodeType.ConstDecl;
    }
	...
}

```

根据`ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'`，在抽象语法树中，一个`ConstDeclNode`应该拥有一个`const`、一个`Btype`、一个`ConstDef`、零或若干个`ConstDef`为子节点。因此创建了`Token constToken`、`BTypeNode bTypeNode`、`List<ConstDefNode> constDefNodes`等等属性。

可以发现，如果是对应于终结符就添加`Token`类的属性，如果是对应一个或多个非终结符（终结符）就添加`List<xxxNode>`（`List<Token>`）类的属性，如果是对应一个非终结符就添加`xxxNode`类的属性。

对于`print()`的实现，需要遍历每个对应着非终结符的属性，调用其`print()`方法。如果对应的是终结符，比如一个`[`，那就直接输出这个符号内容即可。最后再输出本节点的类别，比如`CompUnitNode`就是输出`<CompUnit >`，调用`typeToString()`即可获得。

```java
 public void print() {
    IOUtils.writeSymbol(constToken.toString());
    bTypeNode.print();
    constDefNodes.get(0).print();
    for (int i = 1; i < constDefNodes.size(); i++) {
        IOUtils.writeSymbol(commaTokens.get(i - 1).toString());
        constDefNodes.get(i).print();
    }
    IOUtils.writeSymbol(semicnToken.toString());
    IOUtils.writeSymbol(typeToString());
}
```

### 5.2. 关于左递归与回溯

因为语法分析采用的是递归下降分析法，所以文法当中不能存在左递归，同时尽可能的避免回溯。

阅读发现，存在左递归的规则都是与`exp`有关的，在这里使用`MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp`为例子进行说明。

第一步，消除左递归。可以发现，`MulExp`肯定是以`UnaryExp`开头的（话有点糙），于是这条规则可以修改为`MulExp → UnaryExp | UnaryExp ('*' | '/' | '%') MulExp`。

第二步，避免回溯。可以发现修改后的规则的右部的两个选择中有相交的First集，导致不能仅根据当前词法成分判断接下来的语法成分是什么，后续可能会造成回溯。可以将其修改为`MulExp → UnaryExp [('*' | '/' | '%') MulExp]`。

观察样例输出，应该是遵循最右规范推导，然后自底向上输出。因为修改了规则，所以生成的语法树会变化，`print()`函数的实现也跟其他的不一样：

```java
@Override
public void print() {
    unaryExpNode.print();
    IOUtils.writeSymbol(typeToString());
    if (mulExpNode != null) {
        IOUtils.writeSymbol(op.toString());
        mulExpNode.print();
    }
}
```

将`IOUtils.writeSymbol(typeToString());`紧跟着`unaryExpNode.print();`即可。

### 5.3. 语法分析器实现

定义`Parser`类，即语法分析器：

```java
public class Parser {
    private List<Token> tokens;//词法单元集合
    private CompUnitNode compUnitNode;
    private int index = 0;//记录分析位置

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }
    
    private Token match(TokenType type) {
        if (tokens.get(index).getType() == type) {
            Token token = tokens.get(index);
            if (index < tokens.size() - 1) {
                index++;
            }
            return token;
        } else if (type == TokenType.SEMICN) {
            Token token = tokens.get(index - 1);
            ErrorHandler.getInstance().addError(ErrorType.i, token.getLineNum());
            return new Token(TokenType.SEMICN, token.getLineNum(), ";");
        } else if (type == TokenType.RPARENT) {
            Token token = tokens.get(index - 1);
            ErrorHandler.getInstance().addError(ErrorType.j, token.getLineNum());
            return new Token(TokenType.RPARENT, token.getLineNum(), ")");
        } else if (type == TokenType.RBRACK) {
            Token token = tokens.get(index - 1);
            ErrorHandler.getInstance().addError(ErrorType.k, token.getLineNum());
            return new Token(TokenType.RBRACK, token.getLineNum(), "]");
        }
        return null;
    }

    public CompUnitNode getCompUnitNode() {
        return compUnitNode;
    }

    public void analyze() {
        compUnitNode = CompUnit();
    }

    public void print() {
        if (compUnitNode != null) compUnitNode.print();
    }
    ...
}

```

在`match(TokenType type)`中进行终结符的匹配、分析位置的移动以及错误处理。

对于分析部分，每一条规则都创建对应的函数，按照递归下降法进行分析。

* 对于终结符，调用match进行匹配
* 对于非终结符，调用对应的分析函数进行匹配
* 对于右部有多种选择的，根据first集进行区分

**注意：**因为存在缺少`)`、`]`、`;`的错误类型，因此以这三个符号判断某个非终结符存不存在、有没有结束时需要小心。例如下方判断`FuncRParams`存不存在时，应该使用其first集进行判断，而不是通过当前的`tokens(index)`是不是`)`来确定。

```java
private UnaryExpNode UnaryExp() {
    // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
    if (isUnaryOP()) {
        UnaryOpNode unaryOpNode = UnaryOp();
        UnaryExpNode unaryExpNode = UnaryExp();
        return new UnaryExpNode(unaryOpNode, unaryExpNode);
    } else if (isIdent()) {
        Token identToken = match(TokenType.IDENFR);
        Token lParentToken = match(TokenType.LPARENT);
        FuncRParamsNode funcRParamsNode = null;
        if (isFuncRParams()) {
            funcRParamsNode = FuncRParams();
        }
        Token rParentToken = match(TokenType.RPARENT);
        return new UnaryExpNode(identToken, lParentToken, rParentToken, funcRParamsNode);
    } else {
        PrimaryExpNode primaryExpNode = PrimaryExp();
        return new UnaryExpNode(primaryExpNode);
    }
}
```

## 6. 语义分析设计

### 6.1. 符号管理设计

首先定义符号抽象父类`Symbol`类：

```java
public abstract class Symbol {
    protected String name;
    protected int scopeNum;
    protected int type;// 0 var 1 array 2 func
    protected int btype; // 0 int 1 char 2 void
    protected int con;//0 var 1 const
    protected int lineNum;//标识符声明的位置

    @Override
    public String toString() {
        return scopeNum + " " + name + " " + toType() + "\n";
    }

    public String toType() {
        String str = "";
        if (con == 1) str += "Const";

        if (btype == 0) str += "Int";
        else if (btype == 1) str += "Char";
        else str += "Void";

        if (type == 1) str += "Array";
        else if (type == 2) str += "Func";

        return str;
    }
	...
}

```

* `scopeNum`是符号所处在的作用域的号数
* `lineNum`是符号被声明时处于的行号
* `type`是这个符号的类型，0表示普通变量，1表示数组变量，2表示函数
* `btype`是这个符号的二进制类型（函数的返回值类型），0表示`int`，1表示`char`，2表示`void`
* `con`表示这个符号是不是常量
* `toType()`是将符号类型转化为字符串，方便输出。后续判断类型的时候可以让代码可读性更高。比如`symbol.toType().contains("Array")`而不是`symbol.type==1`，后者看起来不是很方便，有时候会让我混乱

接着定义`VarSymbol`和`FuncSymbol`。前者表示普通变量和数组变量，后者表示函数：

```java
public class VarSymbol extends Symbol {
    private List<Object> values;
	... 
}

public class FuncSymbol extends Symbol {
    private List<FuncParam> params = new ArrayList<>();

    public void addParam(FuncParam param) {
        params.add(param);
    }
    ...
}

public class FuncParam {
    private String funcName;
    private int btype;//0 int 1 char
    private int type;//0 var 1 array

    public String toType() {
        String str = "";
        if (btype == 0) str += "Int";
        else str += "Char";
        if (type == 1) str += "Array";
        return str+"\n";
    }
}

```

* `values`是用来记录值的，但是目前还没使用
* `params`是用来记录函数形参的

符号表类`SymbolScope`定义：

```java
public class SymbolScope {
    private Map<String, Symbol> symbolTable = new LinkedHashMap<>();
    private List<SymbolScope> children = new ArrayList<>();
    private SymbolScope parent;
    private int scopeNum;
    private boolean needReturn = false;
    private boolean isFunc = false;

    public SymbolScope() {
    }

   	....

    public Symbol getSymbolDeep(String ident) {
        Symbol symbol = null;
        for (SymbolScope table = this; table != null; table = table.parent) {
            symbol = table.getSymbol(ident);
            if (symbol != null) {
                return symbol;
            }
        }
        return symbol;
    }

    ....
}

```

我使用的是树状符号表。一个SymbolScope实例可以看作一个作用域。

* `symbolTable`：记录符号的Map，符号名为键，符号为值。因为输出要按照声明顺序，所以实际为`LinkedHashMap`
* `childrenTables`：子作用域的符号表集合
* `parent`：父作用域的符号表
* `scopeNum`：作用域号数，相当于`id`
* `isFunc`：用来记录这个符号表（作用域）是不是属于函数的。实际编码的时候发现需要加上这个属性，在后期错误处理需要用到
* `needReturn`：如果这个符号表（作用域）是属于函数的，还需要记录这个函数是否需要返回值。实际编码的时候发现需要加上这个属性，在后期错误处理需要用到
* `getSymbol`：就是在本层作用域（本符号表）中寻找符号
* `getSymbolDepp`：就是在本层以及父层、父层的父层等等寻找符号

### 6.2. 符号管理实现

符号表的建立在`/frontend/Buider.java`中

```java
public class Builder {
    private static final Builder instance = new Builder();
    public static int scope = 0;
    public static int loop = 0;//记录循环
    private SymbolTable root;
    private SymbolTable cur;
	
    
    public void build(CompUnitNode compUnitNode) {
        CompUnit(compUnitNode);
    }

    private void pushTable(boolean needReturn, boolean isFunc) {
        scope++;
        SymbolTable newTable = new SymbolTable();
        newTable.setScopeNum(scope);
        newTable.setParentTable(cur);
        newTable.setFunc(isFunc);
        newTable.setNeedReturn(needReturn);
        cur.addChild(newTable);
        cur = newTable;
    }

    private void popTable() {
        cur = cur.getParentTable();
    }

    private void initTable() {
        loop = 0;
        scope = 1;
        root = new SymbolTable();
        root.setScopeNum(scope);
        root.setNeedReturn(false);
        root.setFunc(false);
        cur = root;
    }
    ...
}
```

* `loop`：记录循环。当`loop`为0时，表示当前不处于循环体
* `root`：树状符号表的根节点
* `cur`：当前在使用的符号表
* `pushTable`：在将进入新作用域前调用，创建一个新的符号表，初始化新符号表的一些属性
* `popTable`：在从作用域退出后调用，表示返回父作用域

符号表的建立，思路就是遍历语法分析中生成的语法树，主要是处理声明语句，将声明的变量加入符号表（先暂时不处理值），将声明的函数加入符号表，将函数的形参加入符号表（不要漏了将形参加入`funcSymbol`的`params`中）。

### 6.3. 错误处理

错误处理才是这次作业比较麻烦的地方，非常容易遗漏。

**`b`、`c`类：**

使用在`SymbolTable`中定义的`getSymbol`和`getSymbolDeep`两个方法去解决。只需要注意在寻找`b`类错误的时候只在本层找，`c`类错误在本层以及所有祖先层找。

**`d`、`e`类：**

`d`类简单，就拿`FuncRParamsNode`的`expNodes`集合大小与这个函数的`params`集合大小比较就行。需要注意的是当`FuncRParams`为空的情况。

`e`类只需要判断四种情况，变量传数组、数组传变量、int数组传char数组、char数组传int数组，使用下面函数处理：

```java
private boolean matchParams(List<FuncParam> params, List<ExpNode> expNodes, SymbolTable table) {
    for (int i = 0; i < params.size(); i++) {
        String paramType = params.get(i).toType();
        ExpNode expNode = expNodes.get(i);
        String tmp = expNode.getType();
        if (tmp.equals("0")) {
            //非数组
            if (paramType.contains("Array")) {
                return false;
            }
        } else {
            String symbolType = table.getSymbolDeep(tmp).toType();
            if (symbolType.contains("Array") && paramType.contains("Array")) {
                if ((symbolType.contains("Int") && paramType.contains("Char")) ||
                        (symbolType.contains("Char") && paramType.contains("Int"))) {
                    return false;
                }
            } else if ((symbolType.contains("Array") && !paramType.contains("Array"))
                    || (!symbolType.contains("Array") && paramType.contains("Array"))) {
                return false;
            }
        }
    }
    return true;
}
```

`expNode.getType()`需要解释一下：就是不停地往“深”处调用`getType()`。

对于`MulExp`、`AddExp → MulExp | AddExp ('+' | '−') MulExp`等，只需要判断`op`存不存在。如果存在，说明这个式子肯定是一个非数组型的（题目保证过不使用数组地址做加减乘除括号等运算），返回一个字符串“0”。反之就继续向深处调用`getType()`。

对于`UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp'`，第三种选择刚刚讲过，只会是非数组型，第二个选择因为函数会返回`int`或`char`，也是非数组型，因此也返回字符串“0”。第一种选择就继续调用它的`getType()`。

对于`PrimaryExp → '(' Exp ')' | LVal | Number | Character`，第一种刚刚讲过。第三四种肯定也是非数组了。第二种还要继续往深处调用。

对于`LVal → Ident ['[' Exp ']']`，如果有左括号，那包是非数组型的。如果没有，这时候我们拿不到这个符号是什么，所以我们直接返回这个符号名，在最开始的调用处用符号表搜索这个符号，再进一步判断。

下面给出一部分代码：

```java
//UnaryExpNode.java
//UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp'
public String getType() {
    if (primaryExpNode != null) {
        return primaryExpNode.getType();
    } else {
        return "0";
    }
}
```

**`f`类：**

`f`类的话需要注意下面这种情况，`return`所在的直接作用域并不是函数的那个作用域：

```c
void a(){
    if(1==1){
        return 1;
    }
}
```

`return`语句在的直接作用域是这个`if`语句作用域，我们需要找到外层的这个`a`，判断它需不需要返回值。

```java
case Return:
    // Stmt → 'return' [Exp] ';'
    ExpNode expNode = stmtNode.getExpNode();
    SymbolTable tmp = null;
    boolean flag = false;
    if (expNode != null) {
        //有返回值
        for (tmp = cur; tmp != null; tmp = tmp.getParentTable()) {
            //一直往上找，找到所属的函数
            if (tmp.isFunc()) {
                if (!tmp.getNeedReturn()) flag = true;
                break;
            }
        }
        if (flag) {
            ErrorHandler.getInstance().addError(ErrorType.f, stmtNode.getReturnToken().getLineNum());
        }
        Exp(stmtNode.getExpNode());
    }
	break;
```

**`g`类：**

因为题目保证了只需要找函数末尾有没有`return`，所以我们在`block(BlockNode blockNode)`中取最后一个`blockItem`来判断即可，注意当`blockItem`数量为0时的情况。

```java
 private void Block(BlockNode blockNode) {
    // Block → '{' { BlockItem } '}'
    ...
    if (cur.isFunc() && cur.getNeedReturn()) {
        if (blockItemNodes.isEmpty()) {
            ErrorHandler.getInstance().addError(ErrorType.g, blockNode.getrBraceToken().getLineNum());
            return;
        }
        BlockItemNode last = blockItemNodes.get(blockItemNodes.size() - 1);
        if (last.getDeclNode() != null) {
            ErrorHandler.getInstance().addError(ErrorType.g, blockNode.getrBraceToken().getLineNum());
            return;
        }
        if (last.getStmtNode().getStmtType() != StmtNode.StmtType.Return) {
            ErrorHandler.getInstance().addError(ErrorType.g, blockNode.getrBraceToken().getLineNum());
            return;
        }
    }
}
```

**`h`类：**

当常量被**赋值**时，才需要报错。所以需要传入一个`isAssign`，如果有`=`进行赋值的，`isAssign`就为真，比如`Stmt → LVal '=' 'getint''('')'';'`。

```java
private void LVal(LValNode lValNode, boolean isAssign) {
    // LVal → Ident ['[' Exp ']']
    Token ident = lValNode.getIdent();
    Symbol symbol = cur.getSymbolDeep(ident.getContent());
    if (symbol == null) {
        ErrorHandler.getInstance().addError(ErrorType.c, ident.getLineNum());
        return;
    } else if (symbol.getCon() == 1 && isAssign) {
        ErrorHandler.getInstance().addError(ErrorType.h, ident.getLineNum());
        return;
    }
    if (lValNode.getExpNode() != null) {
        Exp(lValNode.getExpNode());
    }
}
```

**`I`类：**

就计算字符串中的`%d`和`%c`个数，与表达式个数比较一下就行。

**`m`类：**

全局维护一个 `loop`，初始为 0，每次进入循环就加一，退出循环就减一。在`break`或`continue`时判断一下`loop`是不是0就好了。

```java
case For:
    //Stmt → 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
    ...
    loop++;
    Stmt(stmtNode.getStmtNode());
    loop--;
    break;
```

```java
case Break:
case Continue:
    // Stmt → 'break' ';' | 'continue' ';'
    if (loop <= 0) {
        ErrorHandler.getInstance().addError(ErrorType.m, stmtNode.getBreakToken().getLineNum());
    }
    break;
```

## 7. 代码生成设计

### 7.1. 中间代码llvm ir生成

<img src="D:\2221\大三\编译\llvm类图.png" alt="llvm类图" style="zoom:50%;" />

在llvm中一切皆value，上图基本描述了llvm中类与类之间的关系。一个`Module`代表一个文件，一个`Module`又由多个`GlobalValue`组成......这样一层一层地往下拆解。所以我们建类的时候就基本按照这个图来即可。

然后还有一个比较重要的就是`User`类。在这个图中，所有的指令类都是`User`的子类，可以用下面这个例子去理解：

```llvm
A: %add1 = add nsw i32 %a, %b
B: %add2 = add nsw i32 %a, %b
C: %sum  = add nsw i32 %add1, %add2
```

A是一条`Instruction`，它在代码中的体现就是 `%add1`，即指令的返回值。`%add1`或者说A这条指令是一个user，它可以使用操作数`%a`和`%b`；`%add2`是一个user，它可以使用操作数`%a`和`%b`。接着`%add1`和`%add2`又作为参数被`%sum`使用，通过这个user-use链，可以将指令与参数，指令与指令相互连接起来。

#### 7.1.1.Type的设计

llvm中的`Type`也是类似的。所有的类型都继承`Type`，像`i32`和`i8`，就是一种`IntergerType`；`i32*`和`i8*`就是`PointerType`。当然这个图里的类还不够完整，我还参考学长的设计添加了`ArrayType`等类型，如下：

![我的Type](D:\2221\大三\编译\我的Type.png)

* `LableType`就是标签类型，是基本块的类型。
* `IntegerType`就是基本类型，`i32`或者`i8`。
* `FunctionType`是函数类型，包含了函数的返回类型，和一个函数参数类型集合。
* `ArrayType`是数组类型，包含的元素类型和数组长度。
* `PointerType`是指针类型，包含指向的元素的类型，可能是`i8*`、`[i8 x 3]*`、还可能是`i8**`。	

以`IntegerType`为例子：

```java
public class IntegerType implements Type {
    private final int bit;

    public static final IntegerType i8 = new IntegerType(8);

    public static final IntegerType i32 = new IntegerType(32);

    private IntegerType(int bit) {
        this.bit = bit;
    }

    @Override
    public String toString() {
        return "i" + bit;
    }
}
```

`IntegerType`就只有一个属性，用来记录这个基本类型是8位还是32位的，最开始我也考虑过有1位的`i1`，就是对应`boolean`，但是后来发现没啥用，就删掉了。这里使用`static final`是因为变量的类型`i32`都是一样的，不需要每一个变量都新`new`一个`IntegerType`，不像`ArrayType`那样每一个数组类型都有不同的长度，需要建立不同的`ArrayType`对象。另一个原因是在判断这个类型是`i8`或者`i32`的时候方便一点，直接使用`== Interger.i32`，而不是`((IntegerType)type).isI32`，太丑了。

#### 7.1.2.Value的设计

建立完`Type`以及其子类后，就可以设计`Value`以及其子类了。首先当然是写出`Value`类了：

```java
public class Value {
    private final IRModule module = IRModule.getInstance();
    private String name;
    private Type type;
    private int id; // 全局唯一的id
    public static int REG_NUMBER = 0; // LLVM 中的寄存器编号
    public static int STR_NUMBER = 0; // 全局字符串的编号
    public static int LABEL_NUMBER = 0; // LLVM 中基本块的编号
    public static int MIPS_ID = 0;

    public Value(String name, Type type) {
        this.name = name;
        this.type = type;
        this.id = MIPS_ID++;
    }

    public String getName() {
        return name;
    }

    ......

    @Override
    public String toString() {
        return type.toString() + " " + name;
    }
}

```

* `name`：value的名字
* `type`：value的类型
* `id`：记录每一个`value`的唯一id，是在生成目标代码mips时会用到的
* `REG_NUMBER`和`LABEL_NUMBER`是记录一个函数中变量号和基本块号的

在进入一个新`Function`中时，`REG_NUMBER`和`LABEL_NUMBER`会重置为0。因为llvm中就是这么设计的，甚至llvm中变量和基本块编号是共用的。这里就要提及llvm中的数字编号，大号必须在小号后面使用，即：

```
...
28:
	%8 = add nsw i32 %6, %7
```

这种是不允许的，数字编号在处理`if`和`for`的时候有一点麻烦。但是使用字符串编号就不会关心这个，我原来用的数字编号，后面就改成字符串编号了，局部变量叫`%var_xxx`，基本块叫`%label_xxx`，全局字符串叫`@str_xxx`，其他的全局变量叫`@变量名`。顺便将变量编号和基本块编号区分开了。

接着设计我认为第二重要的`user`类：

```java
public class User extends Value{

    private List<Value> operands;

    public User(String name, Type type) {
        super(name, type);
        this.operands = new ArrayList<>();
    }

    public List<Value> getOperands() {
        return operands;
    }

    public Value getOperand(int index) {
        return operands.get(index);
    }

    public void addOperand(Value operand) {
        this.operands.add(operand);
    }
}

```

* `operands`：操作数集合，因为有些`user`的操作数有多个，所以搞成一个集合。

下面是我类的设计架构：

![我的Value](D:\2221\大三\编译\我的Value.png)

* `BasicBlock`表示基本块，其`name`是`label_xxx`，其`type`是`LableType`类型，保存着`Instruction`指令集合和`belongFunction`所属函数。
* `Function`表示函数，其`name`是函数名，其`type`是`FunctionType`类型。它保存着`BasicBlock`基本块集合，以及`Argument`参数集合。
* `Argument`表示函数参数，其`name`是`%arg_xxx`。
* `Const`是一个抽象类，表示数字、字符、字符串等。
* `GlobalVar`表示全局变量或全局常量，其`name`为`@变量名`。
* `Instruction`是一个抽象类，表示指令，其`name`为`%var_xxx`。

![我的Instruction](D:\2221\大三\编译\我的Instruction.png)

* 所有的指令都继承自`Instruction`，以上就是实验中会使用到的所有指令。`BinaryInst`是二元指令，加减乘除什么的。

比较特殊的是，我还建了一个`ReturnValue`类。因为像`AddExp`这个规则对应的函数，有时候返回的是一个具体的`int`值，有时候返回一个`Value`类型的对象，还有时候需要返回其他一些综合属性，但是函数返回类型只能是一个，所以我就将这些返回信息全部保存在`ReturnValue`对象中一起返回。将属性都设置为`public`单纯是我比较懒。

```java
public class ReturnValue {
    public int constValue;
    public Value value;
    public boolean needLoad = false;

    public ReturnValue(int constValue) {
        this.constValue = constValue;
        this.value = null;
    }

    public ReturnValue(Value value) {
        this.value = value;
    }
}
```

#### 7.1.3.LLVM IR生成器

再来就是这里需要建立符号表。一个作用域域有`valueTable`和`constTable`两个表，分别保存`value`和常量。一般情况下，键都指的是变量原本的名字，值就是其对应的Value。特别地，常量数组所有元素都保存在`constTable`中，键是`数组名;偏移量`，比如`arr[10]={1,2,3}`中`arr[0]`记录为`arr;0`。

```java
public class ValueScope {
    private HashMap<String, Value> valueTable = new HashMap<>();
    private HashMap<String, Integer> constTable = new HashMap<>();
    private ValueTable parent;
    private List<ValueTable> childrenTables = new ArrayList<>();

    public ValueTable() {
    }

   ......

}

```

上面这些都准备好后，就可以遍历语法树了。在遍历过程中，我们会频繁地创建Value，这里可以使用工厂模式，可以屏蔽一些操作细节，复用代码。所有的Value都在工厂中创建，以`buildStore`为例子：

```java
public StoreInst buildStore(BasicBlock basicBlock, Value pointer, Value value) {
    Type targetType = ((PointerType) pointer.getType()).getTargetType();
    if (targetType == IntegerType.i32 && value.getType() == IntegerType.i8) {
        value = buildFactory.buildZext(basicBlock, value);
    } else if (targetType == IntegerType.i8 && value.getType() == IntegerType.i32) {
        value = buildFactory.buildTrunc(basicBlock, value);
    }
    StoreInst storeInst = new StoreInst(basicBlock, pointer, value);
    basicBlock.addInstruction(storeInst);
    return storeInst;
}
```

这次实验会涉及大量类型转换，位扩展或者截断，比如在赋值时，你可能会把一个`i32`的value存到`i8*`的地址中。我们直接在`buildStore`中进行处理，那么就不需要每次赋值时都考虑类型转换，只要无脑`buildStore`就好，这样能避免某处遗漏了类型转换。

说到遍历语法树，我认为`Stmt → 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt`还不太好写，我尝试记录一下我的思路。按照指导书的说法，这条规则可以改写为`Stmt → 'for' '(' [ForStmt1] ';' [Cond] ';' [ForStmt2] ')' Stmt (BasicBlock)`。

1. 这个`forStmt1`是属于for语句前面那个基本块（就先叫`enterBlock`）的，所以先判断`forStmt1`存不存在，存在就调用相应的处理函数`ForStmt()`，将`forStmt1`中的指令全部加入基本块。
2. 分别看`cond`和`forSTmt2`存不存在，存在就创建对应的基本块`condBlock`和`forStmtBlock`，不然就是`null`
3. 循环体`stmt`肯定是存在的，创建一个`forBlock`表示循环体中的**第一个**基本块。还要为for循环结束后的跳转地创建一个`finalBlock`基本块。
4. 然后就可以按照下图，调用相应的处理函数以及添加`BrInst`跳转指令了。
   1. 如果`cond`存在，那就是`enterBlock->condBlock`。
      1. 添加`enterBlock->condBlock`，让`curBlock=condBlock,curTureBlock=forBlock,curFalseBlock=finalBlock`，调用`Cond()`函数。该函数里面已经加上了到`forBlock`的跳转。
   2. 反之就是添加`forStmt1->forBlock`。让`forEndBlock = finalBlock,curBlock = forBlock`，调用`Stmt()`函数。
      1. 从`Stmt()`出来后，`curBlock`表示循环体内的**最后一个**基本块，我们要决定它跳转去哪
      2. 如果`forStmt2`不为空，那就是添加`curBlock->forStmtBlock`
      3. 反之，如果`cond`不为空，那就是添加`curBlock->condBlock`
      4. 反之，就是添加`condBlock->forBlock`，跳转到循环体开头的第一个基本块
   3. 如果`forStmt2`存在，修改`curBlock=forstmtBlock`，调用`ForStmt()`
      1. 同样从`ForStmt()`出来后，要决定`curBlock`跳转到哪
      2. 如果`cond`不为空，那就是添加`curBlock->condBlock`
      3. 反之，添加`curBlock->forBlock`
   4. 最后将`curBlock`设为`finalBlock`，结束循环
   5. 考虑到有`continue`，所以还需要设置一下continue时要跳转的目的地

写到这里时，我想到了其实不需要反复判断`cond`和`forStmt2`存不存在。可以直接给它们创建不为`null`的基本块，它们不存在时对应的是只有`br`语句的基本块，存在时对应的就是有其他语句和`br`语句的基本块。但这样有时候会相对多一些语句。

<img src="D:\2221\大三\编译\for循环逻辑.png" alt="for循环逻辑" style="zoom: 33%;" />



```java
private BasicBlock forEndBlock = null;
private BasicBlock curTrueBlock = null;
private BasicBlock curFalseBlock = null;
...
case For:
    //Stmt → 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
    BasicBlock basicBlock = curBlock;
    if (stmtNode.getForStmtNodeFir() != null) {
        ForStmt(stmtNode.getForStmtNodeFir());
    }

    BasicBlock condBlock = null;
    if (stmtNode.getCondNode() != null) {
        condBlock = buildFactory.buildBasicBlock(curFunction);
    }
    BasicBlock forStmtBlock = null;
    if (stmtNode.getForStmtNodeSec() != null) {
        forStmtBlock = buildFactory.buildBasicBlock(curFunction);
    }
    BasicBlock forBlock = buildFactory.buildBasicBlock(curFunction);
    BasicBlock finalBlock = buildFactory.buildBasicBlock(curFunction);
    buildFactory.buildBr(curBlock, condBlock == null ? forBlock : condBlock);
    //设置continue
	if (forStmtBlock != null) {
        continueBlock = forStmtBlock;
    } else if (condBlock != null) {
        continueBlock = condBlock;
    } else {
        continueBlock = forBlock;
    }
    forEndBlock = finalBlock;
    curBlock = forBlock;
    Stmt(stmtNode.getStmtNode(), curFunction);

    //进行到这里，curBlock是for循环体的最后一个语句块
    if (forStmtBlock != null) {
        buildFactory.buildBr(curBlock, forStmtBlock);
    } else if (condBlock != null) {
        buildFactory.buildBr(curBlock, condBlock);
    } else {
        buildFactory.buildBr(curBlock, forBlock);
    }

    if (stmtNode.getForStmtNodeSec() != null) {
        curBlock = forStmtBlock;
        ForStmt(stmtNode.getForStmtNodeSec());
        buildFactory.buildBr(curBlock, condBlock == null ? forBlock : condBlock);
    }
    
    curBlock = condBlock;
    curTrueBlock = forBlock;
    curFalseBlock = finalBlock;
    if (stmtNode.getCondNode() != null) {
        Cond(stmtNode.getCondNode(), curFunction);
    }
    curBlock = finalBlock;
    break;
```

### 7.2. 目标代码mips生成

llvm转mips其实不是很麻烦，只需要将每种llvm语句对应到若干条mips指令即可。

在生成中间代码时，类型转化是一个比较折磨人的地方，但在生成mips时可以不需要区分byte和word，直接无脑当成word就完事了，就不需要考虑位对齐、用`lb`还是`lw`这些问题。

还需要建立符号表。我这里是搞了一个`StackSlot`，表示栈中的一个4字节单元：

```java
public class StackSlot {
    private int pos;
    private Value value;

    public StackSlot(int pos, Value value) {
        this.pos = pos;
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    public int getPos() {
        return pos;
    }
}
```

* `pos`：表示其在栈中的位置
* `value`：这个单元对应的llvm中的虚拟寄存器

然后符号表是用的`private HashMap<String, StackSlot> stack = new HashMap<>();`，我给每一个value都规定了一个唯一的id，用这个id生成的键不会重复。因此感觉用`HashMap`比`ArrayList`好点，查找更方便。

根据id生成键的规则如下：

```java
public String getName() {
    return name;
}
public String getNameWithID() {
    if (isNumber()) return getName();
    else if (this instanceof Function) return getName();
    else if (this instanceof GlobalVar) return ((GlobalVar) this).getOriginalName();//没@
    else return getName() + "_" + +id;
}
```

然后就是关于`$sp`指针申请栈空间的问题。需要申请一个8字节空间时，第一种是先将`$sp`自减8，然后使用`0($sp)`和`4($sp)`，偏移量是非负数；第二种是直接用负数偏移量`-4($sp)`和`-8($sp)`，只有当调用函数时才将`$sp`自减。

第一种的麻烦点在于处理for循环时，每次新循环要将上次循环为临时变量申请的空间从栈弹出。比如在for循环前，有一个变量a，相对sp的偏移量为0。在循环体中每次都`int c=a+1`，生成mips就是

```
lw $t0,0($sp)
subu $sp,$sp,4
addu $t0,$t0,1
sw $t0,0($sp)
```

第一次循环结束时如果不将c弹出，那么第二次循环再次取`0($sp)`时就不是a了。要知道一次循环中一共申请了多少临时空间，然后在一次循环要结束时弹出，好像有点麻烦。

第二种的好处就是在sp除了调用函数外不会动。当然我也试过`$fp`和`$sp`配合使用，出了一些bug，不如只用sp方便。

主要的生成mips函数`generateMIPS`如下：

```
public String generateMIPS(IRModule irModule) {
    //.data
    for (GlobalVar globalVar : irModule.getGlobals()) {
        //处理全局变量
    }
    //.text
    for (int i = 5; i < functions.size(); i++) {
        //处理非库函数
        for (int j = 0; j < 4 && j < argNum; j++) {
            //保存在寄存器里的参数
        }
        for (int j = 4; j < argNum; j++) {
            //保存在栈里的参数
        }

        for (int j = 0; j < basicBlocks.size(); j++) {
            //遍历每一个基本块
            for (int k = 0; k < instructions.size(); k++) {
                //遍历每一条语句
                if (instruction instanceof BinaryInst) {

                } else if (instruction instanceof BrInst) {

                } else if (instruction instanceof CallInst) {

                } else if (instruction instanceof TruncInst) {

                } else if (instruction instanceof ZextInst) {

                } else if (instruction instanceof GEPInst) {

                } else if (instruction instanceof LoadInst) {

                } else if (instruction instanceof AllocaInst) {

                } else if (instruction instanceof RetInst) {

                } else if (instruction instanceof StoreInst) {

                }
            }
        }
    }

    return sb.toString();
}
```

然后为每一种llvm语句编写对应的翻译代码即可。数组相关的稍微麻烦一点点。

## 8. 代码优化设计

我只进行了两处简单的优化

### 8.1对于UnaryExp的优化

对于形如`+-+--a`这样的式子，优化前我是每次遇到一个减号，用0减去原来的数；遇到加号则直接返回原来的数。

优化后，我向下遍历`UnaryExp`，统计减号的个数，若减号个数为奇数，那么就使用0减去最后一次遇到的那个`UnaryExp`；若为偶数，直接返回最后遇到的`UnaryExp`。

代码如下：

```java
int cnt = 0;
TokenType op = null;
UnaryExpNode tmp = unaryExpNode;
while (tmp.getUnaryOpNode() != null) {
    op = tmp.getUnaryOpType();
    if (op == TokenType.MINU) cnt++;
    else if (op == TokenType.NOT) break;
    tmp = tmp.getUnaryExpNode();
}
if (cnt % 2 == 0) {
    if (op == TokenType.NOT) {
        ReturnValue returnValue = UnaryExp(tmp.getUnaryExpNode(), isConst);
        returnValue.value = buildFactory.buildNot(curBlock, returnValue.value);
        return returnValue;
    }
    return UnaryExp(tmp, isConst);
} else {
    ReturnValue returnValue = null;
    if (op == TokenType.NOT) {
        returnValue = UnaryExp(tmp.getUnaryExpNode(), isConst);
        returnValue.value = buildFactory.buildNot(curBlock, returnValue.value);
    } else {
        returnValue = UnaryExp(tmp, isConst);
    }
    if (isConst) {
        returnValue.constValue = -returnValue.constValue;
    } else {
        returnValue.value = buildFactory.buildBinary(curBlock, Operator.Sub, ConstInt.ZERO, returnValue.value);
    }
    return returnValue;
}
```

### 8.2对于乘法的优化

当乘数是2的n次幂时，可以使用左移运算，比使用直接相乘的指令要快一些。

代码如下：

```java
private boolean isPowerOfTwo(int n) {
    // 判断条件：n > 0 且 n 与 n-1 按位与运算的结果为 0
    return n > 0 && (n & (n - 1)) == 0;
}

private int logBase2Bitwise(int n) {
    if (n <= 0 || !isPowerOfTwo(n)) {
        return -1;  // 非 2 的幂返回 -1
    }
    int count = 0;
    while (n > 1) {
        n >>= 1;  // 每次右移一位
        count++;
    }
    return count;  // 返回 2 的幂次
}

private void translateBinaryInst(BinaryInst inst) {
    ...
    if (inst.isMul()) {
        Value left = inst.getLeft();
        Value right = inst.getRight();
        if (isNumber(left.getNameWithID()) && isPowerOfTwo(Integer.parseInt(left.getNameWithID()))) {
            load("$t0", right.getNameWithID());
            sb.append("\tsll, $t0, $t0").append(", ").append(logBase2Bitwise(Integer.parseInt(left.getNameWithID()))).append("\n");
        } else if (isNumber(right.getNameWithID()) && isPowerOfTwo(Integer.parseInt(right.getNameWithID()))) {
            ...
        }else{
            ...
        }
        store("$t0", inst.getNameWithID());
        return;
    }
    ...
}
```
