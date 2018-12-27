/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.antlr.filler.impl.ddl;

import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.filler.SQLStatementFiller;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.index.IndexSegment;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.token.IndexToken;
import io.shardingsphere.core.rule.ShardingRule;

/**
 * Index filler.
 *
 * @author duhongjun
 */
public final class IndexFiller implements SQLStatementFiller<IndexSegment> {
    
    @Override
    public void fill(final IndexSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        IndexToken indexToken = sqlSegment.getToken();
        if (!sqlStatement.getTables().isEmpty() && null == indexToken.getTableName()) {
            indexToken.setTableName(sqlStatement.getTables().getSingleTableName());
        } else {
            indexToken.setTableName("");
        }
        sqlStatement.getSQLTokens().add(sqlSegment.getToken());
    }
}
