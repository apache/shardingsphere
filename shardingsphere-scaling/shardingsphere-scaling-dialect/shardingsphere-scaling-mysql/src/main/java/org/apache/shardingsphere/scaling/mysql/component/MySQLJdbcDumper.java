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

package org.apache.shardingsphere.scaling.mysql.component;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.scaling.core.config.InventoryDumperConfiguration;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.execute.executor.dumper.AbstractJDBCDumper;
import org.apache.shardingsphere.scaling.core.utils.JDBCUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * MySQL JDBC Dumper.
 */
public final class MySQLJdbcDumper extends AbstractJDBCDumper {
    
    public MySQLJdbcDumper(final InventoryDumperConfiguration inventoryDumperConfig, final DataSourceManager dataSourceManager) {
        super(inventoryDumperConfig, dataSourceManager);
        JDBCUtil.appendJDBCParameter(inventoryDumperConfig.getDataSourceConfig(), ImmutableMap.<String, String>builder().put("yearIsDateType", "false").build());
    }
    
    @Override
    public Object readValue(final ResultSet resultSet, final int index) throws SQLException {
        if (isDateTimeValue(resultSet.getMetaData().getColumnType(index))) {
            return resultSet.getString(index);
        } else {
            return resultSet.getObject(index);
        }
    }
    
    private boolean isDateTimeValue(final int columnType) {
        return Types.TIME == columnType || Types.DATE == columnType || Types.TIMESTAMP == columnType;
    }
    
    @Override
    protected PreparedStatement createPreparedStatement(final Connection connection, final String sql) throws SQLException {
        PreparedStatement result = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        result.setFetchSize(Integer.MIN_VALUE);
        return result;
    }
}
