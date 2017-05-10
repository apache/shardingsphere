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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.sqlserver.SQLServerKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.sqlserver.SQLServerLexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Literals;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.LimitContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SQLServerTop;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SelectSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;

/**
 * SQLServer解析器.
 *
 * @author zhangliang
 */
public final class SQLServerParser extends SQLParser {
    
    public SQLServerParser(final String sql, final ShardingRule shardingRule) {
        super(new SQLServerLexer(sql), shardingRule);
        getLexer().nextToken();
    }
    
    public SQLServerTop parseTop() {
        // TODO
//        if (getLexer().equalAny(SQLServerKeyword.TOP)) {
//            SQLServerTop top = new SQLServerTop();
//            getLexer().nextToken();
//            
//            boolean paren = false;
//            if (getLexer().equalAny(Symbol.LEFT_PAREN)) {
//                paren = true;
//                getLexer().nextToken();
//            }
//            
//            top.setExpr(primary());
//            
//            if (paren) {
//                getLexer().accept(Symbol.RIGHT_PAREN);
//            }
//            
//            if (getLexer().equalAny(SQLServerKeyword.PERCENT)) {
//                getLexer().nextToken();
//                top.setPercent(true);
//            }
//            
//            return top;
//        }
        return null;
    }
    
    protected void skipOutput() {
        if (equalAny(SQLServerKeyword.OUTPUT)) {
            throw new SQLParsingUnsupportedException(SQLServerKeyword.OUTPUT);
        }
    }
    
    public void parseOffset(final SelectSQLContext sqlContext) {
        getLexer().nextToken();
        int offset;
        int offsetIndex = -1;
        if (equalAny(Literals.INT)) {
            offset = Integer.parseInt(getLexer().getCurrentToken().getLiterals());
        } else if (equalAny(Symbol.QUESTION)) {
            offsetIndex = getParametersIndex();
            offset = -1;
            setParametersIndex(offsetIndex + 1);
        } else {
            throw new SQLParsingException(getLexer());
        }
        getLexer().nextToken();
        LimitContext limitContext;
        if (skipIfEqual(DefaultKeyword.FETCH)) {
            getLexer().nextToken();
            int rowCount;
            int rowCountIndex = -1;
            getLexer().nextToken();
            if (equalAny(Literals.INT)) {
                rowCount = Integer.parseInt(getLexer().getCurrentToken().getLiterals());
            } else if (equalAny(Symbol.QUESTION)) {
                rowCountIndex = getParametersIndex();
                rowCount = -1;
                setParametersIndex(rowCountIndex + 1);
            } else {
                throw new SQLParsingException(getLexer());
            }
            getLexer().nextToken();
            getLexer().nextToken();
            limitContext = new LimitContext(offset, rowCount, offsetIndex, rowCountIndex);
        } else {
            limitContext = new LimitContext(offset, offsetIndex);
        }
        sqlContext.setLimitContext(limitContext);
    }
}
