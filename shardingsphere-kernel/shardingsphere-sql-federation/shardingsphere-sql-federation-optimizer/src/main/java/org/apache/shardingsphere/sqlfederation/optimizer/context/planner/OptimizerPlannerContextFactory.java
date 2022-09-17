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

package org.apache.shardingsphere.sqlfederation.optimizer.context.planner;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.plan.RelOptCluster;
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
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sqlfederation.optimizer.planner.QueryOptimizePlannerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Optimizer planner context factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OptimizerPlannerContextFactory {
    
    private static final Map<String, SqlLibrary> DATABASE_TYPE_SQL_LIBRARIES = new HashMap<>();
    
    static {
        DATABASE_TYPE_SQL_LIBRARIES.put(SqlLibrary.MYSQL.name().toLowerCase(), SqlLibrary.MYSQL);
        DATABASE_TYPE_SQL_LIBRARIES.put(SqlLibrary.POSTGRESQL.name().toLowerCase(), SqlLibrary.POSTGRESQL);
        DATABASE_TYPE_SQL_LIBRARIES.put(SqlLibrary.ORACLE.name(), SqlLibrary.ORACLE);
        DATABASE_TYPE_SQL_LIBRARIES.put("openGauss", SqlLibrary.POSTGRESQL);
    }
    
    /**
     * Create optimizer planner context map.
     *
     * @param databases databases
     * @return created optimizer planner context map
     */
    public static Map<String, OptimizerPlannerContext> create(final Map<String, ShardingSphereDatabase> databases) {
        Map<String, OptimizerPlannerContext> result = new ConcurrentHashMap<>(databases.size(), 1);
        for (Entry<String, ShardingSphereDatabase> entry : databases.entrySet()) {
            result.put(entry.getKey(), new OptimizerPlannerContext(QueryOptimizePlannerFactory.createHepPlanner()));
        }
        return result;
    }
    
    /**
     * Create catalog reader.
     * 
     * @param schemaName schema name
     * @param schema schema
     * @param relDataTypeFactory rel data type factory
     * @param connectionConfig connection config
     * @return calcite catalog reader
     */
    public static CalciteCatalogReader createCatalogReader(final String schemaName, final Schema schema, final RelDataTypeFactory relDataTypeFactory, final CalciteConnectionConfig connectionConfig) {
        CalciteSchema rootSchema = CalciteSchema.createRootSchema(true);
        rootSchema.add(schemaName, schema);
        return new CalciteCatalogReader(rootSchema, Collections.singletonList(schemaName), relDataTypeFactory, connectionConfig);
    }
    
    /**
     * Create validator.
     *
     * @param catalogReader catalog reader
     * @param relDataTypeFactory rel data type factory
     * @param databaseType database type
     * @param connectionConfig connection config
     * @return sql validator
     */
    public static SqlValidator createValidator(final CalciteCatalogReader catalogReader, final RelDataTypeFactory relDataTypeFactory,
                                               final DatabaseType databaseType, final CalciteConnectionConfig connectionConfig) {
        SqlValidator.Config validatorConfig = SqlValidator.Config.DEFAULT
                .withLenientOperatorLookup(connectionConfig.lenientOperatorLookup())
                .withConformance(connectionConfig.conformance())
                .withDefaultNullCollation(connectionConfig.defaultNullCollation())
                .withIdentifierExpansion(true);
        SqlOperatorTable sqlOperatorTable = getSQLOperatorTable(catalogReader, databaseType);
        return SqlValidatorUtil.newValidator(sqlOperatorTable, catalogReader, relDataTypeFactory, validatorConfig);
    }
    
    private static SqlOperatorTable getSQLOperatorTable(final CalciteCatalogReader catalogReader, final DatabaseType databaseType) {
        return SqlOperatorTables.chain(Arrays.asList(SqlLibraryOperatorTableFactory.INSTANCE.getOperatorTable(
                Arrays.asList(SqlLibrary.STANDARD, DATABASE_TYPE_SQL_LIBRARIES.getOrDefault(databaseType.getType(), SqlLibrary.MYSQL))), catalogReader));
    }
    
    /**
     * Create Converter.
     * 
     * @param catalogReader catalog reader
     * @param validator validator
     * @param relDataTypeFactory rel data type factory
     * @return sql to rel converter
     */
    public static SqlToRelConverter createConverter(final CalciteCatalogReader catalogReader, final SqlValidator validator, final RelDataTypeFactory relDataTypeFactory) {
        ViewExpander expander = (rowType, queryString, schemaPath, viewPath) -> null;
        Config converterConfig = SqlToRelConverter.config().withTrimUnusedFields(true);
        RelOptCluster cluster = RelOptCluster.create(QueryOptimizePlannerFactory.createVolcanoPlanner(), new RexBuilder(relDataTypeFactory));
        return new SqlToRelConverter(expander, validator, catalogReader, cluster, StandardConvertletTable.INSTANCE, converterConfig);
    }
}
