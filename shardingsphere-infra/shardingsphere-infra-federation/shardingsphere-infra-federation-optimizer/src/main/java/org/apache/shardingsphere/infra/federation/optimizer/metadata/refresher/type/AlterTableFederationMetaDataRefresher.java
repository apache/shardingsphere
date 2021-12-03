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

package org.apache.shardingsphere.infra.federation.optimizer.metadata.refresher.type;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationSchemaMetaData;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.refresher.FederationMetaDataRefresher;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.builder.TableMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Federation meta data refresher for alter table.
 */
public final class AlterTableFederationMetaDataRefresher implements FederationMetaDataRefresher<AlterTableStatement> {
    
    @Override
    public void refresh(final FederationSchemaMetaData schema, final Collection<String> logicDataSourceNames, final AlterTableStatement sqlStatement, 
                        final ShardingSphereMetaData schemaMetaData, final ConfigurationProperties props) throws SQLException {
        SchemaBuilderMaterials materials = new SchemaBuilderMaterials(schemaMetaData.getResource().getDatabaseType(), schemaMetaData.getResource().getDataSources(),
                schemaMetaData.getRuleMetaData().getRules(), props);
        String tableName = sqlStatement.getTable().getTableName().getIdentifier().getValue();
        if (sqlStatement.getRenameTable().isPresent()) {
            String renameTableName = sqlStatement.getRenameTable().get().getTableName().getIdentifier().getValue();
            buildTableMetaData(materials, renameTableName).ifPresent(schema::put);
            schema.remove(tableName);
        } else {
            buildTableMetaData(materials, tableName).ifPresent(schema::put);
        }
    }
    
    private Optional<TableMetaData> buildTableMetaData(final SchemaBuilderMaterials materials, final String tableName) throws SQLException {
        return Optional.ofNullable(TableMetaDataBuilder.load(Collections.singletonList(tableName), materials).get(tableName))
                .map(each -> TableMetaDataBuilder.decorateFederationTableMetaData(each, materials.getRules()));
    }
}
