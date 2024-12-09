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
import org.apache.hadoop.hive.metastore.api.GetTableRequest;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.shardingsphere.infra.database.core.metadata.data.loader.DialectMetaDataLoader;
import org.apache.shardingsphere.infra.database.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.infra.database.core.metadata.data.loader.type.TableMetaDataLoader;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.database.datatype.DataTypeRegistry;
import org.apache.thrift.TException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Hive meta data loader.
 */
public final class HiveMetaDataLoader implements DialectMetaDataLoader {
    
    private static final String HIVE_METASTORE_URIS = "hive.metastore.uris";
    
    @SuppressWarnings("SqlNoDataSourceInspection")
    @Override
    public Collection<SchemaMetaData> load(final MetaDataLoaderMaterial material) throws SQLException {
        String hiveMetastoreUris;
        try (Statement statement = material.getDataSource().getConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery("SET hive.metastore.uris");
            resultSet.next();
            hiveMetastoreUris = resultSet.getString("set");
        }
        if ("hive.metastore.uris is undefined".equals(hiveMetastoreUris)) {
            Collection<TableMetaData> tableMetaData = new LinkedList<>();
            for (String each : material.getActualTableNames()) {
                TableMetaDataLoader.load(material.getDataSource(), each, material.getStorageType()).ifPresent(tableMetaData::add);
            }
            return Collections.singletonList(new SchemaMetaData(material.getDefaultSchemaName(), tableMetaData));
        }
        HiveMetaStoreClient storeClient = null;
        try {
            // TODO Support set hive.metastore uris when register storage unit.
            HiveConf hiveConf = new HiveConf();
            hiveConf.set(HIVE_METASTORE_URIS, hiveMetastoreUris);
            storeClient = new HiveMetaStoreClient(hiveConf);
            return Collections.singletonList(new SchemaMetaData(material.getDefaultSchemaName(), getTableMetaData(storeClient.getAllTables(material.getDefaultSchemaName()), storeClient, material)));
        } catch (final TException ignored) {
            throw new SQLException();
        } finally {
            if (null != storeClient) {
                storeClient.close();
            }
        }
    }
    
    private Collection<TableMetaData> getTableMetaData(final Collection<String> tables, final HiveMetaStoreClient storeClient, final MetaDataLoaderMaterial material) throws TException {
        Collection<TableMetaData> result = new LinkedList<>();
        for (String each : tables) {
            GetTableRequest req = new GetTableRequest(material.getDefaultSchemaName(), each);
            result.add(new TableMetaData(each, getColumnMetaData(storeClient.getTable(req)), Collections.emptyList(), Collections.emptyList()));
        }
        return result;
    }
    
    private Collection<ColumnMetaData> getColumnMetaData(final Table table) {
        Collection<ColumnMetaData> result = new LinkedList<>();
        for (FieldSchema each : table.getSd().getCols()) {
            result.add(new ColumnMetaData(each.getName(), DataTypeRegistry.getDataType(getDatabaseType(), each.getType()).orElse(Types.VARCHAR), false, false, false, false, false, false));
        }
        return result;
    }
    
    @Override
    public String getDatabaseType() {
        return "Hive";
    }
}
