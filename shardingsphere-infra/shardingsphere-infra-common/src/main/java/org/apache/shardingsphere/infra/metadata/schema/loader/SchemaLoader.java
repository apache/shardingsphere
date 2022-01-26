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

package org.apache.shardingsphere.infra.metadata.schema.loader;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRecognizer;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.builder.TableMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Schema loader.
 */
public final class SchemaLoader {
    
    /**
     * Load schema.
     * 
     * @param dataSourceMap data source map
     * @param rules rules
     * @param props properties
     * @return loaded schema
     * @throws SQLException SQL exception
     */
    public static ShardingSphereSchema load(final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> rules, final Properties props) throws SQLException {
        DatabaseType databaseType = DatabaseTypeRecognizer.getDatabaseType(dataSourceMap.values());
        Map<String, TableMetaData> tableMetaDataMap = TableMetaDataBuilder.load(
                getAllTableNames(rules), new SchemaBuilderMaterials(databaseType, dataSourceMap, rules, new ConfigurationProperties(null == props ? new Properties() : props)));
        return new ShardingSphereSchema(tableMetaDataMap);
    }
    
    private static Collection<String> getAllTableNames(final Collection<ShardingSphereRule> rules) {
        return rules.stream().filter(rule -> rule instanceof TableContainedRule)
                .flatMap(shardingSphereRule -> ((TableContainedRule) shardingSphereRule).getTables().stream()).collect(Collectors.toSet());
    }
}
