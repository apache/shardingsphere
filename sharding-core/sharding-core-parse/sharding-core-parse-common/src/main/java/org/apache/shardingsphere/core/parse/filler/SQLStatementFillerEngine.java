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

package org.apache.shardingsphere.core.parse.filler;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.aware.EncryptRuleAware;
import org.apache.shardingsphere.core.parse.aware.ShardingRuleAware;
import org.apache.shardingsphere.core.parse.aware.ShardingTableMetaDataAware;
import org.apache.shardingsphere.core.parse.rule.registry.ParseRuleRegistry;
import org.apache.shardingsphere.core.parse.rule.registry.statement.SQLStatementRule;
import org.apache.shardingsphere.core.parse.sql.segment.SQLSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.rule.BaseRule;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.spi.database.DatabaseType;

import java.util.Collection;

/**
 * SQL statement filler engine.
 *
 * @author zhangliang
 * @author panjuan
 * @author duhongjun
 */
@RequiredArgsConstructor
public final class SQLStatementFillerEngine {
    
    private final ParseRuleRegistry parseRuleRegistry;
    
    private final DatabaseType databaseType;
    
    private final String sql;
    
    private final BaseRule rule;
    
    private final ShardingTableMetaData shardingTableMetaData;
    
    /**
     * Fill SQL statement.
     *
     * @param sqlSegments SQL segments
     * @param parameterMarkerCount parameter marker count
     * @param rule SQL statement rule
     * @return SQL statement
     */
    @SneakyThrows
    public SQLStatement fill(final Collection<SQLSegment> sqlSegments, final int parameterMarkerCount, final SQLStatementRule rule) {
        SQLStatement result = rule.getSqlStatementClass().newInstance();
        result.setLogicSQL(sql);
        result.setParametersCount(parameterMarkerCount);
        result.getSQLSegments().addAll(sqlSegments);
        for (SQLSegment each : sqlSegments) {
            Optional<SQLSegmentFiller> filler = parseRuleRegistry.findSQLSegmentFiller(databaseType, each.getClass());
            if (filler.isPresent()) {
                doFill(each, result, filler.get());
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private void doFill(final SQLSegment sqlSegment, final SQLStatement sqlStatement, final SQLSegmentFiller filler) {
        if (filler instanceof ShardingRuleAware && rule instanceof ShardingRule) {
            ((ShardingRuleAware) filler).setShardingRule((ShardingRule) rule);
        }
        if (filler instanceof EncryptRuleAware && rule instanceof EncryptRule) {
            ((EncryptRuleAware) filler).setEncryptRule((EncryptRule) rule);
        }
        if (filler instanceof ShardingTableMetaDataAware) {
            ((ShardingTableMetaDataAware) filler).setShardingTableMetaData(shardingTableMetaData);
        }
        filler.fill(sqlSegment, sqlStatement);
    }
}
