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

package org.apache.shardingsphere.infra.optimize.core.metadata.refresher.type;

import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.TableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.optimize.core.metadata.refresher.FederateRefresher;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.type.TableContainedRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import javax.sql.DataSource;

/**
 * Abstract federate refresher.
 */
public abstract class AbstractFederateRefresher<T extends SQLStatement> implements FederateRefresher<T> {
    
    protected TableMetaData loadTableMetaData(final String tableName, final Collection<String> routeDataSourceNames, final SchemaBuilderMaterials materials) throws SQLException {
        for (String routeDataSourceName : routeDataSourceNames) {
            DataSource dataSource = materials.getDataSourceMap().get(routeDataSourceName);
            Optional<TableMetaData> tableMetaDataOptional = Objects.isNull(dataSource) ? Optional.empty() : TableMetaDataLoader.load(dataSource, tableName, materials.getDatabaseType());
            if (!tableMetaDataOptional.isPresent()) {
                continue;
            }
            return tableMetaDataOptional.get();
        }
        return new TableMetaData();
    }
    
    protected boolean containsInTableContainedRule(final String tableName, final SchemaBuilderMaterials materials) {
        for (ShardingSphereRule each : materials.getRules()) {
            if (each instanceof TableContainedRule && ((TableContainedRule) each).getTables().contains(tableName)) {
                return true;
            }
        }
        return false;
    }
}
