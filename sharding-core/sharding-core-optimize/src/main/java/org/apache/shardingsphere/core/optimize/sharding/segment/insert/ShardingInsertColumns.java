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
public final class ShardingInsertColumns {
    
    @Getter
    private final Collection<String> regularColumnNames;
    
    public ShardingInsertColumns(final ShardingRule shardingRule, final TableMetas tableMetas, final InsertStatement insertStatement) {
        regularColumnNames = createRegularColumnNames(shardingRule, tableMetas, insertStatement);
    }
    
    private Collection<String> createRegularColumnNames(final ShardingRule shardingRule, final TableMetas tableMetas, final InsertStatement insertStatement) {
        String tableName = insertStatement.getTable().getTableName();
        Collection<String> result = new LinkedHashSet<>(insertStatement.useDefaultColumns() ? tableMetas.getAllColumnNames(tableName) : insertStatement.getColumnNames());
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(tableName);
        if (generateKeyColumnName.isPresent() && containsDefaultValue(result, insertStatement.getValueCountForPerGroup())) {
            result.remove(generateKeyColumnName.get());
        }
        return result;
    }
    
    private boolean containsDefaultValue(final Collection<String> columnNames, final int valueCountForPerValuesGroup) {
        return columnNames.size() > valueCountForPerValuesGroup;
    }
}
