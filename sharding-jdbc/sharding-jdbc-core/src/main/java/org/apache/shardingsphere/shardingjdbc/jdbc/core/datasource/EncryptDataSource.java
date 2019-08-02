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

import lombok.Getter;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.adapter.AbstractDataSourceAdapter;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.EncryptConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.EncryptRuntimeContext;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Encrypt data source.
 *
 * @author panjuan
 */
@Getter
public class EncryptDataSource extends AbstractDataSourceAdapter {
    
    private final EncryptRuntimeContext runtimeContext;
    
    public EncryptDataSource(final DataSource dataSource, final EncryptRule encryptRule, final Properties props) throws SQLException {
        super(dataSource);
        runtimeContext = new EncryptRuntimeContext(dataSource, encryptRule, props, getDatabaseType());
    }
    
    @Override
    public final EncryptConnection getConnection() throws SQLException {
        return new EncryptConnection(getDataSource().getConnection(), runtimeContext);
    }
    
    /**
     * Get data source.
     *
     * @return data source
     */
    public DataSource getDataSource() {
        return getDataSourceMap().values().iterator().next();
    }
}
