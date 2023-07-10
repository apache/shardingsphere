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

package org.apache.shardingsphere.infra.datasource.pool.metadata.type.dbcp;

import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolPropertiesValidator;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DefaultDataSourcePoolPropertiesValidator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

/**
 * DBCP data source pool meta data.
 */
public final class DBCPDataSourcePoolMetaData implements DataSourcePoolMetaData {
    
    private static final Collection<String> TRANSIENT_FIELD_NAMES = new LinkedList<>();
    
    static {
        buildTransientFieldNames();
    }
    
    private static void buildTransientFieldNames() {
        TRANSIENT_FIELD_NAMES.add("closed");
    }
    
    @Override
    public Map<String, Object> getDefaultProperties() {
        return Collections.emptyMap();
    }
    
    @Override
    public Map<String, Object> getInvalidProperties() {
        return Collections.emptyMap();
    }
    
    @Override
    public Map<String, String> getPropertySynonyms() {
        return Collections.emptyMap();
    }
    
    @Override
    public Collection<String> getTransientFieldNames() {
        return TRANSIENT_FIELD_NAMES;
    }
    
    @Override
    public DBCPDataSourcePoolFieldMetaData getFieldMetaData() {
        return new DBCPDataSourcePoolFieldMetaData();
    }
    
    @Override
    public String getType() {
        return "org.apache.commons.dbcp2.BasicDataSource";
    }
    
    @Override
    public Collection<Object> getTypeAliases() {
        return Arrays.asList("org.apache.commons.dbcp.BasicDataSource", "org.apache.tomcat.dbcp.dbcp2.BasicDataSource");
    }
    
    @Override
    public DataSourcePoolPropertiesValidator getDataSourcePoolPropertiesValidator() {
        return new DefaultDataSourcePoolPropertiesValidator();
    }
}
