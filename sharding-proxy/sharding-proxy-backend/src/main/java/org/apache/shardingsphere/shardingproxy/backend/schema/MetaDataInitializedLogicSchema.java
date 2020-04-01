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

package org.apache.shardingsphere.shardingproxy.backend.schema;

import lombok.Getter;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.shardingproxy.context.ShardingProxyContext;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaDataLoader;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.underlying.common.metadata.datasource.DataSourceMetas;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

/**
 * Logic schema with initialized physicalMetaData.
 */
@Getter
public abstract class MetaDataInitializedLogicSchema extends LogicSchema {

    private final ShardingSphereMetaData physicalMetaData;

    public MetaDataInitializedLogicSchema(final String name, final Map<String, YamlDataSourceParameter> dataSources) throws SQLException {
        super(name, dataSources);
        physicalMetaData = createPhysicalMetaData();
    }

    private ShardingSphereMetaData createPhysicalMetaData() throws SQLException {
        DataSourceMetas dataSourceMetas = new DataSourceMetas(LogicSchemas.getInstance().getDatabaseType(), getDatabaseAccessConfigurationMap());
        SchemaMetaData schemaMetaData = createSchemaMetaData();
        return new ShardingSphereMetaData(dataSourceMetas, schemaMetaData);
    }

    private SchemaMetaData createSchemaMetaData() throws SQLException {
        DataSource dataSource = getBackendDataSource().getDataSources().values().iterator().next();
        int maxConnectionsSizePerQuery = ShardingProxyContext.getInstance().getProperties().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return SchemaMetaDataLoader.load(dataSource, maxConnectionsSizePerQuery, LogicSchemas.getInstance().getDatabaseType().getName());
    }
}
