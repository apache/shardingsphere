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

package com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.oracle.parser;

import com.dangdang.ddframe.rdb.sharding.parser.sql.context.OrderByContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.SQLContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.oracle.lexer.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.oracle.lexer.OracleLexer;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.ParserUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.google.common.base.Optional;

import java.util.List;

public class OracleParser extends SQLParser {
    
    public OracleParser(final String sql, final ShardingRule shardingRule, final List<Object> parameters) {
        super(new OracleLexer(sql), shardingRule, parameters);
        getLexer().nextToken();
    }
    
    @Override
    public Optional<String> parseAlias() {
        if (equalAny(OracleKeyword.CONNECT)) {
            return Optional.absent();
        }
        return super.parseAlias();
    }
    
    @Override
    protected Optional<OrderByContext> parseSelectOrderByItem(final SQLContext sqlContext) {
        Optional<OrderByContext> result = super.parseSelectOrderByItem(sqlContext);
        skipAfterOrderByItem();
        return result;
    }
    
    private void skipAfterOrderByItem() {
        if (skipIfEqual(OracleKeyword.NULLS)) {
            getLexer().nextToken();
            if (!skipIfEqual(OracleKeyword.FIRST, OracleKeyword.LAST)) {
                throw new ParserUnsupportedException(getLexer().getCurrentToken().getType());
            }
        }
    }
}
