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

package org.apache.shardingsphere.traffic.algorithm.match.segment;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.traffic.api.traffic.segment.SegmentTrafficAlgorithm;
import org.apache.shardingsphere.traffic.api.traffic.segment.SegmentTrafficValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Segment match traffic algorithm.
 */
@Getter
@Setter
public class SegmentMatchTrafficAlgorithm implements SegmentTrafficAlgorithm {
    
    private static final String STATEMENT_PROPS_KEY = "statement";
    
    private static final String SEGMENTS_PROPS_KEY = "segments";
    
    @Override
    public final void init() {
    }
    
    @Override
    public final String getType() {
        return "SEGMENT_MATCH";
    }
    
    @Override
    public final boolean match(final SegmentTrafficValue segmentTrafficValue) {
        String statement = getProps().getProperty(STATEMENT_PROPS_KEY);
        Collection<String> segments = Arrays.asList(getProps().getProperty(SEGMENTS_PROPS_KEY).split(","));
        Set<String> collect = segmentTrafficValue.getSegments().stream().map(each -> each.getClass().getSimpleName()).collect(Collectors.toSet());
        return statement.equals(segmentTrafficValue.getStatement().getClass().getSimpleName()) && collect.containsAll(segments);
    }
}
