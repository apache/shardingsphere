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

package org.apache.shardingsphere.readwritesplitting.route.standard.filter;

import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceGroupRule;

import java.util.List;

/**
 * Read data sources filter.
 */
public interface ReadDataSourcesFilter {
    
    /**
     * Filter replica data sources.
     *
     * @param rule readwrite-splitting data source rule
     * @param toBeFilteredReadDataSources to be filtered read data sources
     * @return filtered read data sources
     */
    List<String> filter(ReadwriteSplittingDataSourceGroupRule rule, List<String> toBeFilteredReadDataSources);
}
