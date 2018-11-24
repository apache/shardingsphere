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

package io.shardingsphere.core.parsing.antlr.extractor.sql.segment.filler;

import java.util.List;

import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.result.LimitExtractResult;
import io.shardingsphere.core.parsing.parser.context.limit.Limit;
import io.shardingsphere.core.parsing.parser.context.limit.LimitValue;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.rule.ShardingRule;

/**
 * Limit handler result filler.
 * 
 * @author duhongjun
 */
public class LimitHandlerResultFiller implements HandlerResultFiller {

    @Override
    public void fill(Object extractResult, SQLStatement statement, ShardingRule shardingRule,ShardingTableMetaData shardingTableMetaData) {
        @SuppressWarnings("unchecked")
        List<LimitExtractResult> limitResult = (List<LimitExtractResult>) extractResult;
        if(limitResult.isEmpty()) {
            return;
        }
        SelectStatement selectStatement = (SelectStatement)statement;
        Limit limit = new Limit(limitResult.get(0).getDatabaseType());
        limit.setOffset(new LimitValue(limitResult.get(0).getValue(), limitResult.get(0).getIndex(), false));
        if(1 < limitResult.size()) {
            limit.setRowCount(new LimitValue(limitResult.get(0).getValue(), limitResult.get(0).getIndex(), false));
        }
        selectStatement.setLimit(limit);
    }
}
