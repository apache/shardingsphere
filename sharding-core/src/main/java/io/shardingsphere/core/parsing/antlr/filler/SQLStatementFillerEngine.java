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

package io.shardingsphere.core.parsing.antlr.filler;

import com.google.common.base.Optional;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.rule.registry.ParsingRuleRegistry;
import io.shardingsphere.core.parsing.antlr.rule.registry.statement.SQLStatementRule;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.Collection;

/**
 * SQL statement filler engine.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SQLStatementFillerEngine {
    
    private final ParsingRuleRegistry parsingRuleRegistry = ParsingRuleRegistry.getInstance();
    
    private final String sql;
    
    private final ShardingRule shardingRule;
    
    private final ShardingTableMetaData shardingTableMetaData;
    
    /**
     * Fill SQL statement.
     *
     * @param sqlSegments SQL segments
     * @param rule SQL statement rule
     * @return SQL statement
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public SQLStatement fill(final Collection<SQLSegment> sqlSegments, final SQLStatementRule rule) {
        SQLStatement result = rule.getSqlStatementClass().newInstance();
        for (SQLSegment each : sqlSegments) {
            Optional<SQLStatementFiller> filler = parsingRuleRegistry.findSQLStatementFiller(each.getClass());
            if (filler.isPresent()) {
                filler.get().fill(each, result, sql, shardingRule, shardingTableMetaData);
            }
        }
        return result;
    }
}
