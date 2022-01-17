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

package org.apache.shardingsphere.readwritesplitting.algorithm;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.readwritesplitting.spi.ReadwriteSplittingType;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

/**
 * Static readwrite splitting algorithm.
 */
public class StaticReadwriteSplittingType implements ReadwriteSplittingType {
    
    @Getter
    @Setter
    private Properties props = new Properties();
    
    private String writeDataSourceName;
    
    private String readDataSourceNames;
    
    @Override
    public void init() {
        writeDataSourceName = props.getProperty("write-data-source-name");
        readDataSourceNames = props.getProperty("read-data-source-names");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(writeDataSourceName), "Write data source name is required.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(readDataSourceNames), "Read data source names are required.");
    }
    
    @Override
    public String getWriteDataSource() {
        return writeDataSourceName;
    }
    
    @Override
    public List<String> getReadDataSources() {
        return Splitter.on(",").trimResults().splitToList(readDataSourceNames);
    }
    
    @Override
    public Map<String, String> getDataSources() {
        Map<String, String> result = new HashMap<>(2, 1);
        result.put(ExportableConstants.PRIMARY_DATA_SOURCE_NAME, writeDataSourceName);
        result.put(ExportableConstants.REPLICA_DATA_SOURCE_NAMES, readDataSourceNames);
        return result;
    }
    
    @Override
    public Map<String, Collection<String>> getDataSourceMapper(final String name) {
        Map<String, Collection<String>> result = new HashMap<>(1, 1);
        Collection<String> actualDataSourceNames = new LinkedList<>();
        actualDataSourceNames.add(writeDataSourceName);
        actualDataSourceNames.addAll(Splitter.on(",").trimResults().splitToList(readDataSourceNames));
        result.put(name, actualDataSourceNames);
        return result;
    }
    
    @Override
    public String getType() {
        return "STATIC";
    }
}
