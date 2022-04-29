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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.aware.DataSourceNameAware;
import org.apache.shardingsphere.infra.aware.DataSourceNameAwareFactory;
import org.apache.shardingsphere.readwritesplitting.strategy.ReadwriteSplittingStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Dynamic readwrite splitting strategy.
 */
@RequiredArgsConstructor
@Getter
public final class DynamicReadwriteSplittingStrategy implements ReadwriteSplittingStrategy {
    
    private final String autoAwareDataSourceName;
    
    @Override
    public String getWriteDataSource() {
        return DataSourceNameAwareFactory.newInstance().map(optional -> optional.getPrimaryDataSourceName(autoAwareDataSourceName)).orElse(null);
    }
    
    @Override
    public List<String> getReadDataSources() {
        Optional<DataSourceNameAware> dataSourceNameAware = DataSourceNameAwareFactory.newInstance();
        if (dataSourceNameAware.isPresent() && dataSourceNameAware.get().getRule().isPresent()) {
            return new ArrayList<>(dataSourceNameAware.get().getReplicaDataSourceNames(autoAwareDataSourceName));
        }
        return Collections.emptyList();
    }
    
    @Override
    public Collection<String> getAllDataSources() {
        Collection<String> result = new LinkedList<>();
        Optional<DataSourceNameAware> dataSourceNameAware = DataSourceNameAwareFactory.newInstance();
        if (dataSourceNameAware.isPresent()) {
            result.add(dataSourceNameAware.get().getPrimaryDataSourceName(autoAwareDataSourceName));
            result.addAll(dataSourceNameAware.get().getReplicaDataSourceNames(autoAwareDataSourceName));
        }
        return result;
    }
}
