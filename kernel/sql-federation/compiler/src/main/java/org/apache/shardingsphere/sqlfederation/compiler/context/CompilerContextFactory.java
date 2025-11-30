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

package org.apache.shardingsphere.sqlfederation.compiler.context;

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.sql.SqlOperatorTable;
import org.apache.calcite.sql.fun.SqlLibrary;
import org.apache.calcite.sql.fun.SqlLibraryOperatorTableFactory;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.parser.rule.builder.SQLParserRuleBuilder;
import org.apache.shardingsphere.sqlfederation.compiler.context.connection.config.ConnectionConfigBuilderFactory;
import org.apache.shardingsphere.sqlfederation.compiler.context.schema.CalciteSchemaBuilder;
import org.apache.shardingsphere.sqlfederation.compiler.sql.function.mysql.MySQLOperatorTable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Compiler context factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CompilerContextFactory {
    
    private static final Map<String, SqlLibrary> DATABASE_TYPE_SQL_LIBRARIES = new CaseInsensitiveMap<>();
    
    static {
        DATABASE_TYPE_SQL_LIBRARIES.put("MySQL", SqlLibrary.MYSQL);
        DATABASE_TYPE_SQL_LIBRARIES.put("PostgreSQL", SqlLibrary.POSTGRESQL);
        DATABASE_TYPE_SQL_LIBRARIES.put("openGauss", SqlLibrary.POSTGRESQL);
        DATABASE_TYPE_SQL_LIBRARIES.put("Oracle", SqlLibrary.ORACLE);
    }
    
    /**
     * Create compiler context.
     *
     * @param databases databases
     * @return created compiler context
     */
    public static CompilerContext create(final Collection<ShardingSphereDatabase> databases) {
        // TODO consider to use sqlParserRule in global rule
        SQLParserRule sqlParserRule = new SQLParserRuleBuilder().build(new DefaultSQLParserRuleConfigurationBuilder().build(), databases, new ConfigurationProperties(new Properties()));
        DatabaseType databaseType = databases.isEmpty() ? DatabaseTypeEngine.getDefaultStorageType() : databases.iterator().next().getProtocolType();
        CalciteConnectionConfig connectionConfig = new ConnectionConfigBuilderFactory(databaseType).build();
        CalciteSchema calciteSchema = CalciteSchemaBuilder.build(databases);
        return new CompilerContext(sqlParserRule, calciteSchema, connectionConfig, getOperatorTables(databaseType));
    }
    
    private static Collection<SqlOperatorTable> getOperatorTables(final DatabaseType databaseType) {
        SqlOperatorTable operatorTable = SqlLibraryOperatorTableFactory.INSTANCE.getOperatorTable(
                Arrays.asList(SqlLibrary.STANDARD, DATABASE_TYPE_SQL_LIBRARIES.getOrDefault(databaseType.getType(), SqlLibrary.MYSQL)));
        return Arrays.asList(new MySQLOperatorTable(), operatorTable);
    }
}
