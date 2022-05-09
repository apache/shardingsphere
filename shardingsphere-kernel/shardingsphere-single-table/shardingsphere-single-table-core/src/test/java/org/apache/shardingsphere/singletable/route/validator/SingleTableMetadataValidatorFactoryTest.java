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

package org.apache.shardingsphere.singletable.route.validator;

import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerDropSchemaStatement;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertTrue;

public final class SingleTableMetadataValidatorFactoryTest {
    
    @Test
    @SuppressWarnings("rawtypes")
    public void assertNewInstanceForPostgreSQL() {
        SQLStatement statement = new PostgreSQLDropSchemaStatement();
        Optional<SingleTableMetadataValidator> actual = SingleTableMetadataValidatorFactory.newInstance(statement);
        assertTrue(actual.isPresent());
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void assertNewInstanceForSQLServer() {
        SQLStatement statement = new SQLServerDropSchemaStatement();
        Optional<SingleTableMetadataValidator> actual = SingleTableMetadataValidatorFactory.newInstance(statement);
        assertTrue(actual.isPresent());
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void assertNewInstanceForOpenGauss() {
        SQLStatement statement = new OpenGaussDropSchemaStatement();
        Optional<SingleTableMetadataValidator> actual = SingleTableMetadataValidatorFactory.newInstance(statement);
        assertTrue(actual.isPresent());
    }
    
}
