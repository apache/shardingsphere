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

package org.apache.shardingsphere.core.parse.filler.sharding.dml.select;

import lombok.Setter;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.filler.api.ShardingRuleAwareFiller;
import org.apache.shardingsphere.core.parse.filler.api.ShardingTableMetaDataAwareFiller;
import org.apache.shardingsphere.core.parse.filler.sharding.dml.ShardingOrPredicateFiller;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.SubqueryPredicateSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Subquery predicate filler for sharding.
 *
 * @author duhongjun
 */
@Setter
public final class ShardingSubqueryPredicateFiller implements SQLSegmentFiller<SubqueryPredicateSegment>, ShardingRuleAwareFiller, ShardingTableMetaDataAwareFiller {
    
    private final ShardingRowNumberPredicateFiller shardingRowNumberPredicateFiller = new ShardingRowNumberPredicateFiller();
    
    private ShardingRule shardingRule;
    
    private ShardingTableMetaData shardingTableMetaData;
    
    @Override
    public void fill(final SubqueryPredicateSegment sqlSegment, final SQLStatement sqlStatement) {
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        ShardingOrPredicateFiller shardingOrPredicateFiller = getShardingOrPredicateFiller();
        for (OrPredicateSegment each : sqlSegment.getOrPredicates()) {
            selectStatement.getSubqueryParseConditions().add(shardingOrPredicateFiller.buildConditions(each, sqlStatement));
            shardingRowNumberPredicateFiller.fill(each, sqlStatement);
        }
    }
    
    private ShardingOrPredicateFiller getShardingOrPredicateFiller() {
        ShardingOrPredicateFiller result = new ShardingOrPredicateFiller();
        result.setShardingRule(shardingRule);
        result.setShardingTableMetaData(shardingTableMetaData);
        return result;
    }
}
