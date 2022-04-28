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

package org.apache.shardingsphere.readwritesplitting.type.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.infra.aware.DataSourceNameAware;
import org.apache.shardingsphere.infra.aware.DataSourceNameAwareFactory;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.readwritesplitting.type.ReadwriteSplittingDataSourceProcessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Dynamic readwrite splitting data source processor.
 */
@Getter
public final class DynamicReadwriteSplittingDataSourceProcessor implements ReadwriteSplittingDataSourceProcessor {
    
    private final String autoAwareDataSourceName;
    
    public DynamicReadwriteSplittingDataSourceProcessor(final Properties props) {
        autoAwareDataSourceName = props.getProperty("auto-aware-data-source-name");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(autoAwareDataSourceName), "auto aware data source name is required.");
    }
    
    @Override
    public String getWriteDataSource() {
        Optional<DataSourceNameAware> dataSourceNameAware = DataSourceNameAwareFactory.newInstance();
        return dataSourceNameAware.map(optional -> optional.getPrimaryDataSourceName(autoAwareDataSourceName)).orElse(null);
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
    public Map<String, String> getDataSources() {
        Optional<DataSourceNameAware> dataSourceNameAware = DataSourceNameAwareFactory.newInstance();
        Map<String, String> result = new HashMap<>(2, 1);
        if (!Strings.isNullOrEmpty(autoAwareDataSourceName) && dataSourceNameAware.isPresent() && dataSourceNameAware.get().getRule().isPresent()) {
            result.put(ExportableConstants.PRIMARY_DATA_SOURCE_NAME, dataSourceNameAware.get().getPrimaryDataSourceName(autoAwareDataSourceName));
            result.put(ExportableConstants.REPLICA_DATA_SOURCE_NAMES, String.join(",", dataSourceNameAware.get().getReplicaDataSourceNames(autoAwareDataSourceName)));
            return result;
        }
        return result;
    }
    
    @Override
    public Map<String, Collection<String>> getDataSourceMapper(final String name) {
        Map<String, Collection<String>> result = new HashMap<>(1, 1);
        Collection<String> actualDataSourceNames = new LinkedList<>();
        Optional<DataSourceNameAware> dataSourceNameAware = DataSourceNameAwareFactory.newInstance();
        if (dataSourceNameAware.isPresent()) {
            actualDataSourceNames.add(dataSourceNameAware.get().getPrimaryDataSourceName(autoAwareDataSourceName));
            actualDataSourceNames.addAll(dataSourceNameAware.get().getReplicaDataSourceNames(autoAwareDataSourceName));
        }
        result.put(name, actualDataSourceNames);
        return result;
    }
}
