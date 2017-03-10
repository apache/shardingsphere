package com.alibaba.druid.sql.lexer;


import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 词法解析字典.
 *
 * @author zhangliang
 */
@Getter
public class Dictionary {
    
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
}
