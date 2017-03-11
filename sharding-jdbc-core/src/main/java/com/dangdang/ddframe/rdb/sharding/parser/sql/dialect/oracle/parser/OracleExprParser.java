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
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.SQLExprParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.google.common.base.Optional;

import java.util.List;

public class OracleExprParser extends SQLExprParser {
    
    public OracleExprParser(final ShardingRule shardingRule, final List<Object> parameters, final String sql) {
        super(shardingRule, parameters, new OracleLexer(sql));
        getLexer().nextToken();
    }
    
    @Override
    protected Optional<String> as() {
        if (getLexer().equalToken(OracleKeyword.CONNECT)) {
            return null;
        }
        return super.as();
    }
    
    @Override
    public OrderByContext parseSelectOrderByItem(final SQLContext sqlContext) {
        OrderByContext result = super.parseSelectOrderByItem(sqlContext);
        if (getLexer().skipIfEqual(OracleKeyword.NULLS)) {
            getLexer().nextToken();
            if (!getLexer().skipIfEqual(OracleKeyword.FIRST, OracleKeyword.LAST)) {
                throw new ParserUnsupportedException(getLexer().getToken());
            }
        }
        return result;
    }
}
