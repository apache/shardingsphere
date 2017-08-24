package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * SELECT剩余语句解析器.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public class SelectRestClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    /**
     * 解析剩余语句.
     */
    public final void parse() {
        Collection<Keyword> unsupportedRestKeywords = new LinkedList<>();
        unsupportedRestKeywords.addAll(Arrays.asList(DefaultKeyword.UNION, DefaultKeyword.INTERSECT, DefaultKeyword.EXCEPT, DefaultKeyword.MINUS));
        unsupportedRestKeywords.addAll(Arrays.asList(getUnsupportedKeywordsRest()));
        lexerEngine.unsupportedIfEqual(unsupportedRestKeywords.toArray(new Keyword[unsupportedRestKeywords.size()]));
    }
    
    protected Keyword[] getUnsupportedKeywordsRest() {
        return new Keyword[0];
    }
}
