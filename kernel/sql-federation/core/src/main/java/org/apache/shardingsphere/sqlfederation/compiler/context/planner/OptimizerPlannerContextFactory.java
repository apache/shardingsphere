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

package org.apache.shardingsphere.sqlfederation.compiler.context.planner;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sqlfederation.compiler.context.parser.OptimizerParserContext;
import org.apache.shardingsphere.sqlfederation.compiler.metadata.schema.SQLFederationSchema;
import org.apache.shardingsphere.sqlfederation.compiler.planner.util.SQLFederationPlannerUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Optimizer planner context factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OptimizerPlannerContextFactory {
    
    private static final JavaTypeFactory DEFAULT_DATA_TYPE_FACTORY = new JavaTypeFactoryImpl();
    
    /**
     * Create optimizer planner context map.
     *
     * @param databases databases
     * @param parserContexts parser contexts
     * @param sqlParserRule sql parser rule
     * @return created optimizer planner context map
     */
    public static Map<String, OptimizerPlannerContext> create(final Map<String, ShardingSphereDatabase> databases, final Map<String, OptimizerParserContext> parserContexts,
                                                              final SQLParserRule sqlParserRule) {
        Map<String, OptimizerPlannerContext> result = new ConcurrentHashMap<>(databases.size(), 1F);
        for (Entry<String, ShardingSphereDatabase> entry : databases.entrySet()) {
            result.put(entry.getKey(), create(entry.getValue(), parserContexts.get(entry.getKey()), sqlParserRule));
        }
        return result;
    }
    
    /**
     * Create optimizer planner context.
     *
     * @param database database
     * @param parserContext parser context
     * @param sqlParserRule sql parser rule
     * @return created optimizer planner context
     */
    public static OptimizerPlannerContext create(final ShardingSphereDatabase database, final OptimizerParserContext parserContext, final SQLParserRule sqlParserRule) {
        Map<String, SqlValidator> validators = new LinkedHashMap<>();
        Map<String, SqlToRelConverter> converters = new LinkedHashMap<>();
        for (Entry<String, ShardingSphereSchema> entry : database.getSchemas().entrySet()) {
            CalciteConnectionConfig connectionConfig = new CalciteConnectionConfigImpl(parserContext.getDialectProps());
            Schema sqlFederationSchema = new SQLFederationSchema(entry.getKey(), entry.getValue(), database.getProtocolType(), DEFAULT_DATA_TYPE_FACTORY);
            CalciteCatalogReader catalogReader = SQLFederationPlannerUtils.createCatalogReader(entry.getKey(), sqlFederationSchema, DEFAULT_DATA_TYPE_FACTORY, connectionConfig);
            SqlValidator validator = SQLFederationPlannerUtils.createSqlValidator(catalogReader, DEFAULT_DATA_TYPE_FACTORY, parserContext.getDatabaseType(), connectionConfig);
            SqlToRelConverter converter = SQLFederationPlannerUtils.createSqlToRelConverter(catalogReader, validator, SQLFederationPlannerUtils.createRelOptCluster(DEFAULT_DATA_TYPE_FACTORY),
                    sqlParserRule, parserContext.getDatabaseType(), true);
            validators.put(entry.getKey(), validator);
            converters.put(entry.getKey(), converter);
        }
        return new OptimizerPlannerContext(validators, converters);
    }
}
