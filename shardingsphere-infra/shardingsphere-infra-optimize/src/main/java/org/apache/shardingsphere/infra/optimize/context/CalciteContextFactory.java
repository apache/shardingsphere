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

package org.apache.shardingsphere.infra.optimize.context;

import org.apache.calcite.adapter.enumerable.EnumerableRules;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.Lex;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.rel.rules.CoreRules;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.optimize.schema.CalciteSchemaFactory;

import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Calcite context.
 *
 */
public final class CalciteContextFactory {
    
    private final CalciteConnectionConfig config;
    
    private final RelDataTypeFactory typeFactory;
    
    private final CalciteSchemaFactory factory;
    
    private final RelOptCluster cluster;
    
    public CalciteContextFactory(final Map<String, ShardingSphereMetaData> metaDataMap) throws SQLException {
        config = new CalciteConnectionConfigImpl(createProperties());
        typeFactory = new JavaTypeFactoryImpl();
        factory = new CalciteSchemaFactory(metaDataMap);
        cluster = newCluster();
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("lex", Lex.MYSQL.name());
        result.setProperty("conformance", SqlConformanceEnum.MYSQL_5.name());
        return result;
    }
    
    private RelOptCluster newCluster() {
        RelOptPlanner planner = new VolcanoPlanner();
        addPlanRules(planner);
        planner.addRelTraitDef(ConventionTraitDef.INSTANCE);
        return RelOptCluster.create(planner, new RexBuilder(typeFactory));
    }
    
    private void addPlanRules(final RelOptPlanner planner) {
        planner.addRule(CoreRules.PROJECT_TO_CALC);
        planner.addRule(CoreRules.FILTER_TO_CALC);
        planner.addRule(EnumerableRules.ENUMERABLE_LIMIT_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_JOIN_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_SORT_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_TABLE_SCAN_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_CALC_RULE);
    }
    
    /**
     * Create.
     *
     * @param schema schema
     * @return calcite context
     */
    public CalciteContext create(final String schema) {
        return new CalciteContext(config, typeFactory, cluster, factory.create(schema));
    }
}
