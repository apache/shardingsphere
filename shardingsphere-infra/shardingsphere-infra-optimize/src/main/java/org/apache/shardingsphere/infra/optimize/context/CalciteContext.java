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
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptTable.ViewExpander;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.sql2rel.SqlToRelConverter.Config;
import org.apache.calcite.sql2rel.StandardConvertletTable;

import java.util.Collections;

/**
 * Calcite context.
 *
 */
@Getter
public final class CalciteContext {
    
    private final CalciteSchema rootSchema;
    
    private final CalciteCatalogReader catalogReader;
    
    private final SqlParser.Config parserConfig;
    
    private final SqlValidator validator;
    
    private final SqlToRelConverter relConverter;
    
    public CalciteContext(final CalciteConnectionConfig config,
                          final SqlParser.Config parserConfig, final RelDataTypeFactory typeFactory, final RelOptCluster cluster, final Schema calciteSchema) {
        rootSchema = CalciteSchema.createRootSchema(true);
        rootSchema.add(((CalciteSchema) calciteSchema).name, calciteSchema);
        catalogReader = new CalciteCatalogReader(rootSchema, Collections.singletonList(config.schema()), typeFactory, config);
        this.parserConfig = parserConfig;
        validator = SqlValidatorUtil.newValidator(SqlStdOperatorTable.instance(), catalogReader, typeFactory, SqlValidator.Config.DEFAULT
                .withLenientOperatorLookup(config.lenientOperatorLookup())
                .withSqlConformance(config.conformance())
                .withDefaultNullCollation(config.defaultNullCollation())
                .withIdentifierExpansion(true));
        relConverter = createSqlToRelConverter(cluster);
    }
    
    private SqlToRelConverter createSqlToRelConverter(final RelOptCluster cluster) {
        Config config = SqlToRelConverter.config().withTrimUnusedFields(true);
        ViewExpander expander = (rowType, queryString, schemaPath, viewPath) -> null;
        return new SqlToRelConverter(expander, validator, catalogReader, cluster, StandardConvertletTable.INSTANCE, config);
    }
}
