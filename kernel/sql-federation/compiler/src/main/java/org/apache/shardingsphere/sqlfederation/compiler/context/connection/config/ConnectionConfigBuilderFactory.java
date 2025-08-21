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

package org.apache.shardingsphere.sqlfederation.compiler.context.connection.config;

import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.fun.SqlLibrary;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;

import java.util.Properties;

/**
 * Connection config builder factory.
 */
public final class ConnectionConfigBuilderFactory {
    
    private final ConnectionConfigBuilder dialectBuilder;
    
    public ConnectionConfigBuilderFactory(final DatabaseType databaseType) {
        dialectBuilder = DatabaseTypedSPILoader.findService(ConnectionConfigBuilder.class, databaseType).orElse(null);
    }
    
    /**
     * Build.
     *
     * @return built connection config
     */
    public CalciteConnectionConfig build() {
        return null == dialectBuilder ? buildStandardConnectionConfig() : dialectBuilder.build();
    }
    
    private CalciteConnectionConfig buildStandardConnectionConfig() {
        Properties result = new Properties();
        result.setProperty(CalciteConnectionProperty.LEX.camelName(), Lex.JAVA.name());
        result.setProperty(CalciteConnectionProperty.CONFORMANCE.camelName(), SqlConformanceEnum.LENIENT.name());
        result.setProperty(CalciteConnectionProperty.FUN.camelName(), SqlLibrary.STANDARD.fun);
        result.setProperty(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), String.valueOf(Lex.JAVA.caseSensitive));
        return new CalciteConnectionConfigImpl(result);
    }
}
