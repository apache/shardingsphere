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

package org.apache.shardingsphere.sql.parser.statement.core.segment.dml.sample;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.statement.core.enums.ScanUnit;
import org.apache.shardingsphere.sql.parser.statement.core.segment.SQLSegment;

import java.util.List;

/**
 * Update statistics sample option segment.
 */
@NoArgsConstructor
@Getter
@Setter
public final class SampleOptionSegment implements SQLSegment {
    
    private int startIndex;
    
    private int stopIndex;
    
    private boolean persistSamplePercent;
    
    private String sampleNumber;
    
    private SampleStrategy strategy;
    
    private ScanUnit scanUnit;
    
    private List<String> partitions;
    
    public SampleOptionSegment(final int startIndex, final int stopIndex) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
    }
}
