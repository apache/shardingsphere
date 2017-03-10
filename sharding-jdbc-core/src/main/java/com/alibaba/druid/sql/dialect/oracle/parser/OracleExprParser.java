/*
 * Copyright 1999-2101 Alibaba Group Holding Ltd.
 *
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
 */

package com.alibaba.druid.sql.dialect.oracle.parser;

import com.alibaba.druid.sql.context.OrderByContext;
import com.alibaba.druid.sql.context.SQLContext;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.sql.parser.SQLExprParser;
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
        if (getLexer().equalToken(Token.CONNECT)) {
            return null;
        }
        return super.as();
    }
    
    protected boolean isCharType(final String dataTypeName) {
        return "varchar2".equalsIgnoreCase(dataTypeName) || "nvarchar2".equalsIgnoreCase(dataTypeName)
                || "char".equalsIgnoreCase(dataTypeName) || "varchar".equalsIgnoreCase(dataTypeName) 
                || "nchar".equalsIgnoreCase(dataTypeName) || "nvarchar".equalsIgnoreCase(dataTypeName);
    }
    
    @Override
    public OrderByContext parseSelectOrderByItem(final SQLContext sqlContext) {
        OrderByContext result = super.parseSelectOrderByItem(sqlContext);
        if (getLexer().skipIfEqual(Token.NULLS)) {
            getLexer().nextToken();
            if (getLexer().identifierEquals("FIRST")) {
                getLexer().nextToken();
            } else if (getLexer().identifierEquals("LAST")) {
                getLexer().nextToken();
            } else {
                throw new ParserUnsupportedException(getLexer().getToken());
            }
        }
        return result;
    }
}
