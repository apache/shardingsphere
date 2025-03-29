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

package org.apache.shardingsphere.sqlfederation.optimizer.metadata.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelOptTable.ViewExpander;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.sql.SqlOperatorTable;
import org.apache.calcite.sql.fun.SqlLibrary;
import org.apache.calcite.sql.fun.SqlLibraryOperatorTableFactory;
import org.apache.calcite.sql.util.SqlOperatorTables;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.sql2rel.SqlToRelConverter.Config;
import org.apache.calcite.sql2rel.StandardConvertletTable;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sqlfederation.optimizer.function.SQLFederationFunctionRegister;
import org.apache.shardingsphere.sqlfederation.optimizer.function.mysql.MySQLOperatorTable;
import org.apache.shardingsphere.sqlfederation.optimizer.metadata.view.ShardingSphereViewExpander;
import org.apache.shardingsphere.sqlfederation.optimizer.planner.builder.SQLFederationPlannerBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * SQL federation validator utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLFederationValidatorUtils {
    
    private static final Map<String, SqlLibrary> DATABASE_TYPE_SQL_LIBRARIES = new HashMap<>();
    
    static {
        DATABASE_TYPE_SQL_LIBRARIES.put("MySQL", SqlLibrary.MYSQL);
        DATABASE_TYPE_SQL_LIBRARIES.put("PostgreSQL", SqlLibrary.POSTGRESQL);
        DATABASE_TYPE_SQL_LIBRARIES.put("openGauss", SqlLibrary.POSTGRESQL);
        DATABASE_TYPE_SQL_LIBRARIES.put("Oracle", SqlLibrary.ORACLE);
    }
    
    /**
     * Create catalog reader.
     *
     * @param schemaName schema name
     * @param schema schema
     * @param relDataTypeFactory rel data type factory
     * @param connectionConfig connection config
     * @param databaseType database type
     * @return calcite catalog reader
     */
    public static CalciteCatalogReader createCatalogReader(final String schemaName, final Schema schema, final RelDataTypeFactory relDataTypeFactory, final CalciteConnectionConfig connectionConfig,
                                                           final DatabaseType databaseType) {
        CalciteSchema rootSchema = CalciteSchema.createRootSchema(true);
        rootSchema.add(schemaName, schema);
        DatabaseTypedSPILoader.findService(SQLFederationFunctionRegister.class, databaseType).ifPresent(optional -> optional.registerFunction(rootSchema.plus(), schemaName));
        return new CalciteCatalogReader(rootSchema, Collections.singletonList(schemaName), relDataTypeFactory, connectionConfig);
    }
    
    /**
     * Create sql validator.
     *
     * @param catalogReader catalog reader
     * @param relDataTypeFactory rel data type factory
     * @param databaseType database type
     * @param connectionConfig connection config
     * @return sql validator
     */
    public static SqlValidator createSqlValidator(final CalciteCatalogReader catalogReader, final RelDataTypeFactory relDataTypeFactory, final DatabaseType databaseType,
                                                  final CalciteConnectionConfig connectionConfig) {
        SqlValidator.Config validatorConfig = SqlValidator.Config.DEFAULT.withLenientOperatorLookup(connectionConfig.lenientOperatorLookup()).withConformance(connectionConfig.conformance())
                .withDefaultNullCollation(connectionConfig.defaultNullCollation()).withIdentifierExpansion(true);
        SqlOperatorTable sqlOperatorTable = getSQLOperatorTable(catalogReader, databaseType.getTrunkDatabaseType().orElse(databaseType));
        return SqlValidatorUtil.newValidator(sqlOperatorTable, catalogReader, relDataTypeFactory, validatorConfig);
    }
    
    private static SqlOperatorTable getSQLOperatorTable(final CalciteCatalogReader catalogReader, final DatabaseType databaseType) {
        return SqlOperatorTables.chain(Arrays.asList(new MySQLOperatorTable(),
                SqlLibraryOperatorTableFactory.INSTANCE.getOperatorTable(Arrays.asList(SqlLibrary.STANDARD, DATABASE_TYPE_SQL_LIBRARIES.getOrDefault(databaseType.getType(), SqlLibrary.MYSQL))),
                catalogReader));
    }
    
    /**
     * Create sql to rel converter.
     *
     * @param catalogReader catalog reader
     * @param validator validator
     * @param cluster cluster
     * @param sqlParserRule sql parser rule
     * @param databaseType database type
     * @param needsViewExpand whether sql needs view expand or not
     * @return sql to rel converter
     */
    public static SqlToRelConverter createSqlToRelConverter(final CalciteCatalogReader catalogReader, final SqlValidator validator, final RelOptCluster cluster, final SQLParserRule sqlParserRule,
                                                            final DatabaseType databaseType, final boolean needsViewExpand) {
        ViewExpander expander =
                needsViewExpand ? new ShardingSphereViewExpander(sqlParserRule, databaseType, createSqlToRelConverter(catalogReader, validator, cluster, sqlParserRule, databaseType, false))
                        : (rowType, queryString, schemaPath, viewPath) -> null;
        // TODO remove withRemoveSortInSubQuery when calcite can expand view which contains order by correctly
        Config converterConfig = SqlToRelConverter.config().withTrimUnusedFields(true).withRemoveSortInSubQuery(false).withExpand(true);
        return new SqlToRelConverter(expander, validator, catalogReader, cluster, StandardConvertletTable.INSTANCE, converterConfig);
    }
    
    /**
     * Create rel opt cluster.
     *
     * @param relDataTypeFactory rel data type factory
     * @param convention convention
     * @return rel opt cluster
     */
    public static RelOptCluster createRelOptCluster(final RelDataTypeFactory relDataTypeFactory, final Convention convention) {
        RelOptPlanner volcanoPlanner = SQLFederationPlannerBuilder.buildVolcanoPlanner(convention);
        return RelOptCluster.create(volcanoPlanner, new RexBuilder(relDataTypeFactory));
    }
}
