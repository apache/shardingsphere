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

package org.apache.shardingsphere.readwritesplitting.distsql.parser.segment;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;

import java.util.Collection;
import java.util.Properties;

/**
 * Readwrite-splitting rule segment.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class ReadwriteSplittingRuleSegment implements ASTNode {
    
    private final String name;
    
    private final String autoAwareResource;
    
    private final String writeDataSourceQueryEnabled;
    
    private final String writeDataSource;
    
    private final Collection<String> readDataSources;
    
    private final String loadBalancer;
    
    private final Properties props;
    
    public ReadwriteSplittingRuleSegment(final String name, final String autoAwareResource, final String writeDataSourceQueryEnabled, final String loadBalancer, final Properties props) {
        this(name, autoAwareResource, writeDataSourceQueryEnabled, null, null, loadBalancer, props);
    }
    
    public ReadwriteSplittingRuleSegment(final String name, final String writeDataSource, final Collection<String> readDataSources, final String loadBalancer, final Properties props) {
        this(name, null, null, writeDataSource, readDataSources, loadBalancer, props);
    }
    
    /**
     * Is it an auto aware type.
     *
     * @return is auto ware or not
     */
    public boolean isAutoAware() {
        return !Strings.isNullOrEmpty(autoAwareResource);
    }
}
