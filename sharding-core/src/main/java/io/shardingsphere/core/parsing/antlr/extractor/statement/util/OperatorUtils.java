/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.core.parsing.antlr.extractor.statement.util;

import java.util.HashMap;
import java.util.Map;

import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Symbol;

/**
 * Condition utility.
 * 
 * @author duhongjun
 */
public class OperatorUtils {
    
    private static final Map<String, String> parens = new HashMap<>();
    static {
        parens.put("(", ")");
        parens.put("[", "]");
        parens.put("{", "}");
    }
    
    /**
     * Judge is rational operator.
     * 
     * @param text input text
     * @return rational operator return true
     */
    public static boolean isRationalOperator(String text) {
        if (isAnd(text)) {
            return true;
        }
        if (isOr(text)) {
            return true;
        }
        return false;
    }
    
    /**
     * Is keyword or operator.
     * 
     * @param text input text
     * @return or operator return true
     */
    public static boolean isOr(String text) {
        if (DefaultKeyword.OR.name().equalsIgnoreCase(text)) {
            return true;
        }
        if (Symbol.DOUBLE_BAR.name().equalsIgnoreCase(text)) {
            return true;
        }
        return false;
    }
    
    /**
     * Is keyword and operator.
     * 
     * @param text input text
     * @return and operator return true
     */
    public static boolean isAnd(String text) {
        if (DefaultKeyword.AND.name().equalsIgnoreCase(text)) {
            return true;
        }
        if (Symbol.DOUBLE_AMP.name().equalsIgnoreCase(text)) {
            return true;
        }
        return false;
    }
    
    /**
     * Is start paren ex '(','[', '{'.
     * 
     * @param text input text
     * @return start paren return true
     */
    public static boolean isStartParen(String text) {
        return parens.containsKey(text);
    }
    
    /**
     * Judge start paren match end paren.
     * 
     * @param startParen start paren
     * @param endParen end paren
     * @return match return true
     */
    public static boolean parenMatch(String startParen, String endParen) {
        if(parens.containsKey(startParen)) {
            return parens.get(startParen).equals(endParen);
        }
        return false;
    }
}
