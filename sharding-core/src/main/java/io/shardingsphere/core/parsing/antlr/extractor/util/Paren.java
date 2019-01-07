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

package io.shardingsphere.core.parsing.antlr.extractor.util;

import lombok.RequiredArgsConstructor;

/**
 * Paren of SQL.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public enum Paren {
    
    PARENTHESES("(", ")"), BRACKET("[", "]"), BRACES("{", "}");
    
    private final String leftParen;
    
    private final String rightParen;
    
    /**
     * Judge passed token is left paren or not.
     * 
     * @param token token
     * @return is left paren or not
     */
    public static boolean isLeftParen(final String token) {
        for (Paren each : Paren.values()) {
            if (each.leftParen.equals(token)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Judge left paren match right paren or not.
     * 
     * @param leftToken left token
     * @param rightToken right token
     * @return match or not
     */
    public static boolean match(final String leftToken, final String rightToken) {
        for (Paren each : Paren.values()) {
            if (each.leftParen.equals(leftToken) && each.rightParen.equals(rightToken)) {
                return true;
            }
        }
        return false;
    }
}
