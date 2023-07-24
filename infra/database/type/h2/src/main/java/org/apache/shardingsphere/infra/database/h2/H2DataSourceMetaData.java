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

package org.apache.shardingsphere.infra.database.h2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.core.datasource.DataSourceMetaData;

import java.util.Properties;

/**
 * Data source meta data for H2.
 */
@RequiredArgsConstructor
@Getter
public final class H2DataSourceMetaData implements DataSourceMetaData {
    
    private static final String MODEL_MEM = "mem";
    
    private static final String MODEL_PWD = "~";
    
    private static final String MODEL_FILE = "file:";
    
    private final String hostname;
    
    private final String model;
    
    private final int port;
    
    private final String catalog;
    
    @Override
    public String getSchema() {
        return null;
    }
    
    @Override
    public Properties getQueryProperties() {
        return new Properties();
    }
    
    @Override
    public Properties getDefaultQueryProperties() {
        return new Properties();
    }
    
    @Override
    public boolean isInSameDatabaseInstance(final DataSourceMetaData dataSourceMetaData) {
        if (!(dataSourceMetaData instanceof H2DataSourceMetaData)) {
            return false;
        }
        if (!isSameModel(getModel(), ((H2DataSourceMetaData) dataSourceMetaData).getModel())) {
            return false;
        }
        return DataSourceMetaData.super.isInSameDatabaseInstance(dataSourceMetaData);
    }
    
    private boolean isSameModel(final String model1, final String model2) {
        if (MODEL_MEM.equalsIgnoreCase(model1)) {
            return model1.equalsIgnoreCase(model2) || MODEL_PWD.equalsIgnoreCase(model2) || MODEL_FILE.equalsIgnoreCase(model2);
        }
        if (MODEL_PWD.equalsIgnoreCase(model1)) {
            return model1.equalsIgnoreCase(model2) || MODEL_MEM.equalsIgnoreCase(model2) || MODEL_FILE.equalsIgnoreCase(model2);
        }
        if (MODEL_FILE.equalsIgnoreCase(model1)) {
            return model1.equalsIgnoreCase(model2) || MODEL_MEM.equalsIgnoreCase(model2) || MODEL_PWD.equalsIgnoreCase(model2);
        }
        return model1.equalsIgnoreCase(model2);
    }
}
