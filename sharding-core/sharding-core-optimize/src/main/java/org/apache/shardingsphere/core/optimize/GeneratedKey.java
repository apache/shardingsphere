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

package org.apache.shardingsphere.core.optimize;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.parser.context.condition.GeneratedKeyCondition;
import org.apache.shardingsphere.core.parse.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.LinkedList;
import java.util.List;

/**
 * Generated key.
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class GeneratedKey {
    
    private final String columnName;
    
    private final List<Comparable<?>> generatedKeys = new LinkedList<>();
    
    /**
     * Get generate key.
     * 
     * @param shardingRule sharding rule
     * @param parameters SQL parameters
     * @param insertStatement insert statement
     * @return generate key
     */
    public static Optional<GeneratedKey> getGenerateKey(final ShardingRule shardingRule, final List<Object> parameters, final InsertStatement insertStatement) {
        return insertStatement.isContainGenerateKeyColumn(shardingRule) ? findGeneratedKey(parameters, insertStatement) : createGeneratedKey(shardingRule, insertStatement);
    }
    
    private static Optional<GeneratedKey> createGeneratedKey(final ShardingRule shardingRule, final InsertStatement insertStatement) {
        String tableName = insertStatement.getTables().getSingleTableName();
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(tableName);
        return generateKeyColumnName.isPresent()
                ? Optional.of(createGeneratedKey(shardingRule, generateKeyColumnName.get(), tableName, insertStatement.getInsertValues().getInsertValues().size())) : Optional.<GeneratedKey>absent();
    }
    
    private static GeneratedKey createGeneratedKey(final ShardingRule shardingRule, final String generateKeyColumnName, final String generateKeyTableName, final int insertValueSize) {
        GeneratedKey result = new GeneratedKey(generateKeyColumnName);
        for (int i = 0; i < insertValueSize; i++) {
            result.getGeneratedKeys().add(shardingRule.generateKey(generateKeyTableName));
        }
        return result;
    }
    
    private static Optional<GeneratedKey> findGeneratedKey(final List<Object> parameters, final InsertStatement insertStatement) {
        GeneratedKey result = null;
        for (GeneratedKeyCondition each : insertStatement.getGeneratedKeyConditions()) {
            if (null == result) {
                result = new GeneratedKey(each.getColumn().getName());
            }
            result.getGeneratedKeys().add(-1 == each.getIndex() ? each.getValue() : (Comparable<?>) parameters.get(each.getIndex()));
        }
        return Optional.fromNullable(result);
    }
}
