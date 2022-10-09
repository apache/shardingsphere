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

package org.apache.shardingsphere.sqlfederation.optimizer.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.adapter.enumerable.EnumerableRules;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelOptRule;
import org.apache.calcite.plan.RelOptTable.ViewExpander;
import org.apache.calcite.plan.hep.HepMatchOrder;
import org.apache.calcite.plan.hep.HepPlanner;
import org.apache.calcite.plan.hep.HepProgramBuilder;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.RelCollationTraitDef;
import org.apache.calcite.rel.rules.AggregateExpandDistinctAggregatesRule;
import org.apache.calcite.rel.rules.CoreRules;
import org.apache.calcite.rel.rules.ProjectRemoveRule;
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
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sqlfederation.optimizer.metadata.expander.ShardingSphereViewExpander;
import org.apache.shardingsphere.sqlfederation.optimizer.metadata.translatable.TranslatableFilterRule;
import org.apache.shardingsphere.sqlfederation.optimizer.metadata.translatable.TranslatableProjectFilterRule;
import org.apache.shardingsphere.sqlfederation.optimizer.metadata.translatable.TranslatableProjectRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * SQL federation planner util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLFederationPlannerUtil {
    
    private static final int DEFAULT_MATCH_LIMIT = 1024;
    
    private static final Map<String, SqlLibrary> DATABASE_TYPE_SQL_LIBRARIES = new HashMap<>();
    
    static {
        DATABASE_TYPE_SQL_LIBRARIES.put("MySQL", SqlLibrary.MYSQL);
        DATABASE_TYPE_SQL_LIBRARIES.put("PostgreSQL", SqlLibrary.POSTGRESQL);
        DATABASE_TYPE_SQL_LIBRARIES.put("openGauss", SqlLibrary.POSTGRESQL);
        DATABASE_TYPE_SQL_LIBRARIES.put("Oracle", SqlLibrary.ORACLE);
    }
    
    /**
     * Create new instance of volcano planner.
     *
     * @return volcano planner instance
     */
    public static RelOptPlanner createVolcanoPlanner() {
        RelOptPlanner result = new VolcanoPlanner();
        setUpRules(result);
        return result;
    }
    
    /**
     * Create new instance of hep planner.
     *
     * @return hep planner instance
     */
    public static RelOptPlanner createHepPlanner() {
        HepProgramBuilder builder = new HepProgramBuilder();
        builder.addGroupBegin().addRuleCollection(getFilterRules()).addGroupEnd().addMatchOrder(HepMatchOrder.BOTTOM_UP);
        builder.addGroupBegin().addRuleCollection(getProjectRules()).addGroupEnd().addMatchOrder(HepMatchOrder.BOTTOM_UP);
        builder.addGroupBegin().addRuleCollection(getAggregationRules()).addGroupEnd().addMatchOrder(HepMatchOrder.BOTTOM_UP);
        builder.addGroupBegin().addRuleCollection(getCalcRules()).addGroupEnd().addMatchOrder(HepMatchOrder.BOTTOM_UP);
        builder.addGroupBegin().addRuleCollection(getSubQueryRules()).addGroupEnd().addMatchOrder(HepMatchOrder.BOTTOM_UP);
        builder.addMatchLimit(DEFAULT_MATCH_LIMIT);
        return new HepPlanner(builder.build());
    }
    
    private static void setUpRules(final RelOptPlanner planner) {
        planner.addRelTraitDef(ConventionTraitDef.INSTANCE);
        planner.addRelTraitDef(RelCollationTraitDef.INSTANCE);
        EnumerableRules.rules().forEach(planner::addRule);
    }
    
    private static Collection<RelOptRule> getSubQueryRules() {
        Collection<RelOptRule> result = new LinkedList<>();
        result.add(CoreRules.FILTER_SUB_QUERY_TO_CORRELATE);
        result.add(CoreRules.PROJECT_SUB_QUERY_TO_CORRELATE);
        result.add(CoreRules.JOIN_SUB_QUERY_TO_CORRELATE);
        return result;
    }
    
    private static Collection<RelOptRule> getCalcRules() {
        Collection<RelOptRule> result = new LinkedList<>();
        result.add(AggregateExpandDistinctAggregatesRule.Config.DEFAULT.toRule());
        result.add(CoreRules.PROJECT_TO_CALC);
        result.add(CoreRules.FILTER_TO_CALC);
        result.add(CoreRules.PROJECT_CALC_MERGE);
        result.add(CoreRules.FILTER_CALC_MERGE);
        result.add(EnumerableRules.ENUMERABLE_FILTER_TO_CALC_RULE);
        result.add(EnumerableRules.ENUMERABLE_PROJECT_TO_CALC_RULE);
        result.add(CoreRules.CALC_MERGE);
        return result;
    }
    
    private static Collection<RelOptRule> getProjectRules() {
        Collection<RelOptRule> result = new LinkedList<>();
        result.add(CoreRules.PROJECT_MERGE);
        result.add(CoreRules.PROJECT_CORRELATE_TRANSPOSE);
        result.add(CoreRules.PROJECT_SET_OP_TRANSPOSE);
        result.add(CoreRules.PROJECT_JOIN_TRANSPOSE);
        result.add(CoreRules.PROJECT_REDUCE_EXPRESSIONS);
        result.add(ProjectRemoveRule.Config.DEFAULT.toRule());
        result.add(TranslatableProjectRule.INSTANCE);
        return result;
    }
    
    private static Collection<RelOptRule> getFilterRules() {
        Collection<RelOptRule> result = new LinkedList<>();
        result.add(CoreRules.FILTER_INTO_JOIN);
        result.add(CoreRules.JOIN_CONDITION_PUSH);
        result.add(CoreRules.SORT_JOIN_TRANSPOSE);
        result.add(CoreRules.FILTER_AGGREGATE_TRANSPOSE);
        result.add(CoreRules.FILTER_PROJECT_TRANSPOSE);
        result.add(CoreRules.FILTER_SET_OP_TRANSPOSE);
        result.add(CoreRules.FILTER_REDUCE_EXPRESSIONS);
        result.add(CoreRules.FILTER_MERGE);
        result.add(CoreRules.JOIN_PUSH_EXPRESSIONS);
        result.add(CoreRules.JOIN_PUSH_TRANSITIVE_PREDICATES);
        result.add(TranslatableFilterRule.INSTANCE);
        result.add(TranslatableProjectFilterRule.INSTANCE);
        return result;
    }
    
    private static Collection<RelOptRule> getAggregationRules() {
        Collection<RelOptRule> result = new LinkedList<>();
        result.add(CoreRules.AGGREGATE_MERGE);
        result.add(CoreRules.AGGREGATE_REDUCE_FUNCTIONS);
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
     * Create sql validator.
     *
     * @param catalogReader catalog reader
     * @param relDataTypeFactory rel data type factory
     * @param databaseType database type
     * @param connectionConfig connection config
     * @return sql validator
     */
    public static SqlValidator createSqlValidator(final CalciteCatalogReader catalogReader, final RelDataTypeFactory relDataTypeFactory,
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
    public static SqlToRelConverter createSqlToRelConverter(final CalciteCatalogReader catalogReader, final SqlValidator validator, final RelOptCluster cluster,
                                                            final SQLParserRule sqlParserRule, final DatabaseType databaseType, final boolean needsViewExpand) {
        ViewExpander expander = needsViewExpand ? new ShardingSphereViewExpander(sqlParserRule, databaseType,
                createSqlToRelConverter(catalogReader, validator, cluster, sqlParserRule, databaseType, false)) : (rowType, queryString, schemaPath, viewPath) -> null;
        Config converterConfig = SqlToRelConverter.config().withTrimUnusedFields(true);
        return new SqlToRelConverter(expander, validator, catalogReader, cluster, StandardConvertletTable.INSTANCE, converterConfig);
    }
    
    /**
     * Create rel opt cluster.
     * 
     * @param relDataTypeFactory rel data type factory
     * @return rel opt cluster
     */
    public static RelOptCluster createRelOptCluster(final RelDataTypeFactory relDataTypeFactory) {
        return RelOptCluster.create(SQLFederationPlannerUtil.createVolcanoPlanner(), new RexBuilder(relDataTypeFactory));
    }
}
