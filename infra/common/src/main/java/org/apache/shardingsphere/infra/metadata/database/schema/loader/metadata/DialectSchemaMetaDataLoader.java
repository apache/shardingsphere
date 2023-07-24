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

package org.apache.shardingsphere.infra.metadata.database.schema.loader.metadata;

import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Dialect schema meta data loader.
 */
@SingletonSPI
public interface DialectSchemaMetaDataLoader extends DatabaseTypedSPI {
    
    /**
     * Load schema meta data.
     *
     * @param dataSource data source
     * @param tables tables
     * @param defaultSchemaName default schema name
     * @return schema meta data collection
     * @throws SQLException SQL exception
     */
    Collection<SchemaMetaData> load(DataSource dataSource, Collection<String> tables, String defaultSchemaName) throws SQLException;
}
