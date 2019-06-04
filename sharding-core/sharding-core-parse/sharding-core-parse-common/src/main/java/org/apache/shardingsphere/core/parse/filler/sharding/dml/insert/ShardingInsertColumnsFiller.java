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

package org.apache.shardingsphere.core.parse.filler.sharding.dml.insert;

import lombok.Setter;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.filler.api.ShardingTableMetaDataAwareFiller;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;

/**
 * Insert columns filler.
 *
 * @author zhangliang
 * @author panjuan
 */
@Setter
public final class ShardingInsertColumnsFiller implements SQLSegmentFiller<InsertColumnsSegment>, ShardingTableMetaDataAwareFiller {
    
    private ShardingTableMetaData shardingTableMetaData;
    
    @Override
    public void fill(final InsertColumnsSegment sqlSegment, final SQLStatement sqlStatement) {
        if (sqlStatement instanceof InsertStatement) {
            InsertStatement insertStatement = (InsertStatement) sqlStatement;
            if (sqlSegment.getColumns().isEmpty()) {
                fillFromMetaData(insertStatement);
            } else {
                fillFromSQL(sqlSegment, insertStatement);
            }
        }
    }
    
    private void fillFromMetaData(final InsertStatement insertStatement) {
        String tableName = insertStatement.getTables().getSingleTableName();
        for (String each : shardingTableMetaData.getAllColumnNames(tableName)) {
            insertStatement.getColumnNames().add(each);
        }
    }
    
    private void fillFromSQL(final InsertColumnsSegment sqlSegment, final InsertStatement insertStatement) {
        for (ColumnSegment each : sqlSegment.getColumns()) {
            insertStatement.getColumnNames().add(each.getName());
        }
    }
}
