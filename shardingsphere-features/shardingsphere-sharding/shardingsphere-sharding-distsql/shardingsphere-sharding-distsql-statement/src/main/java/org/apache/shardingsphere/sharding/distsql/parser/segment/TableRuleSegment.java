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

package org.apache.shardingsphere.sharding.distsql.parser.segment;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

/**
 * Table rule segment.
 */
@Getter
public final class TableRuleSegment extends AbstractTableRuleSegment {
    
    @Setter
    private ShardingStrategySegment tableStrategySegment;
    
    @Setter
    private ShardingStrategySegment databaseStrategySegment;
    
    public TableRuleSegment(final String logicTable, final Collection<String> dataSourcesNote) {
        super(logicTable, dataSourcesNote);
    }
    
    public TableRuleSegment(final String logicTable, final Collection<String> dataSourcesNote, final ShardingStrategySegment databaseStrategy,
                            final ShardingStrategySegment tableStrategy, final KeyGenerateStrategySegment keyGenerateStrategySegment) {
        super(logicTable, dataSourcesNote, keyGenerateStrategySegment);
        this.tableStrategySegment = tableStrategy;
        this.databaseStrategySegment = databaseStrategy;
    }
    
    public TableRuleSegment(final String logicTable, final Collection<String> dataSourcesNote, final KeyGenerateStrategySegment keyGenerateStrategySegment) {
        super(logicTable, dataSourcesNote, keyGenerateStrategySegment);
    }
}
