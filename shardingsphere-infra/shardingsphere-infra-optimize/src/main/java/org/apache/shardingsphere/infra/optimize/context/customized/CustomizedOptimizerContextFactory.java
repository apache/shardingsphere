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

package org.apache.shardingsphere.infra.optimize.context.customized;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelOptTable.ViewExpander;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParser.Config;
import org.apache.calcite.sql.parser.impl.SqlParserImpl;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.sql2rel.StandardConvertletTable;
import org.apache.shardingsphere.infra.optimize.context.original.OriginalOptimizerContext;
import org.apache.shardingsphere.infra.optimize.core.plan.PlannerInitializer;

import java.util.Collections;

/**
 * Customized optimizer context factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CustomizedOptimizerContextFactory {
    
    /**
     * Create customized optimize context.
     *
     * @param schemaName schema name
     * @param logicSchema logic schema
     * @param originalOptimizerContext original optimizer context
     * @return created customized optimize context
     */
    public static CustomizedOptimizerContext create(final String schemaName, final Schema logicSchema, final OriginalOptimizerContext originalOptimizerContext) {
        CalciteConnectionConfig connectionConfig = new CalciteConnectionConfigImpl(originalOptimizerContext.getProps());
        // TODO Remove calcite's parser, Use ShardingSphere parser instead.
        Config parserConfig = SqlParser.config()
                .withLex(connectionConfig.lex())
                .withIdentifierMaxLength(SqlParser.DEFAULT_IDENTIFIER_MAX_LENGTH)
                .withConformance(connectionConfig.conformance())
                .withParserFactory(SqlParserImpl.FACTORY);
        RelDataTypeFactory relDataTypeFactory = new JavaTypeFactoryImpl();
        CalciteCatalogReader catalogReader = createCatalogReader(schemaName, logicSchema, relDataTypeFactory, connectionConfig);
        SqlValidator validator = createValidator(catalogReader, relDataTypeFactory, connectionConfig);
        SqlToRelConverter relConverter = createRelConverter(catalogReader, validator, relDataTypeFactory);
        return new CustomizedOptimizerContext(originalOptimizerContext, schemaName, logicSchema, parserConfig, validator, relConverter);
    }
    
    private static CalciteCatalogReader createCatalogReader(final String schemaName, 
                                                            final Schema logicSchema, final RelDataTypeFactory relDataTypeFactory, final CalciteConnectionConfig connectionConfig) {
        CalciteSchema rootSchema = CalciteSchema.createRootSchema(true);
        rootSchema.add(schemaName, logicSchema);
        return new CalciteCatalogReader(rootSchema, Collections.singletonList(schemaName), relDataTypeFactory, connectionConfig);
    }
    
    private static SqlValidator createValidator(final CalciteCatalogReader catalogReader, final RelDataTypeFactory relDataTypeFactory, final CalciteConnectionConfig connectionConfig) {
        SqlValidator.Config validatorConfig = SqlValidator.Config.DEFAULT
                .withLenientOperatorLookup(connectionConfig.lenientOperatorLookup())
                .withSqlConformance(connectionConfig.conformance())
                .withDefaultNullCollation(connectionConfig.defaultNullCollation())
                .withIdentifierExpansion(true);
        return SqlValidatorUtil.newValidator(SqlStdOperatorTable.instance(), catalogReader, relDataTypeFactory, validatorConfig);
    }
    
    private static SqlToRelConverter createRelConverter(final CalciteCatalogReader catalogReader, final SqlValidator validator, final RelDataTypeFactory relDataTypeFactory) {
        ViewExpander expander = (rowType, queryString, schemaPath, viewPath) -> null;
        SqlToRelConverter.Config relConverterConfig = SqlToRelConverter.config().withTrimUnusedFields(true);
        return new SqlToRelConverter(expander, validator, catalogReader, createCluster(relDataTypeFactory), StandardConvertletTable.INSTANCE, relConverterConfig);
    }
    
    private static RelOptCluster createCluster(final RelDataTypeFactory relDataTypeFactory) {
        RelOptPlanner planner = new VolcanoPlanner();
        PlannerInitializer.init(planner);
        return RelOptCluster.create(planner, new RexBuilder(relDataTypeFactory));
    }
}
