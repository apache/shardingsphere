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

package org.apache.shardingsphere.core.optimize.sharding.segment.insert;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.api.segment.InsertColumns;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Insert columns for sharding.
 *
 * @author zhangliang
 * @author panjuan
 */
@ToString
public final class ShardingInsertColumns implements InsertColumns {
    
    @Getter
    private final Collection<String> regularColumnNames;
    
    public ShardingInsertColumns(final ShardingRule shardingRule, final TableMetas tableMetas, final InsertStatement insertStatement) {
        regularColumnNames = insertStatement.useDefaultColumns()
                ? getRegularColumnNamesFromMetaData(shardingRule, tableMetas, insertStatement) : getRegularColumnNamesFromSQLStatement(shardingRule, insertStatement);
    }
    
    private Collection<String> getRegularColumnNamesFromMetaData(final ShardingRule shardingRule, final TableMetas tableMetas, final InsertStatement insertStatement) {
        Collection<String> allColumnNames = tableMetas.getAllColumnNames(insertStatement.getTable().getTableName());
        Collection<String> result = new LinkedHashSet<>(allColumnNames);
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(insertStatement.getTable().getTableName());
        if (generateKeyColumnName.isPresent() && isUseDefaultGenerateKeyFromMetaData(allColumnNames, insertStatement.getValueCountForPerGroup())) {
            result.remove(generateKeyColumnName.get());
        }
        return result;
    }
    
    private boolean isUseDefaultGenerateKeyFromMetaData(final Collection<String> allColumnNames, final int valueCountForPerGroup) {
        return allColumnNames.size() > valueCountForPerGroup;
    }
    
    private Collection<String> getRegularColumnNamesFromSQLStatement(final ShardingRule shardingRule, final InsertStatement insertStatement) {
        Collection<String> result = new LinkedHashSet<>(insertStatement.getColumnNames());
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(insertStatement.getTable().getTableName());
        if (generateKeyColumnName.isPresent() && isUseDefaultGenerateKeyFromSQLStatement(insertStatement)) {
            result.remove(generateKeyColumnName.get());
        }
        return result;
    }
    
    private boolean isUseDefaultGenerateKeyFromSQLStatement(final InsertStatement insertStatement) {
        return !insertStatement.getColumns().isEmpty() && insertStatement.getColumns().size() > insertStatement.getValueCountForPerGroup();
    }
}
