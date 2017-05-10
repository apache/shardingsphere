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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleLexer;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;
import com.google.common.base.Optional;

/**
 * Oracle解析器.
 *
 * @author zhangliang
 */
public final class OracleParser extends SQLParser {
    
    public OracleParser(final String sql, final ShardingRule shardingRule) {
        super(new OracleLexer(sql), shardingRule);
        getLexer().nextToken();
    }
    
    @Override
    public Optional<String> parseAlias() {
        if (equalAny(OracleKeyword.CONNECT)) {
            return Optional.absent();
        }
        return super.parseAlias();
    }
}
