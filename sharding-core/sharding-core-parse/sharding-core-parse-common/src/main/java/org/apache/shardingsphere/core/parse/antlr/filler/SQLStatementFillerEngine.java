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

package org.apache.shardingsphere.core.parse.antlr.filler;

import com.google.common.base.Optional;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.antlr.filler.api.EncryptRuleAwareFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.api.ShardingRuleAwareFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.api.ShardingTableMetaDataAwareFiller;
import org.apache.shardingsphere.core.parse.antlr.rule.registry.ParsingRuleRegistry;
import org.apache.shardingsphere.core.parse.antlr.rule.registry.statement.SQLStatementRule;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.SQLSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.rule.BaseRule;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;

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
    
    private final BaseRule rule;
    
    private final ShardingTableMetaData shardingTableMetaData;
    
    public SQLStatementFillerEngine(final ParsingRuleRegistry parsingRuleRegistry, 
                                    final DatabaseType databaseType, final String sql, final BaseRule rule, final ShardingTableMetaData shardingTableMetaData) {
        this.parsingRuleRegistry = parsingRuleRegistry;
        this.databaseType = databaseType;
        this.sql = sql;
        this.rule = rule;
        this.shardingTableMetaData = shardingTableMetaData;
    }
    
    /**
     * Fill SQL statement.
     *
     * @param sqlSegments SQL segments
     * @param rule SQL statement rule
     * @return SQL statement
     */
    @SneakyThrows
    public SQLStatement fill(final Collection<SQLSegment> sqlSegments, final SQLStatementRule rule) {
        SQLStatement result = rule.getSqlStatementClass().newInstance();
        result.setLogicSQL(sql);
        for (SQLSegment each : sqlSegments) {
            Optional<SQLSegmentFiller> filler = parsingRuleRegistry.findSQLSegmentFiller(databaseType, each.getClass());
            if (filler.isPresent()) {
                doFill(each, result, filler.get());
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private void doFill(final SQLSegment sqlSegment, final SQLStatement sqlStatement, final SQLSegmentFiller filler) {
        if (filler instanceof ShardingRuleAwareFiller) {
            ((ShardingRuleAwareFiller) filler).setShardingRule((ShardingRule) this.rule);
        }
        if (filler instanceof EncryptRuleAwareFiller) {
            ((EncryptRuleAwareFiller) filler).setEncryptRule((EncryptRule) this.rule);
        }
        if (filler instanceof ShardingTableMetaDataAwareFiller) {
            ((ShardingTableMetaDataAwareFiller) filler).setShardingTableMetaData(shardingTableMetaData);
        }
        filler.fill(sqlSegment, sqlStatement);
    }
}
