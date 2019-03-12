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

package org.apache.shardingsphere.core.parsing.parser.sql.dml.insert;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.context.condition.GeneratedKeyCondition;
import org.apache.shardingsphere.core.parsing.parser.context.insertvalue.InsertValues;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.DMLStatement;
import org.apache.shardingsphere.core.parsing.parser.token.InsertValuesToken;
import org.apache.shardingsphere.core.parsing.parser.token.SQLToken;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.LinkedList;
import java.util.List;

/**
 * Insert statement.
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
@Getter
@Setter
@ToString(callSuper = true)
public final class InsertStatement extends DMLStatement {
    
    private final List<Column> columns = new LinkedList<>();
    
    private List<GeneratedKeyCondition> generatedKeyConditions = new LinkedList<>();
    
    private final InsertValues insertValues = new InsertValues();
    
    private int insertValuesListLastIndex;
    
    /**
     * Get insert values token.
     * 
     * @return insert values token
     */
    public InsertValuesToken getInsertValuesToken() {
        return (InsertValuesToken) Collections2.filter(getSQLTokens(), new Predicate<SQLToken>() {
            
            @Override
            public boolean apply(final SQLToken input) {
                return input instanceof InsertValuesToken;
            }
        }).iterator().next();
    }
    
    /**
     * Get insert column names.
     * 
     * @return insert column names
     */
    public List<String> getInsertColumnNames() {
        return Lists.transform(columns, new Function<Column, String>() {
            @Override
            public String apply(final Column input) {
                return input.getName();
            }
        });
    }
    
    /**
     * Is contain generate key column.
     * 
     * @param shardingRule sharding rule.
     * @return contain generated key column or not.
     */
    public boolean isContainGenerateKeyColumn(final ShardingRule shardingRule) {
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(getTables().getSingleTableName());
        return generateKeyColumnName.isPresent() && columns.contains(new Column(generateKeyColumnName.get(), getTables().getSingleTableName()));
    }
}
