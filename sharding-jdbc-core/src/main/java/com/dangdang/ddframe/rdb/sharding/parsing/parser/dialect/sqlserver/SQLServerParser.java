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
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.sqlserver.SQLServerLexer;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.SelectStatement;
import com.google.common.base.Optional;

/**
 * SQLServer解析器.
 *
 * @author zhangliang
 */
public final class SQLServerParser extends SQLParser {
    
    public SQLServerParser(final String sql, final ShardingRule shardingRule) {
        super(new SQLServerLexer(sql), shardingRule);
    }
    
    @Override
    protected boolean isRowNumberCondition(final SelectStatement selectStatement, final String columnLabel) {
        Optional<String> rowNumberAlias = Optional.absent();
        for (SelectItem each : selectStatement.getItems()) {
            if (each.getAlias().isPresent() && "ROW_NUMBER".equalsIgnoreCase(each.getExpression())) {
                rowNumberAlias = each.getAlias();
            }
        }
        return columnLabel.equalsIgnoreCase(rowNumberAlias.orNull());
    }
}
