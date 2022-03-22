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

package org.apache.shardingsphere.infra.metadata.database.loader;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.loader.SchemaLoader;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * Database loader.
 */
public final class DatabaseLoader {
    
    /**
     * Load database.
     * 
     * @param databaseName database name
     * @param dataSourceMap data source map
     * @param rules rules
     * @param props properties
     * @return loaded database
     * @throws SQLException SQL exception
     */
    public static ShardingSphereDatabase load(final String databaseName, final Map<String, DataSource> dataSourceMap, 
                                              final Collection<ShardingSphereRule> rules, final Properties props) throws SQLException {
        ShardingSphereSchema schema = SchemaLoader.load(dataSourceMap, rules, props);
        Map<String, ShardingSphereSchema> schemas = Collections.singletonMap(databaseName, schema);
        return new ShardingSphereDatabase(schemas);
    }
}
