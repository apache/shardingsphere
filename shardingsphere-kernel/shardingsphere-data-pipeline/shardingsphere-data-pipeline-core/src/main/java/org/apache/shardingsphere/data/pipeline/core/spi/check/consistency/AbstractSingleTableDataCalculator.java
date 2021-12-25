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

package org.apache.shardingsphere.data.pipeline.core.spi.check.consistency;

import org.apache.shardingsphere.data.pipeline.core.datasource.DataSourceFactory;
import org.apache.shardingsphere.data.pipeline.core.datasource.DataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.SingleTableDataCalculator;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.JDBCDataSourceConfiguration;

import java.util.Properties;

/**
 * Abstract single table data calculator.
 */
public abstract class AbstractSingleTableDataCalculator implements SingleTableDataCalculator {
    
    private final DataSourceFactory dataSourceFactory = new DataSourceFactory();
    
    private Properties algorithmProps;
    
    protected final DataSourceWrapper getDataSource(final JDBCDataSourceConfiguration dataSourceConfig) {
        return dataSourceFactory.newInstance(dataSourceConfig);
    }
    
    @Override
    public Properties getAlgorithmProps() {
        return algorithmProps;
    }
    
    @Override
    public void setAlgorithmProps(final Properties algorithmProps) {
        this.algorithmProps = algorithmProps;
    }
    
    @Override
    public void init() {
    }
}
