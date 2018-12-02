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

import com.google.common.base.Optional;

import io.shardingsphere.core.constant.OrderDirection;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.filler.SQLSegmentFiller;
import io.shardingsphere.core.parsing.antlr.sql.segment.GroupBySegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.OrderBySegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.parser.context.OrderItem;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.parsing.parser.token.TableToken;
import io.shardingsphere.core.rule.ShardingRule;

/**
 * Group by segment filler.
 *
 * @author duhongjun
 */
public class GroupBySegmentFiller implements SQLSegmentFiller {
    
    @Override
    public void fill(final SQLSegment sqlSegment, final SQLStatement sqlStatement, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        GroupBySegment groupBySegment = (GroupBySegment) sqlSegment;
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        selectStatement.setGroupByLastPosition(groupBySegment.getGroupByLastPosition());
        for(OrderBySegment each : groupBySegment.getGroupByItems()) {
            if (-1 < each.getIndex()) {
                selectStatement.getGroupByItems().add(new OrderItem(each.getIndex(), OrderDirection.ASC, OrderDirection.ASC));
            } else if (each.getName().isPresent()) {
                String name = each.getName().get();
                if (each.getOwner().isPresent()) {
                    String owner = each.getOwner().get();
                    if (sqlStatement.getTables().getTableNames().contains(owner)) {
                        sqlStatement.addSQLToken(new TableToken(each.getStartPosition(), 0, owner));
                    }
                    selectStatement.getGroupByItems().add(new OrderItem(owner, name, OrderDirection.ASC, OrderDirection.ASC, selectStatement.getAlias(owner+"."+name)));
                }else {
                    selectStatement.getGroupByItems().add(new OrderItem(name, OrderDirection.ASC, OrderDirection.ASC, selectStatement.getAlias(name)));
                }
            }
        }
    }
}
