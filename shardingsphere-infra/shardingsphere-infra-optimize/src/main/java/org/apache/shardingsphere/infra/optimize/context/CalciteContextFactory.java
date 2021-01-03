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

import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.Lex;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParser.Config;
import org.apache.calcite.sql.parser.impl.SqlParserImpl;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.optimize.execute.CalciteInternalExecutor;
import org.apache.shardingsphere.infra.optimize.plan.PlannerInitializer;
import org.apache.shardingsphere.infra.optimize.schema.CalciteLogicSchemaFactory;

import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Calcite context.
 *
 */
public final class CalciteContextFactory {
    
    private final CalciteConnectionConfig connectionConfig;
    
    private final Config parserConfig;
    
    private final RelDataTypeFactory typeFactory;
    
    private final CalciteLogicSchemaFactory factory;
    
    private final RelOptCluster cluster;
    
    public CalciteContextFactory(final Map<String, ShardingSphereMetaData> metaDataMap) {
        connectionConfig = new CalciteConnectionConfigImpl(createProperties());
        parserConfig = SqlParser.config()
                .withLex(connectionConfig.lex())
                .withIdentifierMaxLength(SqlParser.DEFAULT_IDENTIFIER_MAX_LENGTH)
                .withConformance(connectionConfig.conformance())
                .withParserFactory(SqlParserImpl.FACTORY);
        typeFactory = new JavaTypeFactoryImpl();
        factory = new CalciteLogicSchemaFactory(metaDataMap);
        cluster = newCluster();
    }
    
    private Properties createProperties() {
        // TODO Not only MySQL here.
        Properties result = new Properties();
        result.setProperty("lex", Lex.MYSQL.name());
        result.setProperty("conformance", SqlConformanceEnum.MYSQL_5.name());
        return result;
    }
    
    private RelOptCluster newCluster() {
        RelOptPlanner planner = new VolcanoPlanner();
        PlannerInitializer.init(planner);
        return RelOptCluster.create(planner, new RexBuilder(typeFactory));
    }
    
    /**
     * Create.
     *
     * @param schema schema
     * @param executor executor
     * @return calcite context
     */
    public CalciteContext create(final String schema, final CalciteInternalExecutor executor) {
        try {
            return new CalciteContext(connectionConfig, parserConfig, typeFactory, cluster, factory.create(schema, executor));
        } catch (final SQLException ex) {
            throw new ShardingSphereException(ex);
        }
    }
}
