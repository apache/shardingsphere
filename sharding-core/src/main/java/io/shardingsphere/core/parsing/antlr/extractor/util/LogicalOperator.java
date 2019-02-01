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

import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Logical operator.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogicalOperator {
    
    /**
     * Judge is logical operator or not.
     *
     * @param token token
     * @return is logical operator or not
     */
    public static boolean isLogicalOperator(final String token) {
        return isAndOperator(token) || isOrOperator(token);
    }
    
    private static boolean isAndOperator(final String token) {
        return DefaultKeyword.AND.name().equalsIgnoreCase(token) || Symbol.DOUBLE_AMP.getLiterals().equalsIgnoreCase(token);
    }
    
    /**
     * Is OR operator or not.
     *
     * @param token token
     * @return OR operator or not
     */
    public static boolean isOrOperator(final String token) {
        return DefaultKeyword.OR.name().equalsIgnoreCase(token) || Symbol.DOUBLE_BAR.getLiterals().equalsIgnoreCase(token);
    }
}
