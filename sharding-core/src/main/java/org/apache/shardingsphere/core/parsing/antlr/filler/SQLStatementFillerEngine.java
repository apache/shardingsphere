/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.parsing.antlr.filler;

import java.util.Collection;

import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parsing.antlr.filler.common.SQLSegmentCommonFiller;
import org.apache.shardingsphere.core.parsing.antlr.filler.encrypt.SQLStatementEncryptFiller;
import org.apache.shardingsphere.core.parsing.antlr.filler.sharding.SQLSegmentShardingFiller;
import org.apache.shardingsphere.core.parsing.antlr.rule.registry.ParsingRuleRegistry;
import org.apache.shardingsphere.core.parsing.antlr.rule.registry.statement.SQLStatementRule;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.SQLStatementFillerRule;
import org.apache.shardingsphere.core.rule.ShardingRule;

import com.google.common.base.Optional;

import lombok.SneakyThrows;

/**
 * SQL statement filler engine.
 *
 * @author zhangliang
 * @author panjuan
 * @author duhongjun
 */
public final class SQLStatementFillerEngine {
    
    private final ParsingRuleRegistry parsingRuleRegistry;
    
    private final DatabaseType databaseType;
    
    private final String sql;
    
    private final SQLStatementFillerRule sqlStatementFillerRule;
    
    private final ShardingTableMetaData shardingTableMetaData;
    
    public SQLStatementFillerEngine(final ParsingRuleRegistry parsingRuleRegistry, final DatabaseType databaseType, final String sql, final SQLStatementFillerRule sqlStatementFillerRule,
                                    final ShardingTableMetaData shardingTableMetaData) {
        this.parsingRuleRegistry = parsingRuleRegistry;
        this.databaseType = databaseType;
        this.sql = sql;
        this.sqlStatementFillerRule = sqlStatementFillerRule;
        this.shardingTableMetaData = shardingTableMetaData;
    }
    
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
            Optional<SQLSegmentFiller> filler = parsingRuleRegistry.findSQLSegmentFiller(databaseType, each.getClass());
            if (filler.isPresent()) {
                if (filler.get() instanceof SQLSegmentCommonFiller) {
                    ((SQLSegmentCommonFiller<SQLSegment>) filler.get()).fill(each, result, sql, shardingTableMetaData);
                } else if (filler.get() instanceof SQLSegmentShardingFiller) {
                    ((SQLSegmentShardingFiller<SQLSegment>) filler.get()).fill(each, result, sql, (ShardingRule) sqlStatementFillerRule, shardingTableMetaData);
                } else if (filler.get() instanceof SQLStatementEncryptFiller) {
                    ((SQLStatementEncryptFiller<SQLSegment>) filler.get()).fill(each, result, sql, (EncryptRule) sqlStatementFillerRule, shardingTableMetaData);
                }
            }
        }
        return result;
    }
}
