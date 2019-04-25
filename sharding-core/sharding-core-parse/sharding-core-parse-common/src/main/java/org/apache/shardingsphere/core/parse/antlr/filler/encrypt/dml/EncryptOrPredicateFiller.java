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

package org.apache.shardingsphere.core.parse.antlr.filler.encrypt.dml;

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.antlr.filler.api.EncryptRuleAwareFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.api.ShardingTableMetaDataAwareFiller;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.AndPredicateSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.token.EncryptColumnToken;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Column;
import org.apache.shardingsphere.core.parse.old.parser.context.table.Table;
import org.apache.shardingsphere.core.parse.old.parser.context.table.Tables;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;
import java.util.HashSet;

/**
 * Or predicate filler for encrypt.
 *
 * @author duhongjun
 */
@Setter
public final class EncryptOrPredicateFiller implements SQLSegmentFiller<OrPredicateSegment>, EncryptRuleAwareFiller, ShardingTableMetaDataAwareFiller {
    
    private EncryptRule encryptRule;
    
    private ShardingTableMetaData shardingTableMetaData;
    
    @Override
    public void fill(final OrPredicateSegment sqlSegment, final SQLStatement sqlStatement) {
        Collection<Integer> stopIndexes = new HashSet<>();
        for (AndPredicateSegment each : sqlSegment.getAndPredicates()) {
            for (PredicateSegment predicate : each.getPredicates()) {
                if (stopIndexes.add(predicate.getStopIndex())) {
                    fill(new Column(predicate.getColumn().getName(), getTableName(predicate, sqlStatement)), predicate, sqlStatement);
                }
            }
        }
    }
    
    private void fill(final Column column, final PredicateSegment predicate, final SQLStatement sqlStatement) {
        // TODO panjuan: spilt EncryptRule and EncryptorEngine, cannot pass EncryptorEngine to parse module
        if (!encryptRule.getEncryptorEngine().getShardingEncryptor(column.getTableName(), column.getName()).isPresent()) {
            return;
        }
        AndCondition andCondition;
        if (sqlStatement.getEncryptConditions().getOrCondition().getAndConditions().isEmpty()) {
            andCondition = new AndCondition();
            sqlStatement.getEncryptConditions().getOrCondition().getAndConditions().add(andCondition);
        } else {
            andCondition = sqlStatement.getEncryptConditions().getOrCondition().getAndConditions().get(0);
        }
        andCondition.getConditions().add(predicate.getExpression().buildCondition(column, sqlStatement.getLogicSQL()));
        sqlStatement.getSQLTokens().add(new EncryptColumnToken(predicate.getColumn().getStartIndex(), predicate.getStopIndex(), column, true));
    }
    
    // TODO hongjun: find table from parent select statement, should find table in subquery level only
    private String getTableName(final PredicateSegment predicateSegment, final SQLStatement sqlStatement) {
        if (!(sqlStatement instanceof SelectStatement)) {
            return sqlStatement.getTables().getSingleTableName();
        }
        SelectStatement currentSelectStatement = (SelectStatement) sqlStatement;
        while (null != currentSelectStatement.getParentStatement()) {
            currentSelectStatement = currentSelectStatement.getParentStatement();
            String tableName = getTableName(predicateSegment, currentSelectStatement.getTables());
            if (!"".equals(tableName)) {
                return tableName;
            }
        }
        return getTableName(predicateSegment, currentSelectStatement.getTables());
    }
    
    private String getTableName(final PredicateSegment predicateSegment, final Tables tables) {
        if (predicateSegment.getColumn().getOwner().isPresent()) {
            Optional<Table> table = tables.find(predicateSegment.getColumn().getOwner().get());
            return table.isPresent() ? table.get().getName() : "";
        } else {
            return getTableNameFromMetaData(predicateSegment.getColumn().getName(), tables);
        }
    }
    
    private String getTableNameFromMetaData(final String columnName, final Tables tables) {
        for (String each : tables.getTableNames()) {
            if (shardingTableMetaData.containsColumn(each, columnName)) {
                return each;
            }
        }
        return "";
    }
}
