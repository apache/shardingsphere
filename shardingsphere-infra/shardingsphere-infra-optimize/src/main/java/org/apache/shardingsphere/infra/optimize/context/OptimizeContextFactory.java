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

import lombok.Getter;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.config.Lex;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.sql.fun.SqlLibrary;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParser.Config;
import org.apache.calcite.sql.parser.impl.SqlParserImpl;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.sql2rel.StandardConvertletTable;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MariaDBDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OracleDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.SQL92DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.SQLServerDatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.optimize.core.metadata.FederateSchemaMetaDatas;
import org.apache.shardingsphere.infra.optimize.core.plan.PlannerInitializer;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * Optimize context factory.
 */
public final class OptimizeContextFactory {
    
    private static final String LEX_CAMEL_NAME = CalciteConnectionProperty.LEX.camelName();
    
    private static final String CONFORMANCE_CAMEL_NAME = CalciteConnectionProperty.CONFORMANCE.camelName();
    
    private static final String FUN_CAMEL_NAME = CalciteConnectionProperty.FUN.camelName();
    
    private static final String TIME_ZONE = CalciteConnectionProperty.TIME_ZONE.camelName();
    
    @Getter
    private final DatabaseType databaseType;
    
    @Getter
    private final Properties props = new Properties();
    
    private final CalciteConnectionConfig connectionConfig;
    
    /**
     * Remove calcite's parser.
     * @deprecated Use ShardingSphere parser instead.
     */
    @Deprecated
    private final Config parserConfig;
    
    private final RelDataTypeFactory typeFactory;
    
    @Getter
    private final FederateSchemaMetaDatas schemaMetaDatas;
    
    private final RelOptCluster cluster;
    
    public OptimizeContextFactory(final Map<String, ShardingSphereMetaData> metaDataMap) {
        this.databaseType = metaDataMap.isEmpty() ? null : metaDataMap.values().iterator().next().getResource().getDatabaseType();
        initProperties(databaseType);
        typeFactory = new JavaTypeFactoryImpl();
        cluster = newCluster();
        schemaMetaDatas = new FederateSchemaMetaDatas(metaDataMap);
        connectionConfig = new CalciteConnectionConfigImpl(props);
        parserConfig = SqlParser.config()
                .withLex(connectionConfig.lex())
                .withIdentifierMaxLength(SqlParser.DEFAULT_IDENTIFIER_MAX_LENGTH)
                .withConformance(connectionConfig.conformance())
                .withParserFactory(SqlParserImpl.FACTORY);
    }
    
    private void initProperties(final DatabaseType databaseType) {
        // TODO Logic could be improved.
        props.setProperty(TIME_ZONE, "UTC");
        if (databaseType instanceof MySQLDatabaseType || null == databaseType) {
            props.setProperty(LEX_CAMEL_NAME, Lex.MYSQL.name());
            props.setProperty(CONFORMANCE_CAMEL_NAME, SqlConformanceEnum.MYSQL_5.name());
            props.setProperty(FUN_CAMEL_NAME, SqlLibrary.MYSQL.fun);
            return;
        }
        if (databaseType instanceof H2DatabaseType) {
            // TODO No suitable type of Lex
            props.setProperty(LEX_CAMEL_NAME, Lex.MYSQL.name());
            props.setProperty(CONFORMANCE_CAMEL_NAME, SqlConformanceEnum.LENIENT.name());
            props.setProperty(FUN_CAMEL_NAME, SqlLibrary.STANDARD.fun);
            return;
        }
        if (databaseType instanceof MariaDBDatabaseType) {
            props.setProperty(LEX_CAMEL_NAME, Lex.MYSQL.name());
            props.setProperty(CONFORMANCE_CAMEL_NAME, SqlConformanceEnum.MYSQL_5.name());
            props.setProperty(FUN_CAMEL_NAME, SqlLibrary.MYSQL.fun);
            return;
        }
        if (databaseType instanceof OracleDatabaseType) {
            props.setProperty(LEX_CAMEL_NAME, Lex.ORACLE.name());
            props.setProperty(CONFORMANCE_CAMEL_NAME, SqlConformanceEnum.ORACLE_12.name());
            props.setProperty(FUN_CAMEL_NAME, SqlLibrary.ORACLE.fun);
            return;
        }
        if (databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType) {
            // TODO No suitable type of Lex and conformance
            props.setProperty(LEX_CAMEL_NAME, Lex.JAVA.name());
            props.setProperty(CONFORMANCE_CAMEL_NAME, SqlConformanceEnum.BABEL.name());
            props.setProperty(FUN_CAMEL_NAME, SqlLibrary.POSTGRESQL.fun);
            return;
        }
        if (databaseType instanceof SQL92DatabaseType) {
            // TODO No suitable type of Lex
            props.setProperty(LEX_CAMEL_NAME, Lex.MYSQL.name());
            props.setProperty(CONFORMANCE_CAMEL_NAME, SqlConformanceEnum.STRICT_92.name());
            props.setProperty(FUN_CAMEL_NAME, SqlLibrary.STANDARD.fun);
            return;
        }
        if (databaseType instanceof SQLServerDatabaseType) {
            props.setProperty(LEX_CAMEL_NAME, Lex.SQL_SERVER.name());
            props.setProperty(CONFORMANCE_CAMEL_NAME, SqlConformanceEnum.SQL_SERVER_2008.name());
            props.setProperty(FUN_CAMEL_NAME, SqlLibrary.STANDARD.fun);
            return;
        }
        throw new ShardingSphereException("No matching DatabaseType found");
    }
    
    private RelOptCluster newCluster() {
        RelOptPlanner planner = new VolcanoPlanner();
        PlannerInitializer.init(planner);
        return RelOptCluster.create(planner, new RexBuilder(typeFactory));
    }
    
    /**
     * Create.
     *
     * @param schemaName schema name
     * @param logicSchema logic schema
     * @return optimize context
     */
    public OptimizeContext create(final String schemaName, final Schema logicSchema) {
        CalciteCatalogReader catalogReader = createCalciteCatalogReader(schemaName, connectionConfig, typeFactory, logicSchema);
        SqlValidator validator = createSqlValidator(connectionConfig, typeFactory, catalogReader);
        SqlToRelConverter relConverter = createSqlToRelConverter(cluster, validator, catalogReader);
        return new OptimizeContext(databaseType, props, schemaName, logicSchema, parserConfig, validator, relConverter);
    }
    
    private CalciteCatalogReader createCalciteCatalogReader(final String schemaName, final CalciteConnectionConfig config,
                                                            final RelDataTypeFactory typeFactory, final Schema logicSchema) {
        CalciteSchema rootSchema = CalciteSchema.createRootSchema(true);
        rootSchema.add(schemaName, logicSchema);
        return new CalciteCatalogReader(rootSchema, Collections.singletonList(schemaName), typeFactory, config);
    }
    
    private SqlValidator createSqlValidator(final CalciteConnectionConfig config, final RelDataTypeFactory typeFactory, final CalciteCatalogReader catalogReader) {
        return SqlValidatorUtil.newValidator(SqlStdOperatorTable.instance(), catalogReader, typeFactory, SqlValidator.Config.DEFAULT
                .withLenientOperatorLookup(config.lenientOperatorLookup())
                .withSqlConformance(config.conformance())
                .withDefaultNullCollation(config.defaultNullCollation())
                .withIdentifierExpansion(true));
    }
    
    private SqlToRelConverter createSqlToRelConverter(final RelOptCluster cluster, final SqlValidator validator, final CalciteCatalogReader catalogReader) {
        SqlToRelConverter.Config config = SqlToRelConverter.config().withTrimUnusedFields(true);
        RelOptTable.ViewExpander expander = (rowType, queryString, schemaPath, viewPath) -> null;
        return new SqlToRelConverter(expander, validator, catalogReader, cluster, StandardConvertletTable.INSTANCE, config);
    }
}
