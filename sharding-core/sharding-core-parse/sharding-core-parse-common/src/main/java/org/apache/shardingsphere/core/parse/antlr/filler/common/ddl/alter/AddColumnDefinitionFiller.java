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

package org.apache.shardingsphere.core.parse.antlr.filler.common.ddl.alter;

import lombok.Setter;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.antlr.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.api.ShardingTableMetaDataAwareFiller;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.ddl.AlterTableStatement;

/**
 * Add column definition filler.
 *
 * @author duhongjun
 */
@Setter
public final class AddColumnDefinitionFiller implements SQLSegmentFiller<AddColumnDefinitionSegment>, ShardingTableMetaDataAwareFiller {
    
    private ShardingTableMetaData shardingTableMetaData;
    
    @Override
    public void fill(final AddColumnDefinitionSegment sqlSegment, final SQLStatement sqlStatement) {
        AlterTableStatement alterTableStatement = (AlterTableStatement) sqlStatement;
        if (!alterTableStatement.findColumnDefinitionFromMetaData(sqlSegment.getColumnDefinition().getColumnName(), shardingTableMetaData).isPresent()) {
            alterTableStatement.getAddedColumnDefinitions().add(sqlSegment.getColumnDefinition());
        }
        if (sqlSegment.getColumnPosition().isPresent()) {
            alterTableStatement.getChangedPositionColumns().add(sqlSegment.getColumnPosition().get());
        }
    }
}
