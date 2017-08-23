package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle;

import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.OrderByClauseParser;

/**
 * Oracle排序从句解析器.
 *
 * @author zhangliang
 */
public final class OracleOrderByClauseParser extends OrderByClauseParser {
    
    public OracleOrderByClauseParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected OrderType getNullOrderType() {
        if (!getLexerEngine().skipIfEqual(OracleKeyword.NULLS)) {
            return OrderType.ASC;
        }
        if (getLexerEngine().skipIfEqual(OracleKeyword.FIRST)) {
            return OrderType.ASC;
        }
        if (getLexerEngine().skipIfEqual(OracleKeyword.LAST)) {
            return OrderType.DESC;
        }
        throw new SQLParsingException(getLexerEngine());
    }
}
