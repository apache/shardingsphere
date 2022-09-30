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
import lombok.Getter;
import org.apache.shardingsphere.traffic.api.traffic.segment.SegmentTrafficAlgorithm;
import org.apache.shardingsphere.traffic.api.traffic.segment.SegmentTrafficValue;

import java.util.Properties;
import java.util.regex.Pattern;

/**
 * SQL regex traffic algorithm.
 */
public final class SQLRegexTrafficAlgorithm implements SegmentTrafficAlgorithm {
    
    private static final String REGEX_PROPS_KEY = "regex";
    
    @Getter
    private Properties props;
    
    private Pattern regex;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        Preconditions.checkArgument(props.containsKey(REGEX_PROPS_KEY), "%s can not be null", REGEX_PROPS_KEY);
        regex = Pattern.compile(props.getProperty(REGEX_PROPS_KEY));
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
