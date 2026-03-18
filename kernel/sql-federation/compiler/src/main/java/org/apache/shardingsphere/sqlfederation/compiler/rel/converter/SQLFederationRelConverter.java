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

package org.apache.shardingsphere.sqlfederation.compiler.rel.converter;

import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelOptTable.ViewExpander;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOperatorTable;
import org.apache.calcite.sql.util.SqlOperatorTables;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.sql2rel.SqlToRelConverter.Config;
import org.apache.calcite.sql2rel.StandardConvertletTable;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sqlfederation.compiler.context.CompilerContext;
import org.apache.shardingsphere.sqlfederation.compiler.metadata.catalog.SQLFederationCatalogReader;
import org.apache.shardingsphere.sqlfederation.compiler.metadata.view.ShardingSphereViewExpander;
import org.apache.shardingsphere.sqlfederation.compiler.planner.builder.SQLFederationPlannerBuilder;
import org.apache.shardingsphere.sqlfederation.compiler.sql.type.SQLFederationDataTypeFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * SQL federation rel converter.
 */
public final class SQLFederationRelConverter {
    
    private final SqlToRelConverter sqlToRelConverter;
    
    public SQLFederationRelConverter(final CompilerContext compilerContext, final List<String> schemaPath, final DatabaseType databaseType, final Convention convention) {
        RelDataTypeFactory typeFactory = SQLFederationDataTypeFactory.getInstance();
        CalciteConnectionConfig connectionConfig = compilerContext.getConnectionConfig();
        CalciteCatalogReader catalogReader = new SQLFederationCatalogReader(compilerContext.getCalciteSchema(), schemaPath, typeFactory, connectionConfig);
        SqlValidator validator = createSqlValidator(catalogReader, typeFactory, connectionConfig, compilerContext.getOperatorTables());
        RelOptCluster relOptCluster = createRelOptCluster(typeFactory, convention);
        sqlToRelConverter = createSqlToRelConverter(catalogReader, validator, relOptCluster, compilerContext.getSqlParserRule(), databaseType, true);
    }
    
    private SqlValidator createSqlValidator(final CalciteCatalogReader catalogReader, final RelDataTypeFactory typeFactory,
                                            final CalciteConnectionConfig connectionConfig, final Collection<SqlOperatorTable> operatorTables) {
        SqlValidator.Config validatorConfig = SqlValidator.Config.DEFAULT.withLenientOperatorLookup(connectionConfig.lenientOperatorLookup()).withConformance(connectionConfig.conformance())
                .withDefaultNullCollation(connectionConfig.defaultNullCollation()).withIdentifierExpansion(true);
        SqlOperatorTable sqlOperatorTable = getSQLOperatorTable(operatorTables, catalogReader);
        return SqlValidatorUtil.newValidator(sqlOperatorTable, catalogReader, typeFactory, validatorConfig);
    }
    
    private static SqlOperatorTable getSQLOperatorTable(final Collection<SqlOperatorTable> operatorTables, final CalciteCatalogReader catalogReader) {
        Collection<SqlOperatorTable> allOperatorTables = new LinkedList<>(operatorTables);
        allOperatorTables.add(catalogReader);
        return SqlOperatorTables.chain(allOperatorTables);
    }
    
    private SqlToRelConverter createSqlToRelConverter(final CalciteCatalogReader catalogReader, final SqlValidator validator, final RelOptCluster cluster, final SQLParserRule sqlParserRule,
                                                      final DatabaseType databaseType, final boolean needsViewExpand) {
        ViewExpander expander = needsViewExpand
                ? new ShardingSphereViewExpander(sqlParserRule, databaseType, createSqlToRelConverter(catalogReader, validator, cluster, sqlParserRule, databaseType, false))
                : (rowType, queryString, schemaPath, viewPath) -> null;
        // TODO remove withRemoveSortInSubQuery when calcite can expand view which contains order by correctly
        Config converterConfig = SqlToRelConverter.config().withTrimUnusedFields(true).withRemoveSortInSubQuery(false);
        return new SqlToRelConverter(expander, validator, catalogReader, cluster, StandardConvertletTable.INSTANCE, converterConfig);
    }
    
    private RelOptCluster createRelOptCluster(final RelDataTypeFactory typeFactory, final Convention convention) {
        RelOptPlanner volcanoPlanner = SQLFederationPlannerBuilder.buildVolcanoPlanner(convention);
        return RelOptCluster.create(volcanoPlanner, new RexBuilder(typeFactory));
    }
    
    /**
     * Get schema plus.
     *
     * @return schema plus
     */
    public SchemaPlus getSchemaPlus() {
        return sqlToRelConverter.validator.getCatalogReader().getRootSchema().plus();
    }
    
    /**
     * Convert query.
     *
     * @param sqlNode sql node
     * @param needsValidation need validation
     * @param top top
     * @return rel root
     */
    public RelRoot convertQuery(final SqlNode sqlNode, final boolean needsValidation, final boolean top) {
        return sqlToRelConverter.convertQuery(sqlNode, needsValidation, top);
    }
    
    /**
     * Get validated node type.
     *
     * @param sqlNode sql node
     * @return rel data type
     */
    public RelDataType getValidatedNodeType(final SqlNode sqlNode) {
        return Objects.requireNonNull(sqlToRelConverter.validator).getValidatedNodeType(sqlNode);
    }
    
    /**
     * Get cluster.
     *
     * @return cluster
     */
    public RelOptCluster getCluster() {
        return sqlToRelConverter.getCluster();
    }
}
