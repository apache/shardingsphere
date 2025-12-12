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

package org.apache.shardingsphere.data.pipeline.core.metadata.generator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.dialect.DialectPipelineSQLBuilder;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Pipeline DDL generator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class PipelineDDLGenerator {
    
    /**
     * Generate logic DDL.
     *
     * @param databaseType database type
     * @param sourceDataSource source data source
     * @param schemaName schema name
     * @param sourceTableName source table name
     * @param targetTableName target table name
     * @return DDL SQL
     * @throws SQLException SQL exception 
     */
    public static List<String> generateLogicDDL(final DatabaseType databaseType, final DataSource sourceDataSource,
                                                final String schemaName, final String sourceTableName, final String targetTableName) throws SQLException {
        long startTimeMillis = System.currentTimeMillis();
        List<String> result = new ArrayList<>(DatabaseTypedSPILoader.getService(DialectPipelineSQLBuilder.class, databaseType)
                .buildCreateTableSQLs(sourceDataSource, schemaName, sourceTableName));
        log.info("generateLogicDDL, databaseType={}, schemaName={}, sourceTableName={}, targetTableName={}, cost {} ms",
                databaseType.getType(), schemaName, sourceTableName, targetTableName, System.currentTimeMillis() - startTimeMillis);
        return result;
    }
}
