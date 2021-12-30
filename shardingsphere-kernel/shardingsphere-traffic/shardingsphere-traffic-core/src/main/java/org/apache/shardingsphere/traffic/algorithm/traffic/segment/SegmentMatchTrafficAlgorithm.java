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

package org.apache.shardingsphere.traffic.algorithm.traffic.segment;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.traffic.api.traffic.enums.TrafficSegmentType;
import org.apache.shardingsphere.traffic.api.traffic.enums.TrafficStatementType;
import org.apache.shardingsphere.traffic.api.traffic.segment.SegmentTrafficAlgorithm;
import org.apache.shardingsphere.traffic.api.traffic.segment.SegmentTrafficValue;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Segment match traffic algorithm.
 */
@Getter
@Setter
public final class SegmentMatchTrafficAlgorithm implements SegmentTrafficAlgorithm {
    
    private static final String STATEMENT_PROPS_KEY = "statement";
    
    private static final String SEGMENTS_PROPS_KEY = "segments";
    
    private Properties props = new Properties();
    
    private String statement;
    
    private Collection<String> segments;
    
    @Override
    public void init() {
        Preconditions.checkArgument(props.containsKey(STATEMENT_PROPS_KEY), "%s cannot be null.", STATEMENT_PROPS_KEY);
        statement = props.getProperty(STATEMENT_PROPS_KEY);
        Preconditions.checkArgument(props.containsKey(SEGMENTS_PROPS_KEY), "%s cannot be null.", SEGMENTS_PROPS_KEY);
        segments = Splitter.on(",").trimResults().splitToList(props.getProperty(SEGMENTS_PROPS_KEY));
    }
    
    @Override
    public boolean match(final SegmentTrafficValue segmentTrafficValue) {
        TrafficStatementType statementType = TrafficStatementType.getStatementTypeByClazz(segmentTrafficValue.getStatement().getClass());
        Set<String> segmentNames = getSegmentNames(segmentTrafficValue);
        return statement.equalsIgnoreCase(statementType.name()) && segmentNames.containsAll(segments);
    }
    
    private Set<String> getSegmentNames(final SegmentTrafficValue segmentTrafficValue) {
        return segmentTrafficValue.getSegments().stream().map(each -> TrafficSegmentType.getSegmentTypeByClazz(each.getClass()).name())
                .collect(Collectors.toCollection(() -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER)));
    }
    
    @Override
    public String getType() {
        return "SEGMENT_MATCH";
    }
}
