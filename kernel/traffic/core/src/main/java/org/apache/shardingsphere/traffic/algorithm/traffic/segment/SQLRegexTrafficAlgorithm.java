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

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.traffic.api.traffic.segment.SegmentTrafficAlgorithm;
import org.apache.shardingsphere.traffic.api.traffic.segment.SegmentTrafficValue;
import org.apache.shardingsphere.traffic.exception.segment.SegmentTrafficAlgorithmInitializationException;

import java.util.Properties;
import java.util.regex.Pattern;

/**
 * SQL regex traffic algorithm.
 */
public final class SQLRegexTrafficAlgorithm implements SegmentTrafficAlgorithm {
    
    private static final String REGEX_PROPS_KEY = "regex";
    
    private Pattern regex;
    
    @Override
    public void init(final Properties props) {
        ShardingSpherePreconditions.checkState(props.containsKey(REGEX_PROPS_KEY),
                () -> new SegmentTrafficAlgorithmInitializationException(SQLRegexTrafficAlgorithm.class.getName(), String.format("%s cannot be null", REGEX_PROPS_KEY)));
        regex = Pattern.compile(props.getProperty(REGEX_PROPS_KEY));
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(String.valueOf(regex)),
                () -> new SegmentTrafficAlgorithmInitializationException(SQLRegexTrafficAlgorithm.class.getName(), "regex must be not empty"));
    }
    
    @Override
    public boolean match(final SegmentTrafficValue segmentTrafficValue) {
        return regex.matcher(segmentTrafficValue.getSql()).matches();
    }
    
    @Override
    public String getType() {
        return "SQL_REGEX";
    }
}
