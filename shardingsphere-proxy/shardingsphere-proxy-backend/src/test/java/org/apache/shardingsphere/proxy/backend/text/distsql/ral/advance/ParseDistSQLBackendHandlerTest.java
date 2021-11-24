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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.advance;

import com.google.gson.Gson;
import org.apache.shardingsphere.distsql.parser.statement.ral.advanced.parse.ParseStatement;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.advanced.ParseDistSQLBackendHandler;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class ParseDistSQLBackendHandlerTest {
    
    @Test
    public void assertGetRowData() {
        String sql = "select * from t_order";
        ParseStatement parseStatement = new ParseStatement(sql);
        ParseDistSQLBackendHandler parseDistSQLBackendHandler = new ParseDistSQLBackendHandler(new MySQLDatabaseType(), parseStatement, mock(BackendConnection.class));
        parseDistSQLBackendHandler.execute();
        SQLStatement statement = new ShardingSphereSQLParserEngine("MySQL", new ConfigurationProperties(new Properties())).parse(sql, false);
        assertThat(new LinkedList<>(parseDistSQLBackendHandler.getRowData()).getFirst(), is(new Gson().toJson(statement)));
    }
    
    @Test(expected = SQLParsingException.class)
    public void assertExecute() {
        String sql = "wrong sql";
        ParseStatement parseStatement = new ParseStatement(sql);
        ParseDistSQLBackendHandler parseDistSQLBackendHandler = new ParseDistSQLBackendHandler(new MySQLDatabaseType(), parseStatement, mock(BackendConnection.class));
        parseDistSQLBackendHandler.execute();
    }
}
