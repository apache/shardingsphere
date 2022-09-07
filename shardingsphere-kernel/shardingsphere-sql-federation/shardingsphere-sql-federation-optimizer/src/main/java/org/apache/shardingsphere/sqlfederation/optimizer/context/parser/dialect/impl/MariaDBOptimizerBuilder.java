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

package org.apache.shardingsphere.sqlfederation.optimizer.context.parser.dialect.impl;

import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.fun.SqlLibrary;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.shardingsphere.sqlfederation.optimizer.context.parser.dialect.OptimizerSQLDialectBuilder;

import java.util.Properties;

/**
 * Optimizer properties builder for MariaDB.
 */
public final class MariaDBOptimizerBuilder implements OptimizerSQLDialectBuilder {
    
    @Override
    public Properties build() {
        Properties result = new Properties();
        result.setProperty(CalciteConnectionProperty.LEX.camelName(), Lex.MYSQL.name());
        result.setProperty(CalciteConnectionProperty.CONFORMANCE.camelName(), SqlConformanceEnum.MYSQL_5.name());
        result.setProperty(CalciteConnectionProperty.FUN.camelName(), SqlLibrary.MYSQL.fun);
        return result;
    }
    
    @Override
    public String getType() {
        return "MariaDB";
    }
}
