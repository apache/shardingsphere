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

package org.apache.shardingsphere.core.parse.antlr.filler.sharding.dml;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.antlr.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.api.ShardingRuleAwareFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.api.ShardingTableMetaDataAwareFiller;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.WhereSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Where filler.
 *
 * @author duhongjun
 */
@Getter
@Setter
public class WhereFiller implements SQLSegmentFiller<WhereSegment>, ShardingRuleAwareFiller, ShardingTableMetaDataAwareFiller {
    
    private ShardingRule shardingRule;
    
    private ShardingTableMetaData shardingTableMetaData;
    
    @Override
    public void fill(final WhereSegment sqlSegment, final SQLStatement sqlStatement) {
        new OrConditionFiller(shardingRule, shardingTableMetaData).fill(sqlSegment.getConditions(), sqlStatement);
        sqlStatement.setParametersIndex(sqlSegment.getParameterCount());
        if (sqlStatement instanceof DeleteStatement) {
            DeleteStatement deleteStatement = (DeleteStatement) sqlStatement;
            deleteStatement.setWhereStartIndex(sqlSegment.getWhereStartIndex());
            deleteStatement.setWhereStopIndex(sqlSegment.getWhereStopIndex());
            deleteStatement.setWhereParameterStartIndex(sqlSegment.getWhereParameterStartIndex());
            deleteStatement.setWhereParameterEndIndex(sqlSegment.getWhereParameterEndIndex());
        }
    }
}
