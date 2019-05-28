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

package org.apache.shardingsphere.core.rewrite.token;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.AggregationSelectItem;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.DerivedCommonSelectItem;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.SelectItem;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.sql.token.SQLToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.SelectItemsToken;
import org.apache.shardingsphere.core.rule.BaseRule;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL token generator for sharding.
 *
 * @author zhangliang
 */
public final class ShardingTokenGenerator implements SQLTokenGenerator {
    
    @Override
    public List<SQLToken> generateSQLTokens(final SQLStatement sqlStatement, final BaseRule rule, final ShardingTableMetaData shardingTableMetaData) {
        List<SQLToken> result = new LinkedList<>(sqlStatement.getSQLTokens());
        if (sqlStatement instanceof SelectStatement) {
            Optional<SelectItemsToken> selectItemsToken = generateSelectItemsToken((SelectStatement) sqlStatement);
            if (selectItemsToken.isPresent()) {
                result.add(selectItemsToken.get());
            }
        }
        Collections.sort(result);
        return result;
    }
    
    private Optional<SelectItemsToken> generateSelectItemsToken(final SelectStatement selectStatement) {
        SelectItemsToken result = new SelectItemsToken(selectStatement.getSelectListStopIndex() + 1 + " ".length());
        for (SelectItem each : selectStatement.getItems()) {
            if (each instanceof AggregationSelectItem && !((AggregationSelectItem) each).getDerivedAggregationSelectItems().isEmpty()) {
                result.getItems().addAll(Lists.transform(((AggregationSelectItem) each).getDerivedAggregationSelectItems(), new Function<AggregationSelectItem, String>() {
                    
                    @Override
                    public String apply(final AggregationSelectItem input) {
                        return getDerivedItemText(input);
                    }
                }));
            } else if (each instanceof DerivedCommonSelectItem) {
                result.getItems().add(getDerivedItemText(each));
            }
        }
        return result.getItems().isEmpty() ? Optional.<SelectItemsToken>absent() : Optional.of(result);
    }
    
    private String getDerivedItemText(final SelectItem selectItem) {
        Preconditions.checkState(selectItem.getAlias().isPresent());
        return selectItem.getExpression() + " AS " + selectItem.getAlias().get() + " ";
    }
}
