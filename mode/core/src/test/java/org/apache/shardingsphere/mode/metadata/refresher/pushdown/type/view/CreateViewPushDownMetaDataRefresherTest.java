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

package org.apache.shardingsphere.mode.metadata.refresher.pushdown.type.view;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataManagerPersistServiceFixture;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class CreateViewPushDownMetaDataRefresherTest {
    
    private static final String LOGIC_DATA_SOURCE_NAME = "logic_ds";
    
    private static final String SCHEMA_NAME = "PUBLIC";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "H2");
    
    @Test
    void assertRefreshCreateViewUsesLoadedActualViewTable() throws SQLException {
        JdbcDataSource dataSource = createDataSource("create_view");
        executeUpdate(dataSource, "CREATE VIEW \"Foo_View\" AS SELECT 1");
        PushDownMetaDataManagerPersistServiceFixture persistService = new PushDownMetaDataManagerPersistServiceFixture();
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType,
                new ResourceMetaData(Collections.singletonMap(LOGIC_DATA_SOURCE_NAME, dataSource)), new RuleMetaData(Collections.emptyList()), Collections.emptyList());
        CreateViewStatement sqlStatement = new CreateViewStatement(databaseType);
        sqlStatement.setView(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("\"Foo_View\""))));
        new CreateViewPushDownMetaDataRefresher().refresh(persistService, database, LOGIC_DATA_SOURCE_NAME, SCHEMA_NAME,
                databaseType, sqlStatement, new ConfigurationProperties(new Properties()));
        assertThat(persistService.getAlteredTableSchemaName(), is(SCHEMA_NAME));
        assertThat(persistService.getAlteredTables().iterator().next().getName(), is("Foo_View"));
    }
    
    private JdbcDataSource createDataSource(final String databaseName) {
        JdbcDataSource result = new JdbcDataSource();
        result.setURL("jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false");
        result.setUser("sa");
        result.setPassword("");
        return result;
    }
    
    private void executeUpdate(final JdbcDataSource dataSource, final String... sqls) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            for (String each : sqls) {
                statement.execute(each);
            }
        }
    }
}
