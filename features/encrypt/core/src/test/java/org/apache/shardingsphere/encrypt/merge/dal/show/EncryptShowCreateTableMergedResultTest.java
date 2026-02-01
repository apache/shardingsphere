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

package org.apache.shardingsphere.encrypt.merge.dal.show;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.exception.syntax.UnsupportedEncryptSQLException;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.table.MySQLShowCreateTableStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EncryptShowCreateTableMergedResultTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Mock
    private MergedResult mergedResult;
    
    @Test
    void assertNewInstanceWithNotTableAvailableStatement() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        assertThrows(UnsupportedEncryptSQLException.class, () -> new EncryptShowCreateTableMergedResult(mock(RuleMetaData.class), mergedResult, sqlStatementContext, mock(EncryptRule.class)));
    }
    
    @Test
    void assertNewInstanceWithEmptyTable() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getSimpleTables()).thenReturn(Collections.emptyList());
        assertThrows(UnsupportedEncryptSQLException.class, () -> new EncryptShowCreateTableMergedResult(mock(RuleMetaData.class), mergedResult, sqlStatementContext, mock(EncryptRule.class)));
    }
    
    @Test
    void assertNext() throws SQLException {
        when(mergedResult.next()).thenReturn(true);
        assertTrue(createMergedResult(mergedResult, "foo_tbl", mock(EncryptRule.class)).next());
    }
    
    @Test
    void assertGetValueWithOtherColumnIndex() throws SQLException {
        when(mergedResult.next()).thenReturn(true);
        when(mergedResult.getValue(1, String.class)).thenReturn("foo_value");
        EncryptShowCreateTableMergedResult actual = createMergedResult(mergedResult, "foo_tbl", mockEncryptRule(Collections.emptyList()));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, String.class), is("foo_value"));
    }
    
    @Test
    void assertGetValueWithFloatDataTypeAndComment() throws SQLException {
        when(mergedResult.next()).thenReturn(true);
        String actualSQL = "CREATE TABLE `foo_tbl` (`id` INT NOT NULL, `user_id_cipher` FLOAT(10, 2) NOT NULL "
                + "COMMENT ',123\\&/\\`\"abc', `order_id` VARCHAR(30) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        when(mergedResult.getValue(2, String.class)).thenReturn(actualSQL);
        EncryptColumnRuleConfiguration columnRuleConfig = new EncryptColumnRuleConfiguration("user_id", new EncryptColumnItemRuleConfiguration("user_id_cipher", "foo_encryptor"));
        EncryptShowCreateTableMergedResult actual = createMergedResult(mergedResult, "foo_tbl", mockEncryptRule(Collections.singleton(columnRuleConfig)));
        assertTrue(actual.next());
        String expectedSQL = "CREATE TABLE `foo_tbl` (`id` INT NOT NULL, `user_id` FLOAT(10, 2) NOT NULL "
                + "COMMENT ',123\\&/\\`\"abc', `order_id` VARCHAR(30) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        assertThat(actual.getValue(2, String.class), is(expectedSQL));
    }
    
    @Test
    void assertGetValueWithAssistedQueryColumn() throws SQLException {
        when(mergedResult.next()).thenReturn(true);
        String actualSQL = "CREATE TABLE `foo_tbl` (`id` INT NOT NULL, `user_id_cipher` VARCHAR(100) NOT NULL, "
                + "`user_id_assisted` VARCHAR(100) NOT NULL, `order_id` VARCHAR(30) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        when(mergedResult.getValue(2, String.class)).thenReturn(actualSQL);
        EncryptColumnRuleConfiguration columnRuleConfig = new EncryptColumnRuleConfiguration("user_id", new EncryptColumnItemRuleConfiguration("user_id_cipher", "foo_encryptor"));
        columnRuleConfig.setAssistedQuery(new EncryptColumnItemRuleConfiguration("user_id_assisted", "foo_assist_query_encryptor"));
        EncryptShowCreateTableMergedResult actual = createMergedResult(mergedResult, "foo_tbl", mockEncryptRule(Collections.singleton(columnRuleConfig)));
        assertTrue(actual.next());
        String expectedSQL = "CREATE TABLE `foo_tbl` (`id` INT NOT NULL, `user_id` VARCHAR(100) NOT NULL, `order_id` VARCHAR(30) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        assertThat(actual.getValue(2, String.class), is(expectedSQL));
    }
    
    @Test
    void assertGetValueWithLikeQueryColumn() throws SQLException {
        when(mergedResult.next()).thenReturn(true);
        String actualSQL = "CREATE TABLE `foo_tbl` (`id` INT NOT NULL, `user_id_cipher` VARCHAR(100) NOT NULL, "
                + "`user_id_like` VARCHAR(100) NOT NULL, `order_id` VARCHAR(30) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        when(mergedResult.getValue(2, String.class)).thenReturn(actualSQL);
        EncryptColumnRuleConfiguration columnRuleConfig = new EncryptColumnRuleConfiguration("user_id", new EncryptColumnItemRuleConfiguration("user_id_cipher", "foo_encryptor"));
        columnRuleConfig.setLikeQuery(new EncryptColumnItemRuleConfiguration("user_id_like", "foo_like_encryptor"));
        EncryptShowCreateTableMergedResult actual = createMergedResult(mergedResult, "foo_tbl", mockEncryptRule(Collections.singleton(columnRuleConfig)));
        assertTrue(actual.next());
        String expectedSQL = "CREATE TABLE `foo_tbl` (`id` INT NOT NULL, `user_id` VARCHAR(100) NOT NULL, `order_id` VARCHAR(30) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        assertThat(actual.getValue(2, String.class), is(expectedSQL));
    }
    
    @Test
    void assertGetValueWithMultiColumns() throws SQLException {
        when(mergedResult.next()).thenReturn(true);
        String actualSQL = "CREATE TABLE `foo_tbl` (`id` INT NOT NULL, `user_id_cipher` VARCHAR(100) NOT NULL, `user_id_like` VARCHAR(100) NOT NULL, "
                + "`order_id_cipher` VARCHAR(30) NOT NULL, `order_id_like` VARCHAR(30) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        when(mergedResult.getValue(2, String.class)).thenReturn(actualSQL);
        EncryptShowCreateTableMergedResult actual = createMergedResult(mergedResult, "foo_tbl", mockEncryptRule(getEncryptColumnRuleConfigurations()));
        assertTrue(actual.next());
        String expectedSQL = "CREATE TABLE `foo_tbl` (`id` INT NOT NULL, `user_id` VARCHAR(100) NOT NULL, `order_id` VARCHAR(30) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        assertThat(actual.getValue(2, String.class), is(expectedSQL));
    }
    
    private Collection<EncryptColumnRuleConfiguration> getEncryptColumnRuleConfigurations() {
        Collection<EncryptColumnRuleConfiguration> result = new LinkedList<>();
        EncryptColumnRuleConfiguration userIdColumnConfig = new EncryptColumnRuleConfiguration("user_id", new EncryptColumnItemRuleConfiguration("user_id_cipher", "foo_encryptor"));
        userIdColumnConfig.setLikeQuery(new EncryptColumnItemRuleConfiguration("user_id_like", "foo_like_encryptor"));
        result.add(userIdColumnConfig);
        EncryptColumnRuleConfiguration orderIdColumnConfig = new EncryptColumnRuleConfiguration("order_id", new EncryptColumnItemRuleConfiguration("order_id_cipher", "foo_encryptor"));
        orderIdColumnConfig.setLikeQuery(new EncryptColumnItemRuleConfiguration("order_id_like", "foo_like_encryptor"));
        result.add(orderIdColumnConfig);
        return result;
    }
    
    @Test
    void assertGetValueWithoutEncryptTable() throws SQLException {
        when(mergedResult.next()).thenReturn(true);
        String actualSQL = "CREATE TABLE `bar_tbl` (`id` INT NOT NULL, `user_id_cipher` FLOAT(10, 2) NOT NULL "
                + "COMMENT ',123\\&/\\`\"abc', `order_id` VARCHAR(30) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        when(mergedResult.getValue(2, String.class)).thenReturn(actualSQL);
        EncryptColumnRuleConfiguration columnRuleConfig = new EncryptColumnRuleConfiguration("user_id", new EncryptColumnItemRuleConfiguration("user_id_cipher", "foo_encryptor"));
        EncryptShowCreateTableMergedResult actual = createMergedResult(mergedResult, "bar_tbl", mockEncryptRule(Collections.singleton(columnRuleConfig)));
        assertTrue(actual.next());
        String expectedSQL = "CREATE TABLE `bar_tbl` (`id` INT NOT NULL, `user_id_cipher` FLOAT(10, 2) NOT NULL "
                + "COMMENT ',123\\&/\\`\"abc', `order_id` VARCHAR(30) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        assertThat(actual.getValue(2, String.class), is(expectedSQL));
    }
    
    @Test
    void assertGetValueWithInvalidEncryptTable() throws SQLException {
        when(mergedResult.next()).thenReturn(true);
        when(mergedResult.getValue(2, String.class)).thenReturn("foo_tbl");
        EncryptColumnRuleConfiguration columnRuleConfig = new EncryptColumnRuleConfiguration("user_id", new EncryptColumnItemRuleConfiguration("user_id_cipher", "foo_encryptor"));
        EncryptShowCreateTableMergedResult actual = createMergedResult(mergedResult, "foo_tbl", mockEncryptRule(Collections.singleton(columnRuleConfig)));
        assertTrue(actual.next());
        assertThat(actual.getValue(2, String.class), is("foo_tbl"));
    }
    
    private EncryptShowCreateTableMergedResult createMergedResult(final MergedResult mergedResult, final String tableName, final EncryptRule rule) {
        MySQLShowCreateTableStatement sqlStatement = new MySQLShowCreateTableStatement(databaseType, new SimpleTableSegment(new TableNameSegment(1, 4, new IdentifierValue(tableName))));
        sqlStatement.buildAttributes();
        CommonSQLStatementContext sqlStatementContext = new CommonSQLStatementContext(sqlStatement);
        RuleMetaData globalRuleMetaData = mock(RuleMetaData.class);
        when(globalRuleMetaData.getSingleRule(SQLParserRule.class)).thenReturn(new SQLParserRule(new SQLParserRuleConfiguration(new CacheOption(128, 1024L), new CacheOption(2000, 65535L))));
        return new EncryptShowCreateTableMergedResult(globalRuleMetaData, mergedResult, sqlStatementContext, rule);
    }
    
    private EncryptRule mockEncryptRule(final Collection<EncryptColumnRuleConfiguration> encryptColumnRuleConfigs) {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable = new EncryptTable(new EncryptTableRuleConfiguration("foo_tbl", encryptColumnRuleConfigs), Collections.emptyMap());
        when(result.findEncryptTable("foo_tbl")).thenReturn(Optional.of(encryptTable));
        return result;
    }
}
