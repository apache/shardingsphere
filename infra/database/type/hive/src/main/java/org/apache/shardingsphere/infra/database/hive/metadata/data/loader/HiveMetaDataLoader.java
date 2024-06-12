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

package org.apache.shardingsphere.infra.database.hive.metadata.data.loader;

import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.shardingsphere.infra.database.core.metadata.data.loader.DialectMetaDataLoader;
import org.apache.shardingsphere.infra.database.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.database.datatype.DataTypeLoader;
import org.apache.thrift.TException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

/**
 * Hive meta data loader.
 */
public final class HiveMetaDataLoader implements DialectMetaDataLoader {
    
    private static final String HIVE_METASTORE_URIS = "hive.metastore.uris";
    
    @Override
    public Collection<SchemaMetaData> load(final MetaDataLoaderMaterial material) throws SQLException {
        HiveMetaStoreClient storeClient = null;
        try {
            // TODO Support set hive.metastore uris when register storage unit.
            HiveConf hiveConf = new HiveConf();
            hiveConf.set(HIVE_METASTORE_URIS, "");
            storeClient = new HiveMetaStoreClient(hiveConf);
            return Collections.singletonList(new SchemaMetaData(material.getDefaultSchemaName(),
                    getTableMetaData(storeClient.getAllTables(material.getDefaultSchemaName()), storeClient, material)));
        } catch (final TException ignored) {
            throw new SQLException();
        } finally {
            if (null != storeClient) {
                storeClient.close();
            }
        }
    }
    
    private Collection<TableMetaData> getTableMetaData(final Collection<String> tables, final HiveMetaStoreClient storeClient, final MetaDataLoaderMaterial material) throws TException, SQLException {
        Map<String, Integer> dataTypes = getDataType(material.getDataSource());
        Collection<TableMetaData> result = new LinkedList<>();
        for (String each : tables) {
            result.add(new TableMetaData(each, getColumnMetaData(storeClient.getTable(material.getDefaultSchemaName(), each), dataTypes), Collections.emptyList(), Collections.emptyList()));
        }
        return result;
    }
    
    private Map<String, Integer> getDataType(final DataSource dataSource) throws SQLException {
        try (
                Connection connection = dataSource.getConnection()) {
            return new DataTypeLoader().load(connection.getMetaData(), getType());
        }
    }
    
    private Collection<ColumnMetaData> getColumnMetaData(final Table table, final Map<String, Integer> dataTypes) {
        Collection<ColumnMetaData> result = new LinkedList<>();
        for (FieldSchema each : table.getSd().getCols()) {
            result.add(new ColumnMetaData(each.getName(), null == dataTypes.get(each.getType()) ? Types.VARCHAR : dataTypes.get(each.getType()),
                    false, false, false, false, false, false));
        }
        return result;
    }
    
    @Override
    public String getDatabaseType() {
        return "Hive";
    }
}
