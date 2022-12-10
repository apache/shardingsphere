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

package org.apache.shardingsphere.sharding.distsql.parser.segment.table;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.strategy.AuditStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.strategy.KeyGenerateStrategySegment;

import java.util.Collection;

/**
 * Auto table rule segment.
 */
@Getter
@Setter
public final class AutoTableRuleSegment extends AbstractTableRuleSegment {
    
    private String shardingColumn;
    
    private AlgorithmSegment shardingAlgorithmSegment;
    
    public AutoTableRuleSegment(final String logicTable, final Collection<String> dataSources) {
        super(logicTable, dataSources);
    }
    
    public AutoTableRuleSegment(final String logicTable, final Collection<String> dataSources, final String shardingColumn,
                                final AlgorithmSegment shardingAlgorithm, final KeyGenerateStrategySegment keyGenerateStrategySegment,
                                final AuditStrategySegment auditStrategySegment) {
        super(logicTable, dataSources, keyGenerateStrategySegment, auditStrategySegment);
        this.shardingColumn = shardingColumn;
        this.shardingAlgorithmSegment = shardingAlgorithm;
    }
    
    /**
     * Determine whether sharding algorithm completed.
     *
     * @return completed sharding algorithm or not
     */
    public boolean isShardingAlgorithmCompleted() {
        return null != shardingColumn && null != shardingAlgorithmSegment;
    }
}
