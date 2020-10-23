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

package org.apache.shardingsphere.infra.metadata.model.physical.jdbc.handler;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import lombok.Getter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@Getter
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractDatabaseMetaDataDialectHandlerTest {
    
    protected static final String DATABASE_NAME = "demo_ds";
    
    protected static final String TABLE_NAME_PATTERN = "t_order_0";

    protected static final String DATABASE_USER_NAME = "demo_user";

    @Mock
    private Connection connection;
    
    @Mock
    private DatabaseMetaData databaseMetaData;
    
    protected QuoteCharacter getQuoteCharacter(final DatabaseType databaseType) {
        return DatabaseMetaDataDialectHandlerFactory.findHandler(databaseType).map(DatabaseMetaDataDialectHandler::getQuoteCharacter).orElse(QuoteCharacter.NONE);
    }
    
    protected String formatTableNamePattern(final DatabaseType databaseType) {
        return DatabaseMetaDataDialectHandlerFactory.findHandler(databaseType).map(handler -> handler.formatTableNamePattern(TABLE_NAME_PATTERN)).orElse(TABLE_NAME_PATTERN);
    }
    
    protected String getSchema(final DatabaseType databaseType) {
        return DatabaseMetaDataDialectHandlerFactory.findHandler(databaseType).map(handler -> handler.getSchema(connection)).orElse(getSchema(connection));
    }


    private String getSchema(final Connection connection) {
        try {
            return connection.getSchema();
        } catch (final SQLException ignored) {
            return null;
        }
    }
}
