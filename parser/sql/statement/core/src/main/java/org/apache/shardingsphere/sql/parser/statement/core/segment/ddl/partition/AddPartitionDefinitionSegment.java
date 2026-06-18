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

package org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.partition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.PartitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.AlterDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertiesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;

import java.util.Optional;

/**
 * Add partition definition segment.
 */
@RequiredArgsConstructor
@Getter
public final class AddPartitionDefinitionSegment implements AlterDefinitionSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final PartitionSegment partition;
    
    private PartitionValuesSegment partitionValues;
    
    private PropertiesSegment properties;
    
    private ColumnSegment distributedColumn;
    
    private Integer buckets;
    
    /**
     * Set partition values.
     *
     * @param partitionValues partition values
     */
    public void setPartitionValues(final PartitionValuesSegment partitionValues) {
        this.partitionValues = partitionValues;
    }
    
    /**
     * Get partition values.
     *
     * @return partition values
     */
    public Optional<PartitionValuesSegment> getPartitionValues() {
        return Optional.ofNullable(partitionValues);
    }
    
    /**
     * Set properties.
     *
     * @param properties properties
     */
    public void setProperties(final PropertiesSegment properties) {
        this.properties = properties;
    }
    
    /**
     * Get properties.
     *
     * @return properties
     */
    public Optional<PropertiesSegment> getProperties() {
        return Optional.ofNullable(properties);
    }
    
    /**
     * Set distributed column.
     *
     * @param distributedColumn distributed column
     */
    public void setDistributedColumn(final ColumnSegment distributedColumn) {
        this.distributedColumn = distributedColumn;
    }
    
    /**
     * Get distributed column.
     *
     * @return distributed column
     */
    public Optional<ColumnSegment> getDistributedColumn() {
        return Optional.ofNullable(distributedColumn);
    }
    
    /**
     * Set buckets.
     *
     * @param buckets buckets
     */
    public void setBuckets(final Integer buckets) {
        this.buckets = buckets;
    }
    
    /**
     * Get buckets.
     *
     * @return buckets
     */
    public Optional<Integer> getBuckets() {
        return Optional.ofNullable(buckets);
    }
}
