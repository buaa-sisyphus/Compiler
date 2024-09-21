package frontend;

import error.ErrorType;
import token.Token;
import token.TokenType;
import error.Error;

import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.util.*;

public class Lexer {

    private static final Map<String, TokenType> keywords = new HashMap<>() {{ // 关键字
        put("main", TokenType.MAINTK);
        put("const", TokenType.CONSTTK);
        put("int", TokenType.INTTK);
        put("char", TokenType.CHARTK);
        put("break", TokenType.BREAKTK);
        put("continue", TokenType.CONTINUETK);
        put("if", TokenType.IFTK);
        put("else", TokenType.ELSETK);
        put("for", TokenType.FORTK);
        put("getint", TokenType.GETINTTK);
        put("getchar", TokenType.GETCHARTK);
        put("printf", TokenType.PRINTFTK);
        put("return", TokenType.RETURNTK);
        put("void", TokenType.VOIDTK);
    }};
    private static final Lexer instance = new Lexer();// 单例
    private List<Token> tokens = new ArrayList<>();// 识别到的所有单词
    private List<Error> errors = new ArrayList<>();// 试别到的所有错误

    public void analyze() throws IOException {
        PushbackReader reader = new PushbackReader(new FileReader("testfile.txt"), 1);
        int ch;
        int lineNum = 1;
        while ((ch = reader.read()) != -1) {
            char c = (char) ch;
            if (c == '\n') {
                //换行
                lineNum++;
            } else if (c == ' ' || c == '\t' || c == '\r') {
                // 空格
                continue;
            } else if (c == '_' || Character.isLetter(c)) {
                // 标识符
                String str = "" + c;
                while ((ch = reader.read()) != -1) {
                    c = (char) ch;
                    if (c == '_' || Character.isLetter(c) || Character.isDigit(c)) {
                        str += c;
                    } else {
                        reader.unread(c);
                        break;
                    }
                }
                //System.out.println("标识符：" + str);
                tokens.add(new Token(keywords.getOrDefault(str, TokenType.IDENFR), lineNum, str));
            } else if (Character.isDigit(c)) {
                //数字
                String str = "" + c;
                while ((ch = reader.read()) != -1) {
                    c = (char) ch;
                    if (Character.isDigit(c)) {
                        str = str + c;
                    } else {
                        //回退
                        reader.unread(c);
                        break;
                    }
                }
                //System.out.println("数字：" + str);
                tokens.add(new Token(TokenType.INTCON, lineNum, str));
            } else if (c == '\"') {
                // 字符串
                String str = "" + c;
                while ((ch = reader.read()) != -1) {
                    c = (char) ch;
                    if (c == '\"') {
                        str += c;
                        break;
                    } else if (c == '\n') {
                        //todo
                        //System.out.println("字符串有问题");
                        break;
                    } else {
                        str += c;
                    }
                }
                //System.out.println("字符串：" + str);
                tokens.add(new Token(TokenType.STRCON, lineNum, str));
            } else if (c == '\'') {
                // 字符
                String str = "" + c;
                for (int i = 0; i < 2; i++) {
                    if ((ch = reader.read()) != -1) {
                        c = (char) ch;
                        str += c;
                        if (i == 0 && c == '\\') {
                            // 可能为转义字符
                            ch = reader.read();
                            if (ch != -1) {
                                c = (char) ch;
                                str += c;
                                if (c == '\'') {
                                    ch = reader.read();
                                    if (ch != -1) {
                                        c = (char) ch;
                                        if (c == '\'') {
                                            // 是'\''这种情况
                                            str += c;
                                        }else{
                                            // 是'\'这种情况
                                            reader.unread(c);
                                        }
                                        break;
                                    }
                                }
                            }
                        } else if (i == 1) {
                            if(c=='\'') break; //不是转义字符
                            else {
                                //todo
                            }
                        }
                    }
                }
                //System.out.println("字符：" + str);
                tokens.add(new Token(TokenType.CHRCON, lineNum, str));
            } else if (c == '!') {
                // !或!=
                if ((ch = reader.read()) != -1) {
                    c = (char) ch;
                    if (c == '=') {
                        tokens.add(new Token(TokenType.NEQ, lineNum, "!="));
                    } else {
                        reader.unread(c);
                        tokens.add(new Token(TokenType.NOT, lineNum, "!"));
                    }
                } else {
                    tokens.add(new Token(TokenType.NOT, lineNum, "!"));
                }
            } else if (c == '&') {
                // &&
                if ((ch = reader.read()) != -1) {
                    c = (char) ch;
                    if (c == '&') {
                        tokens.add(new Token(TokenType.AND, lineNum, "&&"));
                    } else {
                        reader.unread(c);
                        tokens.add(new Token(TokenType.AND, lineNum, "&"));
                        errors.add(new Error(ErrorType.a, lineNum));
                    }
                } else {
                    tokens.add(new Token(TokenType.AND, lineNum, "&"));
                    errors.add(new Error(ErrorType.a, lineNum));
                }
            } else if (c == '|') {
                // ||
                if ((ch = reader.read()) != -1) {
                    c = (char) ch;
                    if (c == '|') {
                        tokens.add(new Token(TokenType.OR, lineNum, "||"));
                    } else {
                        //todo
                        reader.unread(c);
                        tokens.add(new Token(TokenType.OR, lineNum, "|"));
                        errors.add(new Error(ErrorType.a, lineNum));
                    }
                } else {
                    tokens.add(new Token(TokenType.OR, lineNum, "|"));
                    errors.add(new Error(ErrorType.a, lineNum));
                }
            } else if (c == '+') {
                // +
                tokens.add(new Token(TokenType.PLUS, lineNum, "+"));
            } else if (c == '-') {
                // -
                tokens.add(new Token(TokenType.MINU, lineNum, "-"));
            } else if (c == '*') {
                // *
                tokens.add(new Token(TokenType.MULT, lineNum, "*"));
            } else if (c == '/') {
                // /或//或/*
                if ((ch = reader.read()) != -1) {
                    c = (char) ch;
                    if (c == '/') {
                        // 单行注释
                        while ((ch = reader.read()) != -1) {
                            c = (char) ch;
                            if (c == '\n') {
                                lineNum++;
                                break;
                            }
                        }
                    } else if (c == '*') {
                        // 多行注释
                        while ((ch = reader.read()) != -1) {
                            c = (char) ch;
                            if (c == '\n') {
                                lineNum++;
                            } else if (c == '*') {
                                if ((ch = reader.read()) != -1 && (char) ch == '/') {
                                    break;
                                }
                            }
                        }
                    } else {
                        reader.unread(c);
                        tokens.add(new Token(TokenType.DIV, lineNum, "/"));
                    }
                } else {
                    tokens.add(new Token(TokenType.DIV, lineNum, "/"));
                }
            } else if (c == '%') {
                // %
                tokens.add(new Token(TokenType.MOD, lineNum, "%"));
            } else if (c == '<') {
                // <或<=
                if ((ch = reader.read()) != -1) {
                    c = (char) ch;
                    if (c == '=') {
                        tokens.add(new Token(TokenType.LEQ, lineNum, "<="));
                    } else {
                        reader.unread(c);
                        tokens.add(new Token(TokenType.LSS, lineNum, "<"));
                    }
                } else {
                    tokens.add(new Token(TokenType.LSS, lineNum, "<"));
                }
            } else if (c == '>') {
                // >或>=
                if ((ch = reader.read()) != -1) {
                    c = (char) ch;
                    if (c == '=') {
                        tokens.add(new Token(TokenType.GEQ, lineNum, ">="));
                    } else {
                        reader.unread(c);
                        tokens.add(new Token(TokenType.GRE, lineNum, ">"));
                    }
                } else {
                    tokens.add(new Token(TokenType.GRE, lineNum, ">"));
                }
            } else if (c == '=') {
                // =或==
                if ((ch = reader.read()) != -1) {
                    c = (char) ch;
                    if (c == '=') {
                        tokens.add(new Token(TokenType.EQL, lineNum, "=="));
                    } else {
                        reader.unread(c);
                        tokens.add(new Token(TokenType.ASSIGN, lineNum, "="));
                    }
                } else {
                    tokens.add(new Token(TokenType.ASSIGN, lineNum, "="));
                }
            } else if (c == ';') {
                // ;
                tokens.add(new Token(TokenType.SEMICN, lineNum, ";"));
            } else if (c == ',') {
                // ,
                tokens.add(new Token(TokenType.COMMA, lineNum, ","));
            } else if (c == '(') {
                // (
                tokens.add(new Token(TokenType.LPARENT, lineNum, "("));
            } else if (c == ')') {
                // )
                tokens.add(new Token(TokenType.RPARENT, lineNum, ")"));
            } else if (c == '[') {
                // [
                tokens.add(new Token(TokenType.LBRACK, lineNum, "["));
            } else if (c == ']') {
                // ]
                tokens.add(new Token(TokenType.RBRACK, lineNum, "]"));
            } else if (c == '{') {
                // {
                tokens.add(new Token(TokenType.LBRACE, lineNum, "{"));
            } else if (c == '}') {
                // }
                tokens.add(new Token(TokenType.RBRACE, lineNum, "}"));
            } else {
                //todo
                //System.out.println("未知:" + ch + " " + lineNum);
            }
        }
        reader.close();
    }

    public static Lexer getLexer() {
        return instance;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public List<Error> getErrors() {
        return errors;
    }

}
