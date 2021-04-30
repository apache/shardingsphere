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

package org.apache.shardingsphere.infra.optimizer.converter;

import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.Contexts;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptCostFactory;
import org.apache.calcite.plan.RelOptCostImpl;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.RelCollationTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.sql.validate.SqlValidatorWithHints;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.sql2rel.StandardConvertletTable;
import org.apache.calcite.tools.Frameworks;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.optimizer.schema.ShardingSphereCalciteSchema;

import java.util.Properties;

/**
 * convert calcite SqlNode to calcite RelNode.
 */
public class RelNodeConverter {

    private static boolean topDownOpt = Boolean.parseBoolean(System.getProperty("calcite.planner.topdown.opt", "true"));
    
    private static final RelDataTypeFactory TYPE_FACTORY = new SqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT);
    
    private final RelOptCostFactory costFactory = RelOptCostImpl.FACTORY;

    private final SqlToRelConverter.Config converterConfig;

    private final SqlValidatorWithHints validator;
    
    private final CalciteCatalogReader catalog;
    
    private final ShardingSphereSchema shardingSphereSchema;

    public RelNodeConverter(final String schemaName, final ShardingSphereSchema shardingSphereSchema) {
        this.shardingSphereSchema = shardingSphereSchema;
        this.converterConfig = SqlToRelConverter.config()
                .withInSubQueryThreshold(Integer.MAX_VALUE)
                .withExpand(false);
        CalciteSchema rootSchema = createRootSchema(schemaName);
        this.catalog = new CalciteCatalogReader(rootSchema,
                rootSchema.path(schemaName),
                new JavaTypeFactoryImpl(),
                new CalciteConnectionConfigImpl(new Properties()));
        validator = SqlValidatorUtil.newValidator(SqlStdOperatorTable.instance(), catalog, TYPE_FACTORY, SqlValidator.Config.DEFAULT
                .withLenientOperatorLookup(false)
                .withSqlConformance(SqlConformanceEnum.DEFAULT)
                .withIdentifierExpansion(true));
    }

    /**
     * validate calcite SqlNode and convert SqlNode to RelNode.
     * @param sqlNode SqlNode to be validated and converted
     * @return relational algebra of sql
     */
    public RelNode validateAndConvert(final SqlNode sqlNode) {
        final RelOptCluster cluster = createRelOptCluster();
        final SqlToRelConverter sqlToRelConverter = new SqlToRelConverter(null, validator, catalog, cluster,
                StandardConvertletTable.INSTANCE, converterConfig);
        // validate and convert
        RelRoot root = sqlToRelConverter.convertQuery(sqlNode, true, true);
        return root.rel;
    }

    private RelOptCluster createRelOptCluster() {
        VolcanoPlanner planner = new VolcanoPlanner(costFactory, Contexts.EMPTY_CONTEXT);
        planner.setTopDownOpt(topDownOpt);
        planner.clearRelTraitDefs();
        planner.addRelTraitDef(ConventionTraitDef.INSTANCE);
        planner.addRelTraitDef(RelCollationTraitDef.INSTANCE);
        RexBuilder rexBuilder = new RexBuilder(TYPE_FACTORY);
        return RelOptCluster.create(planner, rexBuilder);
    }

    private CalciteSchema createRootSchema(final String schemaName) {
        SchemaPlus rootSchema = Frameworks.createRootSchema(true);
        rootSchema.add(schemaName, new ShardingSphereCalciteSchema(shardingSphereSchema));
        return CalciteSchema.from(rootSchema);
    }
}
