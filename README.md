# Compiler
## 1. 参考编译器介绍

### 1.1. 总体结构

Pascal编译器的设计主要分为以下几个模块：

* **词法分析器（Lexer）**：负责从源文件中读取字符并将其解析为符号（Tokens），这些符号包括标识符、常量、关键字、操作符等。

* **语法分析器（Parser）**：根据上下文无关文法，将词法分析器产生的符号组成语法正确的结构（如语句、表达式、函数等），并对其进行语法检查。

* **语义分析器（Semantic Analyzer）**：确保程序符号和类型的一致性、变量的声明与使用、过程和函数的参数匹配等。

* **代码生成器（Code Generator）**：生成中间代码（虚拟机的指令集），为每个语句生成对应的机器码或虚拟机指令。

* **解释器（Interpreter）**：通过模拟虚拟机的执行环境，对生成的中间代码进行解释执行，或者直接在目标机器上运行生成的目标代码。

### 1.2. 接口设计

#### 1.2.1. 编译器的输入与输出接口

* **输入接口**：
  * `INPUT`：标准键盘输入。
  * `PRD`：程序中定义的文件变量，文件输入接口。
* **输出接口**：
  * `OUTPUT`：标准屏幕输出。
  * `PRR`：程序中定义的文件变量，文件输出接口。

#### 1.2.2. 词法分析器接口

词法分析器的作用是从源程序中读取字符，逐个扫描并识别出构成语言的基本符号`Tokens`。

* `insymbol`：词法分析器主要接口，从源代码中读取下一个词法单元，并将其转换为相应的符号，大致逻辑如下：
  1. 记录换行符，跳过其他空白符等。
  2. 识别标识符或关键字。
  3. 识别数字。
  4. 识别字符串和字符。
  5. 处理注释。
  6. 识别其他操作和特殊符号。

* `nextch`：读取下一个字符的函数。

#### 1.2.3. 语法分析器接口

语法分析器采用递归下降分析法，从词法分析器接收到的词法单元中构建程序的语法结构，验证是否符合语法规则，并生成抽象语法树。

* `block`：语法分析器主要接口，处理程序块的语法结构。大致逻辑如下：

  1. 初始化，创建块。
  2. 如果是函数或过程，处理参数列表。
  3. 如果是函数，处理返回类型。

  2. 处理声明，包括常量、变量、子程序等等。

  3. 处理语句，即函数的主体。

* `statement`：用于处理单个语句的函数，如赋值语句、条件语句等等。如果遇到嵌套结构，会递归调用自身进行处理。

* `enter*`：以`enter`开头的函数，用于记录和管理符号表。

#### 1.2.4. 语义分析器接口

Pascal 编译器中，语义分析与语法分析紧密结合。语法分析器通过递归下降的方式进行语法分析，并在此过程中进行语义检查。其中涉及的重要接口如下：

* `resulttype`：检查两个操作数的类型是否兼容，并返回结果类型。

* `expression`：解析表达式，检查操作数的类型是否匹配，调用 `resulttype` 进行类型检查。
* `statement`：语句处理。
* `assignment`：处理赋值语句，确保右边表达式的类型与左边变量的类型一致。
* `call`：处理函数或过程的调用，并检查实参和形参的类型是否一致。
* `typ`：用于处理类型声明。
* ...

#### 1.2.5. 代码生成器接口

代码生成器负责为经过语法和语义检查的表达式、语句、过程和函数调用等生成指令，并将这些指令存储在一个代码缓冲区中（`code`数组）。代码生成穿插在如`whilestatement`、`assignment`等函数中。

主要任务包括：

1. 为每个操作生成对应的指令。

2. 为变量、常量和数组等生成加载和存储指令。

3. 为控制结构（如 `if`、`while`、`for` 等）生成跳转指令。

4. 支持函数调用、参数传递、返回值等。

5. 处理符号表中的变量和常量引用。

代码生成器的主要接口是以emit开头，是代码生成的核心部分。

* `emit`：用于生成没有额外参数的指令。
* `emit1`：用于生成带有一个参数的指令。
* `emit2`：用于生成带有两个参数的指令。

#### 1.2.6. 解释器接口

解释器负责执行代码生成器生成的中间代码。

主要任务包括：

1. 逐条执行虚拟机指令。
2. 管理运行时栈，处理函数调用、参数传递、局部变量、返回值等。
3. 执行控制流相关的跳转操作。
4. 处理基本的算术运算、逻辑运算、数据读取与写入等。
5. 处理程序终止、出错等情况。

解释器主要通过以下接口实现：

* `interpret`：从代码数组中读取指令，根据指令的操作码和操作数执行相应的操作。
* `inter0`：处理地址加载和显示更新。
* `inter1`：处理跳转和过程调用。
* `inter2`：处理数组索引和数据块操作。
* `inter3`：处理堆栈操作、和函数退出等等。
* `inter4`：处理比较运算。
* `inter5`：处理布尔运算、算术运算和逻辑运算。
* `inter6`：处理输入输出操作。

#### 1.2.7. 其他接口

错误处理负责记录错误信息。主要由`error`接口完成。

* `error`：负责报告语法或语义错误。

### 1.3. 文件组织

Pascal 编译器的文件组织比较紧凑，主要包含以下几大功能模块：词法分析器、语法分析器、语义分析器、代码生成器、解释器、错误处理模块。

所有这些功能都整合在一个程序文件中，模块之间通过全局变量和数据结构进行数据传递与交互。

## 2. 编译器总体设计

### 2.1. 总体结构

该编译器将分为主程序、词法分析、语法分析、语义分析、代码生成、错误处理六个模块。

### 2.2. 接口设计

#### 2.2.1 输出接口

在`/utils/IOUtils.java`中，定义了多种静态输出方法：

* `writeTokens(List<Token> tokens)`用于词法分析结果的输出
* `write(String str)`用于输出字符串，主要用在语法分析结果的输出
* `writeSymbol(SymbolTable symbolTable)`用于语义分析结果的输出
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
│  Compiler.java
│  config.json
│  error.txt
│  parser.txt
│  README.md
│  testfile.txt
│
├─error
│      Error.java
│      ErrorHandler.java
│      ErrorType.java
│
├─frontend
│      Lexer.java
│      Parser.java
│
├─node
│      AddExpNode.java
│      BlockItemNode.java
│      BlockNode.java
│      BTypeNode.java
│      CharacterNode.java
│      CompUnitNode.java
│      CondNode.java
│      ConstDeclNode.java
│      ConstDefNode.java
│      ConstExpNode.java
│      ConstInitValNode.java
│      DeclNode.java
│      EqExpNode.java
│      ExpNode.java
│      ForStmtNode.java
│      FuncDefNode.java
│      FuncFParamNode.java
│      FuncFParamsNode.java
│      FuncRParamsNode.java
│      FuncTypeNode.java
│      InitValNode.java
│      LAndExpNode.java
│      LOrExpNode.java
│      LValNode.java
│      MainFuncDefNode.java
│      MulExpNode.java
│      Node.java
│      NodeType.java
│      NumberNode.java
│      PrimaryExpNode.java
│      RelExpNode.java
│      StmtNode.java
│      UnaryExpNode.java
│      UnaryOpNode.java
│      VarDeclNode.java
│      VarDefNode.java
│
├─token
│      Token.java
│      TokenType.java
│
└─utils
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

## 4.1 词法单元

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

## 4.2 词法分析器实现

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

## 5.1. 抽象语法树节点

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

## 5.2. 关于左递归与回溯

因为语法分析采用的是递归下降分析法，所以文法当中不能存在左递归，同时尽可能的避免回溯。

阅读发现，存在左递归的规则都是与`exp`有关的，在这里使用`MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp`为例子进行说明。

第一步，消除左递归。可以发现，`MulExp`肯定是以`UnaryExp`开头的（话有点糙），于是这条规则可以修改为`MulExp → UnaryExp | UnaryExp ('*' | '/' | '%') MulExp`。

第二步，避免回溯。可以发现修改后的规则的右部的两个选择中有相交的First集，导致不能仅根据当前词法成分判断接下来的语法成分是什么，后续可能会造成回溯。可以将其修改为`MulExp → UnaryExp [('*' | '/' | '%') MulExp]`。

因为修改了规则，所以生成的语法树会变化，`print()`函数的实现也跟其他的不一样：

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

## 7. 代码生成设计

## 8. 代码优化设计
