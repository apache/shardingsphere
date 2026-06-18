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

package org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.AbstractExpectedSQLSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.column.ExpectedColumn;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.index.ExpectedPartition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.partition.ExpectedPartitionValues;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.partition.ExpectedBuckets;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.ExpectedProperties;

import javax.xml.bind.annotation.XmlElement;

/**
 * Expected add partition definition.
 */
@Getter
@Setter
public final class ExpectedAddPartitionDefinition extends AbstractExpectedSQLSegment {
    
    @XmlElement(name = "partition")
    private ExpectedPartition partition;
    
    @XmlElement(name = "partition-values")
    private ExpectedPartitionValues partitionValues;
    
    @XmlElement(name = "properties")
    private ExpectedProperties properties;
    
    @XmlElement(name = "distributed-column")
    private ExpectedColumn distributedColumn;
    
    @XmlElement(name = "buckets")
    private ExpectedBuckets buckets;
}
