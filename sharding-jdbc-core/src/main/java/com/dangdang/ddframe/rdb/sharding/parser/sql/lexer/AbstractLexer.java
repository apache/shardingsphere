/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parser.sql.lexer;

import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.ParserException;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * 词法解析器.
 * 
 * @author zhangliang 
 */
public abstract class AbstractLexer {
    
    @Getter
    private final String input;
    
    private final Term term;
    
    @Getter
    private int position;
    
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private Token token;
    
    @Getter
    private String literals;
    
    public AbstractLexer(final String input, final Dictionary dictionary) {
        this.input = input;
        term = new Term(input, dictionary);
    }
    
    /**
     * 跳至下一个语言符号.
     */
    public final void nextToken() {
        skipWhitespace();
        if (isVariableBegin()) {
            scanVariable();
            return;
        }
        if (isIdentifierBegin()) {
            scanIdentifier();
            return;
        }
        if (isHexDecimalBegin()) {
            scanHexDecimal();
            return;
        }
        if (isNumberBegin()) {
            scanNumber();
            return;
        }
        if (isHintBegin()) {
            scanHint();
            return;
        }
        if (isCommentBegin()) {
            scanComment();
            return;
        }
        if (isTernarySymbol()) {
            scanSymbol(3);
            return;
        }
        if (isBinarySymbol()) {
            scanSymbol(2);
            return;
        }
        if (isUnarySymbol()) {
            scanSymbol(1);
            return;
        }
        if (isCharsBegin()) {
            scanChars();
            return;
        }
        if (isAliasBegin()) {
            scanAlias();
            return;
        }
        if (isEOF()) {
            token = Literals.EOF;
        } else {
            token = Literals.ERROR;
        }
        literals = "";
    }
    
    private void skipWhitespace() {
        while (CharTypes.isWhitespace(charAt(position))) {
            position++;
        }
    }
    
    protected abstract boolean isVariableBegin();
    
    private void scanVariable() {
        term.scanVariable(position);
        setTermResult();
    }
    
    private boolean isIdentifierBegin() {
        return isIdentifierBegin(charAt(position));
    }
    
    private boolean isIdentifierBegin(final char ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || '`' == ch || '_' == ch || '$' == ch;
    }
    
    protected void scanIdentifier() {
        if ('`' == charAt(position)) {
            term.scanContentUntil(position, '`', Literals.IDENTIFIER, false);
        } else {
            term.scanIdentifier(position);
        }
        setTermResult();
    }
    
    private boolean isHexDecimalBegin() {
        return '0' == charAt(position) && 'x' == charAt(position + 1);
    }
    
    private void scanHexDecimal() {
        term.scanHexDecimal(position);
        setTermResult();
    }
    
    private boolean isNumberBegin() {
        return isDigital(charAt(position)) || ('.' == charAt(position) && isDigital(charAt(position + 1)) && !isIdentifierBegin(charAt(position - 1)));
    }
    
    private boolean isDigital(final char ch) {
        return ch >= '0' && ch <= '9';
    }
    
    private void scanNumber() {
        term.scanNumber(position);
        setTermResult();
    }
    
    protected boolean isHintBegin() {
        return false;
    }
    
    private void scanHint() {
        term.scanHint(position);
        setTermResult();
    }
    
    protected boolean isCommentBegin() {
        char currentChar = charAt(position);
        char nextChar = charAt(position + 1);
        return ('-' == currentChar && '-' == nextChar) || (('/' == currentChar && '/' == nextChar) || (currentChar == '/' && nextChar == '*'));
    }
    
    private void scanComment() {
        if (('/' == charAt(position) && '/' == charAt(position + 1)) || ('-' == charAt(position) && '-' == charAt(position + 1))) {
            term.scanSingleLineComment(position, 2);
        } else if ('#' == charAt(position)) {
            term.scanSingleLineComment(position, 1);
        } else if (charAt(position) == '/' && charAt(position + 1) == '*') {
            term.scanMultiLineComment(position);
        }
        setTermResult();
    }
    
    private boolean isTernarySymbol() {
        return isSymbol(":=:") || isSymbol("<=>");
    }
    
    private boolean isBinarySymbol() {
        return isSymbol(":=") || isSymbol("..") || isSymbol("&&") || isSymbol("||")
                || isSymbol(">=") || isSymbol("<=") || isSymbol("<>") || isSymbol(">>") || isSymbol("<<")
                || isSymbol("!=") || isSymbol("!>") || isSymbol("!<");
    }
    
    private boolean isUnarySymbol() {
        return isSymbol("(") || isSymbol(")") || isSymbol("[") || isSymbol("]") || isSymbol("{") || isSymbol("}")
                || isSymbol("+") || isSymbol("-") || isSymbol("*") || isSymbol("/") || isSymbol("%") || isSymbol("^")
                || isSymbol("=") || isSymbol(">") || isSymbol("<") || isSymbol("~") || isSymbol("!") || isSymbol("?")
                || isSymbol("&") || isSymbol("|") || isSymbol(".") || isSymbol(":") || isSymbol("#") || isSymbol(",") || isSymbol(";");
    }
    
    private boolean isSymbol(final String symbol) {
        for (int i = 0; i < symbol.length(); i++) {
            if (symbol.charAt(i) != charAt(position + i)) {
                return false;
            }
        }
        return true;
    }
    
    private void scanSymbol(final int symbolLength) {
        term.scanSymbol(position, symbolLength);
        setTermResult();
    }
    
    private boolean isCharsBegin() {
        return '\'' == charAt(position);
    }
    
    protected void scanChars() {
        term.scanChars(position);
        setTermResult();
    }
    
    private boolean isAliasBegin() {
        return '\"' == charAt(position);
    }
    
    private void scanAlias() {
        term.scanContentUntil(position, '\"', Literals.ALIAS, true);
        setTermResult();
    }
    
    private void setTermResult() {
        literals = term.getLiterals();
        token = term.getToken();
        position = term.getCurrentPosition();
    }
    
    private boolean isEOF() {
        return position >= input.length();
    }
    
    protected final char charAt(final int index) {
        return index >= input.length() ? (char) CharTypes.EOI : input.charAt(index);
    }
    
    protected final int increaseCurrentPosition() {
        return ++position;
    }
    
    public final void accept(final Token token) {
        if (this.token == token) {
            nextToken();
            return;
        }
        throw new ParserException(this, token);
    }
    
    /**
     * 判断当前语言标记是否和其中一个传入的标记相等.
     * 
     * @param tokens 待判断的标记
     * @return 是否有相等的标记
     */
    public final boolean equalToken(final Token... tokens) {
        for (Token each : tokens) {
            if (each == token) {
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * 如果当前语言符号等于传入值, 则跳过.
     *
     * @param tokens 待跳过的语言符号
     * @return 是否跳过(或可理解为是否相等)
     */
    public final boolean skipIfEqual(final Token... tokens) {
        for (Token each : tokens) {
            if (equalToken(each)) {
                nextToken();
                return true;
            }
        }
        return false;
    }
    
    /**
     * 直接跳转至传入的语言符号.
     *
     * @param tokens 跳转至的语言符号
     */
    public final void skipUntil(final Token... tokens) {
        Set<Token> tokenSet = Sets.newHashSet(tokens);
        tokenSet.add(Literals.EOF);
        while (!tokenSet.contains(token)) {
            nextToken();
        }
    }
    
    /**
     * 跳过小括号内所有的语言符号.
     *
     * @return 小括号内所有的语言符号
     */
    public final String skipParentheses() {
        StringBuilder result = new StringBuilder("");
        int count = 0;
        if (Symbol.LEFT_PAREN == token) {
            int beginPosition = position;
            result.append(Symbol.LEFT_PAREN.getLiterals());
            nextToken();
            while (true) {
                if (Literals.EOF == token || (Symbol.RIGHT_PAREN == token && 0 == count)) {
                    break;
                }
                if (Symbol.LEFT_PAREN == token) {
                    count++;
                } else if (Symbol.RIGHT_PAREN == token) {
                    count--;
                }
                nextToken();
            }
            result.append(input.substring(beginPosition, position));
            nextToken();
        }
        return result.toString();
    }
}
