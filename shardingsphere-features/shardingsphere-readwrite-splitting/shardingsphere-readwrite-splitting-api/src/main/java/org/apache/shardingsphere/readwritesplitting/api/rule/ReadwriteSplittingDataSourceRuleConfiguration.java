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

package org.apache.shardingsphere.readwritesplitting.api.rule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.Properties;

/**
 * Readwrite-splitting data source rule configuration.
 */
@RequiredArgsConstructor
@Getter
public final class ReadwriteSplittingDataSourceRuleConfiguration {
    
    private final String name;
    
    private final String type;
    
    private final Properties props;
    
    private final String loadBalancerName;
    
    /**
     * Get auto aware data source name.
     *
     * @return auto aware data source name
     */
    public Optional<String> getAutoAwareDataSourceName() {
        return Optional.ofNullable(props.getProperty("auto-aware-data-source-name"));
    }
    
    /**
     * Get write data source name.
     *
     * @return write data source name
     */
    public Optional<String> getWriteDataSourceName() {
        return Optional.ofNullable(props.getProperty("write-data-source-name"));
    }
    
    /**
     * Get read data source names.
     *
     * @return read data source names
     */
    public Optional<String> getReadDataSourceNames() {
        return Optional.ofNullable(props.getProperty("read-data-source-names"));
    }
}
