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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptTable.ViewExpander;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.sql2rel.SqlToRelConverter.Config;
import org.apache.calcite.sql2rel.StandardConvertletTable;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.optimize.context.parser.OptimizerParserContext;
import org.apache.shardingsphere.infra.optimize.context.parser.dialect.OptimizerSQLDialectBuilderFactory;
import org.apache.shardingsphere.infra.optimize.metadata.FederationMetaData;
import org.apache.shardingsphere.infra.optimize.metadata.FederationSchemaMetaData;
import org.apache.shardingsphere.infra.optimize.metadata.calcite.FederationSchema;
import org.apache.shardingsphere.infra.optimize.planner.QueryOptimizePlannerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Optimizer context factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OptimizerContextFactory {
    
    /**
     * Create optimize context.
     *
     * @param metaDataMap meta data map
     * @return created optimizer context
     */
    public static OptimizerContext create(final Map<String, ShardingSphereMetaData> metaDataMap) {
        FederationMetaData metaData = new FederationMetaData(metaDataMap);
        Map<String, OptimizerParserContext> parserContexts = new HashMap<>();
        Map<String, SqlValidator> validators = new HashMap<>(metaDataMap.size(), 1);
        Map<String, SqlToRelConverter> converters = new HashMap<>(metaDataMap.size(), 1);
        for (Entry<String, FederationSchemaMetaData> entry : metaData.getSchemas().entrySet()) {
            String schemaName = entry.getKey();
            FederationSchema schema = new FederationSchema(entry.getValue());
            DatabaseType databaseType = metaDataMap.get(schemaName).getResource().getDatabaseType();
            parserContexts.put(entry.getKey(), new OptimizerParserContext(databaseType, createSQLDialectProperties(databaseType)));
            CalciteConnectionConfig connectionConfig = new CalciteConnectionConfigImpl(createConnectionProperties());
            RelDataTypeFactory relDataTypeFactory = new JavaTypeFactoryImpl();
            CalciteCatalogReader catalogReader = createCatalogReader(schemaName, schema, relDataTypeFactory, connectionConfig);
            SqlValidator validator = createValidator(catalogReader, relDataTypeFactory, connectionConfig);
            validators.put(schemaName, validator);
            converters.put(schemaName, createConverter(catalogReader, validator, relDataTypeFactory));
        }
        return new OptimizerContext(metaData, parserContexts, validators, converters);
    }
    
    private static Properties createSQLDialectProperties(final DatabaseType databaseType) {
        Properties result = new Properties();
        result.setProperty(CalciteConnectionProperty.TIME_ZONE.camelName(), "UTC");
        result.putAll(OptimizerSQLDialectBuilderFactory.build(databaseType, result));
        return result;
    }
    
    private static Properties createConnectionProperties() {
        Properties result = new Properties();
        result.setProperty(CalciteConnectionProperty.TIME_ZONE.camelName(), "UTC");
        return result;
    }
    
    private static CalciteCatalogReader createCatalogReader(final String schemaName, 
                                                            final Schema schema, final RelDataTypeFactory relDataTypeFactory, final CalciteConnectionConfig connectionConfig) {
        CalciteSchema rootSchema = CalciteSchema.createRootSchema(true);
        rootSchema.add(schemaName, schema);
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
    
    private static SqlToRelConverter createConverter(final CalciteCatalogReader catalogReader, final SqlValidator validator, final RelDataTypeFactory relDataTypeFactory) {
        ViewExpander expander = (rowType, queryString, schemaPath, viewPath) -> null;
        Config converterConfig = SqlToRelConverter.config().withTrimUnusedFields(true);
        return new SqlToRelConverter(expander, validator, catalogReader, createCluster(relDataTypeFactory), StandardConvertletTable.INSTANCE, converterConfig);
    }
    
    private static RelOptCluster createCluster(final RelDataTypeFactory relDataTypeFactory) {
        return RelOptCluster.create(QueryOptimizePlannerFactory.newInstance(), new RexBuilder(relDataTypeFactory));
    }
}
