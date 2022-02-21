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

package org.apache.shardingsphere.data.pipeline.mysql.ingest;

import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.AbstractInventoryDumper;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

/**
 * MySQL JDBC Dumper.
 */
public final class MySQLInventoryDumper extends AbstractInventoryDumper {
    
    private static final String YEAR_DATA_TYPE = "YEAR";
    
    public MySQLInventoryDumper(final InventoryDumperConfiguration inventoryDumperConfig, final PipelineChannel channel,
                                final DataSource dataSource, final PipelineTableMetaDataLoader metaDataLoader) {
        super(inventoryDumperConfig, channel, dataSource, metaDataLoader);
        Properties queryProps = new Properties();
        queryProps.setProperty("yearIsDateType", Boolean.FALSE.toString());
        inventoryDumperConfig.getDataSourceConfig().appendJDBCQueryProperties(queryProps);
    }
    
    @Override
    public Object readValue(final ResultSet resultSet, final int index) throws SQLException {
        if (isYearDataType(resultSet.getMetaData().getColumnTypeName(index))) {
            Object result = resultSet.getObject(index);
            return resultSet.wasNull() ? null : result;
        } else if (isDateTimeValue(resultSet.getMetaData().getColumnType(index))) {
            return resultSet.getString(index);
        } else {
            return resultSet.getObject(index);
        }
    }
    
    private boolean isDateTimeValue(final int columnType) {
        return Types.TIME == columnType || Types.DATE == columnType || Types.TIMESTAMP == columnType;
    }
    
    private boolean isYearDataType(final String columnDataTypeName) {
        return YEAR_DATA_TYPE.equalsIgnoreCase(columnDataTypeName);
    }
    
    @Override
    protected PreparedStatement createPreparedStatement(final Connection connection, final String sql) throws SQLException {
        PreparedStatement result = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        result.setFetchSize(Integer.MIN_VALUE);
        return result;
    }
}
