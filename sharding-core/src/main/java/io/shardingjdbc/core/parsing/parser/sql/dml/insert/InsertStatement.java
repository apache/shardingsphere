/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.parsing.parser.sql.dml.insert;

import com.google.common.base.Optional;
import io.shardingjdbc.core.parsing.lexer.token.Symbol;
import io.shardingjdbc.core.parsing.parser.context.GeneratedKey;
import io.shardingjdbc.core.parsing.parser.context.condition.Column;
import io.shardingjdbc.core.parsing.parser.context.condition.Condition;
import io.shardingjdbc.core.parsing.parser.context.condition.Conditions;
import io.shardingjdbc.core.parsing.parser.expression.SQLNumberExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLPlaceholderExpression;
import io.shardingjdbc.core.parsing.parser.sql.dml.DMLStatement;
import io.shardingjdbc.core.parsing.parser.token.GeneratedKeyToken;
import io.shardingjdbc.core.parsing.parser.token.ItemsToken;
import io.shardingjdbc.core.parsing.parser.token.SQLToken;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.rule.TableRule;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert statement.
 *
 * @author zhangliang
 */
@Getter
@Setter
@ToString
public final class InsertStatement extends DMLStatement {
    
    private final Collection<Column> columns = new LinkedList<>();
    
    private final List<Conditions> multipleConditions = new LinkedList<>();
    
    private int columnsListLastPosition;
    
    private int generateKeyColumnIndex = -1;
    
    private int afterValuesPosition;
    
    private int valuesListLastPosition;
    
    private GeneratedKey generatedKey;
    
    /**
     * Append generate key token.
     *
     * @param shardingRule databases and tables sharding rule
     */
    public void appendGenerateKeyToken(final ShardingRule shardingRule) {
        if (null != generatedKey) {
            return;
        }
        Optional<TableRule> tableRule = shardingRule.tryFindTableRuleByLogicTable(getTables().getSingleTableName());
        if (!tableRule.isPresent()) {
            return;
        }
        Optional<GeneratedKeyToken> generatedKeysToken = findGeneratedKeyToken();
        if (!generatedKeysToken.isPresent()) {
            return;
        }
        ItemsToken valuesToken = new ItemsToken(generatedKeysToken.get().getBeginPosition());
        appendGenerateKeyToken(shardingRule, tableRule.get(), valuesToken);
        getSqlTokens().remove(generatedKeysToken.get());
        getSqlTokens().add(valuesToken);
    }
    
    private void appendGenerateKeyToken(final ShardingRule shardingRule, final TableRule tableRule, final ItemsToken valuesToken) {
        if (0 == getParametersIndex()) {
            Number generatedKey = shardingRule.generateKey(tableRule.getLogicTable());
            valuesToken.getItems().add(generatedKey.toString());
            getConditions().add(new Condition(new Column(tableRule.getGenerateKeyColumn(), tableRule.getLogicTable()), new SQLNumberExpression(generatedKey)), shardingRule);
            this.generatedKey = new GeneratedKey(tableRule.getLogicTable(), -1, generatedKey);
        } else {
            valuesToken.getItems().add(Symbol.QUESTION.getLiterals());
            getConditions().add(new Condition(new Column(tableRule.getGenerateKeyColumn(), tableRule.getLogicTable()), new SQLPlaceholderExpression(getParametersIndex())), shardingRule);
            generatedKey = new GeneratedKey(tableRule.getGenerateKeyColumn(), getParametersIndex(), null);
        }
    }
    
    private Optional<GeneratedKeyToken> findGeneratedKeyToken() {
        for (SQLToken each : getSqlTokens()) {
            if (each instanceof GeneratedKeyToken) {
                return Optional.of((GeneratedKeyToken) each);
            }
        }
        return Optional.absent();
    }
}
