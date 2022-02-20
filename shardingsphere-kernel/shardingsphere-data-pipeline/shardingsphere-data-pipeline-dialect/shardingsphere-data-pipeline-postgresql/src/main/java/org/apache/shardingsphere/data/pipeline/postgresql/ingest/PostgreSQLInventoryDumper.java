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

package org.apache.shardingsphere.data.pipeline.postgresql.ingest;

import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.AbstractInventoryDumper;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.postgresql.util.PGobject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * PostgreSQL JDBC dumper.
 */
public final class PostgreSQLInventoryDumper extends AbstractInventoryDumper {
    
    private static final String PG_MONEY_TYPE = "money";
    
    private static final String PG_BIT_TYPE = "bit";
    
    public PostgreSQLInventoryDumper(final InventoryDumperConfiguration inventoryDumperConfig, final PipelineChannel channel,
                                     final DataSource dataSource, final PipelineTableMetaDataLoader metaDataLoader) {
        super(inventoryDumperConfig, channel, dataSource, metaDataLoader);
    }
    
    @Override
    protected PreparedStatement createPreparedStatement(final Connection connection, final String sql) throws SQLException {
        PreparedStatement result = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        result.setFetchSize(1);
        return result;
    }
    
    @Override
    protected Object readValue(final ResultSet resultSet, final int index) throws SQLException {
        if (isPgMoneyType(resultSet, index)) {
            return resultSet.getBigDecimal(index);
        }
        if (isPgBitType(resultSet, index)) {
            PGobject result = new PGobject();
            result.setType("bit");
            Object resultSetObject = resultSet.getObject(index);
            if (resultSetObject == null) {
                result.setValue(null);
            } else {
                result.setValue((Boolean) resultSetObject ? "1" : "0");
            }
            return result;
        }
        return resultSet.getObject(index);
    }
    
    private boolean isPgMoneyType(final ResultSet resultSet, final int index) throws SQLException {
        return PG_MONEY_TYPE.equalsIgnoreCase(resultSet.getMetaData().getColumnTypeName(index));
    }
    
    private boolean isPgBitType(final ResultSet resultSet, final int index) throws SQLException {
        if (Types.BIT == resultSet.getMetaData().getColumnType(index)) {
            return PG_BIT_TYPE.equalsIgnoreCase(resultSet.getMetaData().getColumnTypeName(index));
        }
        return false;
    }
}
