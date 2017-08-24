package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Distinct从句解析器.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public class DistinctClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    /**
     * 解析Distinct.
     */
    public final void parse() {
        lexerEngine.skipAll(DefaultKeyword.ALL);
        Collection<Keyword> distinctKeywords = new LinkedList<>();
        distinctKeywords.add(DefaultKeyword.DISTINCT);
        distinctKeywords.addAll(Arrays.asList(getSynonymousKeywordsForDistinct()));
        lexerEngine.unsupportedIfEqual(distinctKeywords.toArray(new Keyword[distinctKeywords.size()]));
    }
    
    protected Keyword[] getSynonymousKeywordsForDistinct() {
        return new Keyword[0];
    }
}
