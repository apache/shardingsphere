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

package io.shardingsphere.core.parsing.antlr.extractor.segment.filler;

import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.TableSegment;
import io.shardingsphere.core.parsing.parser.context.table.Table;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;

/**
 * Table handler result filler.
 *
 * @author duhongjun
 */
public class TableHandlerResultFiller extends AbstractHandlerResultFiller {
    
    public TableHandlerResultFiller(final Class<? extends SQLSegment> extractResultClass) {
        super(TableSegment.class);
    }
    
    public TableHandlerResultFiller() {
        super(TableSegment.class);
    }
    
    @Override
    protected void fillSQLStatement(final Object extractResult, final SQLStatement statement, final ShardingTableMetaData shardingTableMetaData) {
        TableSegment tableResult = (TableSegment) extractResult;
        statement.getTables().add(new Table(tableResult.getName(), tableResult.getAlias()));
        statement.getSQLTokens().add(tableResult.getToken());
    }
}
