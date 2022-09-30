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

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import lombok.Getter;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;
import org.apache.shardingsphere.traffic.api.traffic.segment.SegmentTrafficAlgorithm;
import org.apache.shardingsphere.traffic.api.traffic.segment.SegmentTrafficValue;

import java.util.Collection;
import java.util.Properties;
import java.util.TreeSet;

/**
 * SQL match traffic algorithm.
 */
public final class SQLMatchTrafficAlgorithm implements SegmentTrafficAlgorithm {
    
    private static final String SQL_PROPS_KEY = "sql";
    
    private static final String EXCLUDED_CHARACTERS = "[]`'\" ";
    
    @Getter
    private Properties props;
    
    private Collection<String> sql;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        Preconditions.checkArgument(props.containsKey(SQL_PROPS_KEY), "%s can not be null", SQL_PROPS_KEY);
        sql = getExactlySQL(props.getProperty(SQL_PROPS_KEY));
    }
    
    private Collection<String> getExactlySQL(final String value) {
        Collection<String> values = Splitter.on(";").trimResults().omitEmptyStrings().splitToList(value);
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (String each : values) {
            result.add(CharMatcher.anyOf(EXCLUDED_CHARACTERS).removeFrom(each));
        }
        return result;
    }
    
    @Override
    public boolean match(final SegmentTrafficValue segmentTrafficValue) {
        return sql.contains(SQLUtil.trimSemicolon(CharMatcher.anyOf(EXCLUDED_CHARACTERS).removeFrom(segmentTrafficValue.getSql())));
    }
    
    @Override
    public String getType() {
        return "SQL_MATCH";
    }
}
