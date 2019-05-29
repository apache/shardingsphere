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

package org.apache.shardingsphere.core.rewrite.token.generator;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.parse.sql.context.table.Table;
import org.apache.shardingsphere.core.parse.sql.segment.OwnerAvailable;
import org.apache.shardingsphere.core.parse.sql.segment.SQLSegment;
import org.apache.shardingsphere.core.parse.sql.segment.TableAvailable;
import org.apache.shardingsphere.core.parse.sql.segment.common.TableSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.SelectItemsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ShorthandSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.rewrite.token.pojo.TableToken;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Table token generator.
 *
 * @author zhangliang
 */
public final class TableTokenGenerator implements CollectionSQLTokenGenerator<ShardingRule> {
    
    @Override
    public Collection<TableToken> generateSQLTokens(final SQLStatement sqlStatement, final ShardingRule shardingRule) {
        Collection<TableToken> result = new LinkedList<>();
        for (SQLSegment each : sqlStatement.getSqlSegments()) {
            if (each instanceof SelectItemsSegment) {
                result.addAll(createTableTokens(sqlStatement, shardingRule, (SelectItemsSegment) each));
            } else if (each instanceof ColumnSegment) {
                Optional<TableToken> tableToken = createTableToken(sqlStatement, shardingRule, (ColumnSegment) each);
                if (tableToken.isPresent()) {
                    result.add(tableToken.get());
                }
            } else if (each instanceof TableAvailable) {
                Optional<TableToken> tableToken = createTableToken(sqlStatement, shardingRule, (TableAvailable) each);
                if (tableToken.isPresent()) {
                    result.add(tableToken.get());
                }
            }
        }
        return result;
    }
    
    private Collection<TableToken> createTableTokens(final SQLStatement sqlStatement, final ShardingRule shardingRule, final SelectItemsSegment selectItemsSegment) {
        Collection<TableToken> result = new LinkedList<>();
        for (SelectItemSegment each : selectItemsSegment.getSelectItems()) {
            if (each instanceof ShorthandSelectItemSegment) {
                Optional<TableToken> tableToken = createTableToken(sqlStatement, shardingRule, (ShorthandSelectItemSegment) each);
                if (tableToken.isPresent()) {
                    result.add(tableToken.get());
                }
            }
        }
        return result;
    }
    
    private Optional<TableToken> createTableToken(final SQLStatement sqlStatement, final ShardingRule shardingRule, final OwnerAvailable<TableSegment> segment) {
        Optional<TableSegment> owner = segment.getOwner();
        if (!owner.isPresent()) {
            return Optional.absent();
        }
        Optional<Table> table = sqlStatement.getTables().find(owner.get().getName());
        if (table.isPresent() && !table.get().getAlias().isPresent() && shardingRule.findTableRule(table.get().getName()).isPresent()) {
            return Optional.of(new TableToken(owner.get().getStartIndex(), owner.get().getStopIndex(), owner.get().getName(), owner.get().getQuoteCharacter()));
        }
        return Optional.absent();
    }
    
    private Optional<TableToken> createTableToken(final SQLStatement sqlStatement, final ShardingRule shardingRule, final TableAvailable segment) {
        if (shardingRule.findTableRule(segment.getTableName()).isPresent() || !(sqlStatement instanceof SelectStatement)) {
            return Optional.of(new TableToken(segment.getStartIndex(), segment.getStopIndex(), segment.getTableName(), segment.getTableQuoteCharacter()));
        }
        return Optional.absent();
    }
}
