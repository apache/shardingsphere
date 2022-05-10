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

package org.apache.shardingsphere.singletable.route.engine;

import org.apache.shardingsphere.infra.metadata.schema.QualifiedTable;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerAlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerDropSchemaStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

public final class SingleTableRouteEngineFactoryTest {
    
    private Collection<QualifiedTable> emptySingleTableNames;
    
    private Collection<QualifiedTable> nonEmptySingleTableNames;
    
    @Before
    public void setup() {
        emptySingleTableNames = Collections.emptyList();
        nonEmptySingleTableNames = Collections.singleton(new QualifiedTable("demo_ds", "t_order"));
    }
    
    @Test
    public void assertNewInstanceWithCreateSchemaStatementForPostgreSQL() {
        Optional<SingleTableRouteEngine> actual = SingleTableRouteEngineFactory.newInstance(emptySingleTableNames, new PostgreSQLCreateSchemaStatement());
        assertTrue(actual.isPresent());
        actual = SingleTableRouteEngineFactory.newInstance(nonEmptySingleTableNames, new PostgreSQLCreateSchemaStatement());
        assertTrue(actual.isPresent());
    }
    
    @Test
    public void assertNewInstanceWithAlterSchemaStatementForPostgreSQL() {
        Optional<SingleTableRouteEngine> actual = SingleTableRouteEngineFactory.newInstance(emptySingleTableNames, new PostgreSQLAlterSchemaStatement());
        assertTrue(actual.isPresent());
        actual = SingleTableRouteEngineFactory.newInstance(nonEmptySingleTableNames, new PostgreSQLAlterSchemaStatement());
        assertTrue(actual.isPresent());
    }
    
    @Test
    public void assertNewInstanceWithDropSchemaStatementForPostgreSQL() {
        Optional<SingleTableRouteEngine> actual = SingleTableRouteEngineFactory.newInstance(emptySingleTableNames, new PostgreSQLDropSchemaStatement());
        assertTrue(actual.isPresent());
        actual = SingleTableRouteEngineFactory.newInstance(nonEmptySingleTableNames, new PostgreSQLDropSchemaStatement());
        assertTrue(actual.isPresent());
    }
    
    @Test
    public void assertNewInstanceWithCreateSchemaStatementForSQLServer() {
        Optional<SingleTableRouteEngine> actual = SingleTableRouteEngineFactory.newInstance(emptySingleTableNames, new SQLServerCreateSchemaStatement());
        assertTrue(actual.isPresent());
        actual = SingleTableRouteEngineFactory.newInstance(nonEmptySingleTableNames, new SQLServerCreateSchemaStatement());
        assertTrue(actual.isPresent());
    }
    
    @Test
    public void assertNewInstanceWithAlterSchemaStatementForSQLServer() {
        Optional<SingleTableRouteEngine> actual = SingleTableRouteEngineFactory.newInstance(emptySingleTableNames, new SQLServerAlterSchemaStatement());
        assertTrue(actual.isPresent());
        actual = SingleTableRouteEngineFactory.newInstance(nonEmptySingleTableNames, new SQLServerAlterSchemaStatement());
        assertTrue(actual.isPresent());
    }
    
    @Test
    public void assertNewInstanceWithDropSchemaStatementForSQLServer() {
        Optional<SingleTableRouteEngine> actual = SingleTableRouteEngineFactory.newInstance(emptySingleTableNames, new SQLServerDropSchemaStatement());
        assertTrue(actual.isPresent());
        actual = SingleTableRouteEngineFactory.newInstance(nonEmptySingleTableNames, new SQLServerDropSchemaStatement());
        assertTrue(actual.isPresent());
    }
    
    @Test
    public void assertNewInstanceWithCreateSchemaStatementForOpenGauss() {
        Optional<SingleTableRouteEngine> actual = SingleTableRouteEngineFactory.newInstance(emptySingleTableNames, new OpenGaussCreateSchemaStatement());
        assertTrue(actual.isPresent());
        actual = SingleTableRouteEngineFactory.newInstance(nonEmptySingleTableNames, new OpenGaussCreateSchemaStatement());
        assertTrue(actual.isPresent());
    }
    
    @Test
    public void assertNewInstanceWithAlterSchemaStatementForOpenGauss() {
        Optional<SingleTableRouteEngine> actual = SingleTableRouteEngineFactory.newInstance(emptySingleTableNames, new OpenGaussAlterSchemaStatement());
        assertTrue(actual.isPresent());
        actual = SingleTableRouteEngineFactory.newInstance(nonEmptySingleTableNames, new OpenGaussAlterSchemaStatement());
        assertTrue(actual.isPresent());
    }
    
    @Test
    public void assertNewInstanceWithDropSchemaStatementForOpenGauss() {
        Optional<SingleTableRouteEngine> actual = SingleTableRouteEngineFactory.newInstance(emptySingleTableNames, new OpenGaussDropSchemaStatement());
        assertTrue(actual.isPresent());
        actual = SingleTableRouteEngineFactory.newInstance(nonEmptySingleTableNames, new OpenGaussDropSchemaStatement());
        assertTrue(actual.isPresent());
    }
}
