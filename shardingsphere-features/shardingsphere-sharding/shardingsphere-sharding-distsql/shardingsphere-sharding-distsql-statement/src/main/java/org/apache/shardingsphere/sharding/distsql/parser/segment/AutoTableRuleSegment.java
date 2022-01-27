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
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;

import java.util.Collection;

/**
 * Auto table rule segment.
 */
@Getter
public final class AutoTableRuleSegment extends AbstractTableRuleSegment {
    
    @Setter
    private String shardingColumn;
    
    @Setter
    private AlgorithmSegment shardingAlgorithmSegment;
    
    public AutoTableRuleSegment(final String logicTable, final Collection<String> dataSources) {
        super(logicTable, dataSources);
    }
    
    public AutoTableRuleSegment(final String logicTable, final Collection<String> dataSources, final String shardingColumn,
                                final AlgorithmSegment shardingAlgorithm, final KeyGenerateStrategySegment keyGenerateStrategySegment) {
        super(logicTable, dataSources, keyGenerateStrategySegment);
        this.shardingColumn = shardingColumn;
        this.shardingAlgorithmSegment = shardingAlgorithm;
    }
    
    /**
     * Determine whether there is a complete sharding algorithm.
     *
     * @return has datasource or not
     */
    public boolean isCompleteShardingAlgorithm() {
        return null != shardingColumn && null != shardingAlgorithmSegment;
    }
}
