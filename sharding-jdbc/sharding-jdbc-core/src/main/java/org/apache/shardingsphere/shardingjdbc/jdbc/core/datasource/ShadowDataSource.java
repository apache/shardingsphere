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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import org.apache.shardingsphere.core.rule.ShadowRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.adapter.AbstractDataSourceAdapter;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.ShadowConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.ShadowRuntimeContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Shadow data source.
 *
 * @author zhyee
 */
@Getter
public class ShadowDataSource extends AbstractDataSourceAdapter {
    
    private static final String ACTUAL_DATABASE = "actual";
    
    private static final String SHADOW_DATABASE = "shadow";
    
    private final ShadowRuntimeContext runtimeContext;
    
    public ShadowDataSource(final DataSource actualDataSource, final DataSource shadowDataSource, final ShadowRule shadowRule, final Properties props) throws SQLException {
        super(ImmutableMap.of(ACTUAL_DATABASE, actualDataSource, SHADOW_DATABASE, shadowDataSource));
        runtimeContext = new ShadowRuntimeContext(actualDataSource, shadowDataSource, shadowRule, props, getDatabaseType());
    }
    
    @Override
    public final Connection getConnection() throws SQLException {
        return new ShadowConnection(getDataSourceMap().get(ACTUAL_DATABASE).getConnection(), getDataSourceMap().get(SHADOW_DATABASE).getConnection(), runtimeContext);
    }
}
