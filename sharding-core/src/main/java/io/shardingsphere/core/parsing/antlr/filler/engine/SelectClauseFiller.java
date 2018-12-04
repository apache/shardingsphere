/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.core.parsing.antlr.filler.engine;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.AggregationType;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.filler.SQLSegmentFiller;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.SelectClauseSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.CommonExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.ExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.FunctionExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.PropertyExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.StarExpressionSegment;
import io.shardingsphere.core.parsing.parser.context.selectitem.AggregationSelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.CommonSelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.StarSelectItem;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.parsing.parser.token.TableToken;
import io.shardingsphere.core.rule.ShardingRule;

/**
 * Select clause filler.
 *
 * @author duhongjun
 */
public class SelectClauseFiller implements SQLSegmentFiller {
    
    @Override
    public void fill(final SQLSegment sqlSegment, final SQLStatement sqlStatement, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        SelectClauseSegment selectClauseSegment = (SelectClauseSegment) sqlSegment;
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        selectStatement.setSelectListLastPosition(selectClauseSegment.getSelectListLastPosition());
        for (ExpressionSegment each : selectClauseSegment.getExpressions()) {
            if (each instanceof StarExpressionSegment) {
                if (!selectStatement.isContainStar()) {
                    selectStatement.setContainStar(true);
                }
                StarExpressionSegment starSegment = (StarExpressionSegment) each;
                selectStatement.getItems().add(new StarSelectItem(starSegment.getOwner()));
                Optional<String> owner = starSegment.getOwner();
                if (owner.isPresent() && selectStatement.getTables().getTableNames().contains(owner.get())) {
                    selectStatement.addSQLToken(new TableToken(starSegment.getStartPosition(), 0, owner.get()));
                }
                continue;
            }
            if (each instanceof PropertyExpressionSegment) {
                PropertyExpressionSegment propertySegment = (PropertyExpressionSegment) each;
                Optional<String> owner = propertySegment.getOwner();
                if (owner.isPresent() && selectStatement.getTables().getTableNames().contains(owner.get())) {
                    selectStatement.addSQLToken(new TableToken(propertySegment.getStartPosition(), 0, owner.get()));
                }
                selectStatement.getItems().add(new CommonSelectItem(selectStatement.getSql().substring(propertySegment.getStartPosition(), propertySegment.getEndPosition() + 1),
                        propertySegment.getAlias()));
                continue;
            }
            if (each instanceof CommonExpressionSegment) {
                CommonExpressionSegment commonSegment = (CommonExpressionSegment) each;
                selectStatement.getItems().add(new CommonSelectItem(commonSegment.getExpression(), commonSegment.getAlias()));
                continue;
            }
            if (each instanceof FunctionExpressionSegment) {
                FunctionExpressionSegment functionSegment = (FunctionExpressionSegment) each;
                AggregationType aggregationType = null;
                for (AggregationType eachType : AggregationType.values()) {
                    if (eachType.name().equalsIgnoreCase(functionSegment.getName())) {
                        aggregationType = eachType;
                        break;
                    }
                }
                String innerExpression = selectStatement.getSql().substring(functionSegment.getInnerExpressionStartIndex(), functionSegment.getInnerExpressionEndIndex() + 1);
                if (null != aggregationType) {
                    selectStatement.getItems().add(new AggregationSelectItem(aggregationType, innerExpression, functionSegment.getAlias()));
                } else {
                    selectStatement.getItems().add(new CommonSelectItem(functionSegment.getName() + innerExpression, functionSegment.getAlias()));
                }
            }
        }
    }
}
