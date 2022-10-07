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

package org.apache.shardingsphere.readwritesplitting.strategy.type;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.readwritesplitting.strategy.ReadwriteSplittingStrategy;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Static readwrite splitting strategy.
 */
@RequiredArgsConstructor
public final class StaticReadwriteSplittingStrategy implements ReadwriteSplittingStrategy {
    
    private final String writeDataSourceName;
    
    private final List<String> readDataSourceNames;
    
    @Override
    public String getWriteDataSource() {
        return writeDataSourceName;
    }
    
    @Override
    public List<String> getReadDataSources() {
        return readDataSourceNames;
    }
    
    @Override
    public Collection<String> getAllDataSources() {
        Collection<String> result = new LinkedList<>();
        result.add(writeDataSourceName);
        result.addAll(readDataSourceNames);
        return result;
    }
}
