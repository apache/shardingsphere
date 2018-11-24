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
import io.shardingsphere.core.parsing.antlr.sql.segment.TableJoinSegment;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;

/**
 * Join table extract result filler.
 * 
 * @author duhongjun
 */
public final class TableJoinHandlerResultFiller extends TableHandlerResultFiller {
    
    public TableJoinHandlerResultFiller() {
        super(TableJoinSegment.class);
    }
    
    @Override
    protected void fillSQLStatement(final Object extractResult, final SQLStatement statement, final ShardingTableMetaData shardingTableMetaData) {
        TableJoinSegment tableResult = (TableJoinSegment) extractResult;
        super.fillSQLStatement(tableResult, statement, shardingTableMetaData);
    }
}
