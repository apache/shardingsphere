/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parsing.parser.context;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLNumberExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLPlaceholderExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.GeneratedKeyToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.ItemsToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.SQLToken;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Insert SQL上下文.
 *
 * @author zhangliang
 */
@Getter
@Setter
public final class InsertSQLContext extends AbstractSQLContext {
    
    private final Collection<ShardingColumnContext> shardingColumnContexts = new LinkedList<>();
    
    private GeneratedKeyContext generatedKeyContext;
    
    private int columnsListLastPosition;
    
    private int valuesListLastPosition;
    
    public InsertSQLContext() {
        super(SQLType.INSERT);
    }
    
    /**
     * 追加自增主键标记对象.
     *
     * @param shardingRule 分片规则
     * @param parametersSize 参数个数
     */
    public void appendGenerateKeyToken(final ShardingRule shardingRule, final int parametersSize) {
        if (null != generatedKeyContext) {
            return;
        }
        Optional<TableRule> tableRule = shardingRule.tryFindTableRule(getTables().get(0).getName());
        if (!tableRule.isPresent()) {
            return;
        }
        Optional<GeneratedKeyToken> generatedKeysToken = findGeneratedKeyToken();
        if (!generatedKeysToken.isPresent()) {
            return;
        }
        ItemsToken valuesToken = new ItemsToken(generatedKeysToken.get().getBeginPosition());
        if (0 == parametersSize) {
            appendGenerateKeyToken(shardingRule, tableRule.get(), valuesToken);
        } else {
            appendGenerateKeyToken(shardingRule, tableRule.get(), valuesToken, parametersSize);
        }
        getSqlTokens().remove(generatedKeysToken.get());
        getSqlTokens().add(valuesToken);
    }
    
    private void appendGenerateKeyToken(final ShardingRule shardingRule, final TableRule tableRule, final ItemsToken valuesToken) {
        Number generatedKey = shardingRule.generateKey(tableRule.getLogicTable());
        valuesToken.getItems().add(generatedKey.toString());
        addCondition(shardingRule, new ShardingColumnContext(tableRule.getGenerateKeyColumn(), tableRule.getLogicTable(), true), new SQLNumberExpr(generatedKey));
        generatedKeyContext = new GeneratedKeyContext(tableRule.getLogicTable(), -1, generatedKey);
    }
    
    private void appendGenerateKeyToken(final ShardingRule shardingRule, final TableRule tableRule, final ItemsToken valuesToken, final int parametersSize) {
        valuesToken.getItems().add("?");
        addCondition(shardingRule, new ShardingColumnContext(tableRule.getGenerateKeyColumn(), tableRule.getLogicTable(), true), new SQLPlaceholderExpr(parametersSize));
        generatedKeyContext = new GeneratedKeyContext(tableRule.getGenerateKeyColumn(), parametersSize);
    }
    
    private Optional<GeneratedKeyToken> findGeneratedKeyToken() {
        for (SQLToken each : getSqlTokens()) {
            if (each instanceof GeneratedKeyToken) {
                return Optional.of((GeneratedKeyToken) each);
            }
        }
        return Optional.absent();
    }
    
    private void addCondition(final ShardingRule shardingRule, final ShardingColumnContext shardingColumnContext, final SQLExpr sqlExpr) {
        if (shardingRule.isShardingColumn(shardingColumnContext)) {
            getConditionContext().add(new ConditionContext.Condition(shardingColumnContext, sqlExpr));
        }
    }
}
