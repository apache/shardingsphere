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

package io.shardingsphere.core.parsing.antlr.filler.engnie;

import io.shardingsphere.core.constant.AggregationType;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.filler.SQLSegmentFiller;
import io.shardingsphere.core.parsing.antlr.sql.segment.CommonExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.FunctionExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.SelectExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.StarExpressionSegment;
import io.shardingsphere.core.parsing.parser.context.selectitem.AggregationSelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.CommonSelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.StarSelectItem;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.rule.ShardingRule;

/**
 * Select expression segment filler.
 *
 * @author duhongjun
 */
public class SelectExpressionSegmentFiller implements SQLSegmentFiller {
    
    @Override
    public void fill(final SQLSegment sqlSegment, final SQLStatement sqlStatement, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        SelectExpressionSegment selectExpressionSegment = (SelectExpressionSegment) sqlSegment;
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        if (selectExpressionSegment instanceof StarExpressionSegment) {
            selectStatement.getItems().add(new StarSelectItem(((StarExpressionSegment) selectExpressionSegment).getOwner()));
        } else if (selectExpressionSegment instanceof CommonExpressionSegment) {
            CommonExpressionSegment commonSegment = (CommonExpressionSegment) sqlSegment;
            selectStatement.getItems().add(new CommonSelectItem(commonSegment.getExpression(), commonSegment.getAlias()));
        } else if (selectExpressionSegment instanceof FunctionExpressionSegment) {
            FunctionExpressionSegment functionSegment = (FunctionExpressionSegment) sqlSegment;
            AggregationType aggregationType = null;
            for (AggregationType each : AggregationType.values()) {
                if (each.name().equalsIgnoreCase(functionSegment.getName())) {
                    aggregationType = each;
                    break;
                }
            }
            if (null != aggregationType) {
                selectStatement.getItems().add(new AggregationSelectItem(aggregationType, functionSegment.getInnerExpression(), functionSegment.getAlias()));
            }
        }
    }
}
