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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

/**
 * Shadow data source.
 */
@Getter
public final class ShadowDataSource extends AbstractDataSourceAdapter {
    
    private static final String ACTUAL_DATABASE = "actual";
    
    private static final String SHADOW_DATABASE = "shadow";
    
    private final DataSource actualDataSource;
    
    private final DataSource shadowDataSource;
    
    public ShadowDataSource(final DataSource actualDataSource, final DataSource shadowDataSource, final ShadowRule shadowRule, final Properties props) throws SQLException {
        super(ImmutableMap.of(ACTUAL_DATABASE, actualDataSource, SHADOW_DATABASE, shadowDataSource), Collections.singletonList(shadowRule), props);
        this.actualDataSource = actualDataSource;
        this.shadowDataSource = shadowDataSource;
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return new ShadowConnection(getDataSourceMap().get(ACTUAL_DATABASE).getConnection(), getDataSourceMap().get(SHADOW_DATABASE).getConnection(), getRuntimeContext());
    }
}
