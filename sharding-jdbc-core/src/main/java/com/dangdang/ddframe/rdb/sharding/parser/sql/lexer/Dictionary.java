/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parser.sql.lexer;

import java.util.HashMap;
import java.util.Map;

/**
 * 词法解析字典.
 *
 * @author zhangliang
 */
public final class Dictionary {
    
    private final Map<String, Token> tokens = new HashMap<>(1024);
    
    /**
     * 填充字典.
     * 
     * @param dialectKeywords 方言关键词
     */
    public void fill(final Keyword... dialectKeywords) {
        for (Symbol each : Symbol.values()) {
            tokens.put(each.getLiterals(), each);
        }
        for (DataType each : DataType.values()) {
            tokens.put(each.name(), each);
        }
        for (DefaultKeyword each : DefaultKeyword.values()) {
            tokens.put(each.name(), each);
        }
        for (Keyword each : dialectKeywords) {
            tokens.put(each.toString(), each);
        }
    }
    
    Token getToken(final String literals, final Token defaultToken) {
        String key = null == literals ? null : literals.toUpperCase();
        return tokens.containsKey(key) ? tokens.get(key) : defaultToken;
    }
    
    Token getToken(final String literals) {
        String key = null == literals ? null : literals.toUpperCase();
        if (tokens.containsKey(key)) {
            return tokens.get(key);
        }
        throw new IllegalArgumentException();
    }
}
