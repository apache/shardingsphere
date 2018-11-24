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

package io.shardingsphere.core.parsing.antlr.extractor.sql.segment.filler;

import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.result.CommonSelectExpressionExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.result.SelectExpressionExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.result.StarSelectExpressionExtractResult;
import io.shardingsphere.core.parsing.parser.context.selectitem.CommonSelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.StarSelectItem;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.rule.ShardingRule;

/**
 * Select expression handler result filler.
 * 
 * @author duhongjun
 */
public class SelectExpressionHandlerResultFiller extends AbstractHandlerResultFiller {
    
    public SelectExpressionHandlerResultFiller() {
        super(SelectExpressionExtractResult.class);
    }
    
    @Override
    protected void fillSQLStatement(final Object extractResult, final SQLStatement statement, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        SelectExpressionExtractResult selectExprResult = (SelectExpressionExtractResult) extractResult;
        SelectStatement selectStatement = (SelectStatement)statement;
        if(selectExprResult instanceof StarSelectExpressionExtractResult) {
            selectStatement.getItems().add(new StarSelectItem(((StarSelectExpressionExtractResult)selectExprResult).getOwner()));
        }else if(selectExprResult instanceof CommonSelectExpressionExtractResult) {
            CommonSelectExpressionExtractResult commonResult = (CommonSelectExpressionExtractResult)extractResult;
            selectStatement.getItems().add(new CommonSelectItem(commonResult.getExpression(), commonResult.getAlias()));
        }
    }
}
