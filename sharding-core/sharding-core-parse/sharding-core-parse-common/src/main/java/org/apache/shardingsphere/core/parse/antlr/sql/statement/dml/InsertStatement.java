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

package org.apache.shardingsphere.core.parse.antlr.sql.statement.dml;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.GeneratedKeyCondition;
import org.apache.shardingsphere.core.parse.old.parser.context.insertvalue.InsertValues;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
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
    
    private final Collection<String> columnNames = new LinkedList<>();
    
    private List<GeneratedKeyCondition> generatedKeyConditions = new LinkedList<>();
    
    private final InsertValues insertValues = new InsertValues();
    
    private boolean containGenerateKey;
    
    /**
     * Is contain generate key column.
     * 
     * @param shardingRule sharding rule.
     * @return contain generated key column or not.
     */
    public boolean isContainGenerateKeyColumn(final ShardingRule shardingRule) {
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(getTables().getSingleTableName());
        return generateKeyColumnName.isPresent() && columnNames.contains(generateKeyColumnName.get());
    }
}
