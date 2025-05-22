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

package org.apache.shardingsphere.sqlfederation.compiler.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.parser.rule.builder.SQLParserRuleBuilder;
import org.apache.shardingsphere.sqlfederation.compiler.context.connection.config.ConnectionConfigBuilderFactory;
import org.apache.shardingsphere.sqlfederation.compiler.context.schema.CalciteSchemaBuilder;

import java.util.Collection;
import java.util.Properties;

/**
 * Compiler context factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CompilerContextFactory {
    
    /**
     * Create compiler context.
     *
     * @param databases databases
     * @return created compiler context
     */
    public static CompilerContext create(final Collection<ShardingSphereDatabase> databases) {
        // TODO consider to use sqlParserRule in global rule
        SQLParserRule sqlParserRule = new SQLParserRuleBuilder().build(new DefaultSQLParserRuleConfigurationBuilder().build(), databases, new ConfigurationProperties(new Properties()));
        CalciteConnectionConfig connectionConfig = buildConnectionConfig(databases);
        CalciteSchema calciteSchema = CalciteSchemaBuilder.build(databases);
        return new CompilerContext(sqlParserRule, calciteSchema, connectionConfig);
    }
    
    private static CalciteConnectionConfig buildConnectionConfig(final Collection<ShardingSphereDatabase> databases) {
        DatabaseType databaseType = databases.isEmpty() ? DatabaseTypeEngine.getDefaultStorageType() : databases.iterator().next().getProtocolType();
        return new ConnectionConfigBuilderFactory(databaseType).build();
    }
}
