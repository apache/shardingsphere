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
import org.apache.calcite.rel.type.RelDataTypeFamily;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.rex.RexUtil;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOperatorTable;
import org.apache.calcite.sql.fun.SqlBetweenOperator;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.type.SqlTypeFamily;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.sql.type.SqlTypeUtil;
import org.apache.calcite.sql.util.SqlOperatorTables;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.sql2rel.SqlRexContext;
import org.apache.calcite.sql2rel.SqlRexConvertlet;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.sql2rel.SqlToRelConverter.Config;
import org.apache.calcite.sql2rel.StandardConvertletTable;
import org.apache.calcite.util.Util;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sqlfederation.compiler.context.CompilerContext;
import org.apache.shardingsphere.sqlfederation.compiler.metadata.catalog.SQLFederationCatalogReader;
import org.apache.shardingsphere.sqlfederation.compiler.metadata.view.ShardingSphereViewExpander;
import org.apache.shardingsphere.sqlfederation.compiler.planner.builder.SQLFederationPlannerBuilder;
import org.apache.shardingsphere.sqlfederation.compiler.sql.type.SQLFederationDataTypeFactory;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
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
        return new SqlToRelConverter(expander, validator, catalogReader, cluster, SQLFederationRelConverter::getConvertlet, converterConfig);
    }
    
    private static @Nullable SqlRexConvertlet getConvertlet(final SqlCall call) {
        if (call.getKind().belongsTo(SqlKind.ORDER_COMPARISON)) {
            return SQLFederationRelConverter::convertOrderComparison;
        }
        return call.getOperator() instanceof SqlBetweenOperator ? SQLFederationRelConverter::convertBetween : StandardConvertletTable.INSTANCE.get(call);
    }
    
    private static RexNode convertOrderComparison(final SqlRexContext context, final SqlCall call) {
        RexBuilder rexBuilder = context.getRexBuilder();
        List<RexNode> operands = convertOperands(context, call);
        return rexBuilder.makeCall(call.getParserPosition(), rexBuilder.deriveReturnType(call.getOperator(), operands), call.getOperator(), operands);
    }
    
    private static RexNode convertBetween(final SqlRexContext context, final SqlCall call) {
        List<RexNode> operands = convertOperands(context, call);
        RexNode value = operands.get(SqlBetweenOperator.VALUE_OPERAND);
        RexNode lower = operands.get(SqlBetweenOperator.LOWER_OPERAND);
        RexNode upper = operands.get(SqlBetweenOperator.UPPER_OPERAND);
        RexBuilder rexBuilder = context.getRexBuilder();
        RexNode firstRange = rexBuilder.makeCall(call.getParserPosition(), SqlStdOperatorTable.AND,
                rexBuilder.makeCall(call.getParserPosition(), SqlStdOperatorTable.GREATER_THAN_OR_EQUAL, value, lower),
                rexBuilder.makeCall(call.getParserPosition(), SqlStdOperatorTable.LESS_THAN_OR_EQUAL, value, upper));
        SqlBetweenOperator operator = (SqlBetweenOperator) call.getOperator();
        RexNode result;
        switch (operator.flag) {
            case ASYMMETRIC:
                result = firstRange;
                break;
            case SYMMETRIC:
                RexNode secondRange = rexBuilder.makeCall(call.getParserPosition(), SqlStdOperatorTable.AND,
                        rexBuilder.makeCall(call.getParserPosition(), SqlStdOperatorTable.GREATER_THAN_OR_EQUAL, value, upper),
                        rexBuilder.makeCall(call.getParserPosition(), SqlStdOperatorTable.LESS_THAN_OR_EQUAL, value, lower));
                result = rexBuilder.makeCall(call.getParserPosition(), SqlStdOperatorTable.OR, firstRange, secondRange);
                break;
            default:
                throw Util.unexpected(operator.flag);
        }
        return operator.isNegated() ? rexBuilder.makeCall(call.getParserPosition(), SqlStdOperatorTable.NOT, result) : result;
    }
    
    private static List<RexNode> convertOperands(final SqlRexContext context, final SqlCall call) {
        RexBuilder rexBuilder = context.getRexBuilder();
        List<RexNode> result = new ArrayList<>(call.operandCount());
        for (SqlNode each : call.getOperandList()) {
            result.add(context.convertExpression(each));
        }
        List<RelDataType> operandTypes = context.getValidator().getValidatedOperandTypes(call);
        if (null != operandTypes) {
            for (int i = 0; i < result.size(); i++) {
                result.set(i, rexBuilder.ensureType(call.getParserPosition(), operandTypes.get(i), result.get(i), true));
            }
        }
        RelDataType consistentType = getConsistentType(context, RexUtil.types(result));
        if (null != consistentType) {
            result.replaceAll(each -> rexBuilder.ensureType(call.getParserPosition(), consistentType, each, true));
        }
        return result;
    }
    
    private static @Nullable RelDataType getConsistentType(final SqlRexContext context, final List<RelDataType> types) {
        if (SqlTypeUtil.areSameFamily(types)) {
            return null;
        }
        List<RelDataType> nonCharacterTypes = new ArrayList<>(types.size());
        for (RelDataType each : types) {
            if (SqlTypeFamily.CHARACTER != each.getFamily()) {
                nonCharacterTypes.add(each);
            }
        }
        if (nonCharacterTypes.size() < types.size()) {
            RelDataTypeFamily family = nonCharacterTypes.get(0).getFamily();
            if (SqlTypeFamily.INTEGER == family || SqlTypeFamily.NUMERIC == family) {
                nonCharacterTypes.add(context.getTypeFactory().createSqlType(SqlTypeName.BIGINT));
            }
        }
        return context.getTypeFactory().leastRestrictive(nonCharacterTypes);
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
