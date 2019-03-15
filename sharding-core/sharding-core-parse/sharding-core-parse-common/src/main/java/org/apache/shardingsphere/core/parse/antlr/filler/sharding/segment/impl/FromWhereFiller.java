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

package org.apache.shardingsphere.core.parse.antlr.filler.sharding.segment.impl;

import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.antlr.filler.sharding.SQLSegmentShardingFiller;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.FromWhereSegment;
import org.apache.shardingsphere.core.parse.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * From where filler.
 *
 * @author duhongjun
 */
public class FromWhereFiller implements SQLSegmentShardingFiller<FromWhereSegment> {
    
    @Override
    public void fill(final FromWhereSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        new OrConditionFiller().fill(sqlSegment.getConditions(), sqlStatement, sql, shardingRule, shardingTableMetaData);
        int count = 0;
        while (count < sqlSegment.getParameterCount()) {
            sqlStatement.increaseParametersIndex();
            count++;
        }
    }
}
