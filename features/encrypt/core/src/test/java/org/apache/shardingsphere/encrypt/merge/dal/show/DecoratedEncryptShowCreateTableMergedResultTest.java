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

import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.binder.statement.dal.ShowCreateTableStatementContext;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DecoratedEncryptShowCreateTableMergedResultTest {
    
    @Mock
    private MergedResult mergedResult;
    
    @Test
    public void assertNextWhenNextExist() throws SQLException {
        assertFalse(createDecoratedEncryptShowCreateTableMergedResult(mergedResult, mock(EncryptRule.class)).next());
    }
    
    @Test
    public void assertNextWhenNextNotExist() throws SQLException {
        when(mergedResult.next()).thenReturn(true);
        assertTrue(createDecoratedEncryptShowCreateTableMergedResult(mergedResult, mock(EncryptRule.class)).next());
    }
    
    @Test
    public void assertGetValueWhenConfigPlainColumn() throws SQLException {
        when(mergedResult.next()).thenReturn(true).thenReturn(false);
        when(mergedResult.getValue(2, String.class)).thenReturn(
                "CREATE TABLE `t_encrypt` (`id` INT NOT NULL, `user_id_cipher` VARCHAR(100) NOT NULL, "
                        + "`user_id` VARCHAR(100) NOT NULL, `order_id` VARCHAR(30) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
        DecoratedEncryptShowCreateTableMergedResult actual = createDecoratedEncryptShowCreateTableMergedResult(mergedResult,
                mockEncryptRule(Collections.singletonList(new EncryptColumnRuleConfiguration("user_id", "user_id_cipher", null, "", "user_id", null, false))));
        assertTrue(actual.next());
        assertThat(actual.getValue(2, String.class),
                is("CREATE TABLE `t_encrypt` (`id` INT NOT NULL, `user_id` VARCHAR(100) NOT NULL, `order_id` VARCHAR(30) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"));
    }
    
    @Test
    public void assertGetValueWhenConfigAssistedQueryColumn() throws SQLException {
        when(mergedResult.next()).thenReturn(true).thenReturn(false);
        when(mergedResult.getValue(2, String.class)).thenReturn(
                "CREATE TABLE `t_encrypt` (`id` INT NOT NULL, `user_id_cipher` VARCHAR(100) NOT NULL, "
                        + "`user_id_assisted` VARCHAR(100) NOT NULL, `order_id` VARCHAR(30) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
        DecoratedEncryptShowCreateTableMergedResult actual = createDecoratedEncryptShowCreateTableMergedResult(mergedResult,
                mockEncryptRule(Collections.singletonList(new EncryptColumnRuleConfiguration("user_id", "user_id_cipher", "user_id_assisted", "", null, null, false))));
        assertTrue(actual.next());
        assertThat(actual.getValue(2, String.class),
                is("CREATE TABLE `t_encrypt` (`id` INT NOT NULL, `user_id` VARCHAR(100) NOT NULL, `order_id` VARCHAR(30) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"));
    }
    
    @Test
    public void assertGetValueWhenConfigLikeQueryColumn() throws SQLException {
        when(mergedResult.next()).thenReturn(true).thenReturn(false);
        when(mergedResult.getValue(2, String.class)).thenReturn(
                "CREATE TABLE `t_encrypt` (`id` INT NOT NULL, `user_id_cipher` VARCHAR(100) NOT NULL, "
                        + "`user_id_like` VARCHAR(100) NOT NULL, `order_id` VARCHAR(30) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
        DecoratedEncryptShowCreateTableMergedResult actual = createDecoratedEncryptShowCreateTableMergedResult(mergedResult,
                mockEncryptRule(Collections.singletonList(new EncryptColumnRuleConfiguration("user_id", "user_id_cipher", "", "user_id_like", null, null, null, null, false))));
        assertTrue(actual.next());
        assertThat(actual.getValue(2, String.class),
                is("CREATE TABLE `t_encrypt` (`id` INT NOT NULL, `user_id` VARCHAR(100) NOT NULL, `order_id` VARCHAR(30) NOT NULL,"
                        + " PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"));
    }
    
    @Test
    public void assertGetValueWhenConfigPlainColumnAndAssistedQueryColumn() throws SQLException {
        when(mergedResult.next()).thenReturn(true).thenReturn(false);
        when(mergedResult.getValue(2, String.class)).thenReturn(
                "CREATE TABLE `t_encrypt` (`id` INT NOT NULL, `user_id_cipher` VARCHAR(100) NOT NULL, `user_id` VARCHAR(100) NOT NULL, "
                        + "`user_id_assisted` VARCHAR(100) NOT NULL, `order_id` VARCHAR(30) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
        DecoratedEncryptShowCreateTableMergedResult actual = createDecoratedEncryptShowCreateTableMergedResult(mergedResult,
                mockEncryptRule(Collections.singletonList(new EncryptColumnRuleConfiguration("user_id", "user_id_cipher", "user_id_assisted", "", "user_id", null, null, null, false))));
        assertTrue(actual.next());
        assertThat(actual.getValue(2, String.class),
                is("CREATE TABLE `t_encrypt` (`id` INT NOT NULL, `user_id` VARCHAR(100) NOT NULL, `order_id` VARCHAR(30) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"));
    }
    
    @Test
    public void assertGetValueWhenConfigPlainColumnAndLikeQueryColumn() throws SQLException {
        when(mergedResult.next()).thenReturn(true).thenReturn(false);
        when(mergedResult.getValue(2, String.class)).thenReturn(
                "CREATE TABLE `t_encrypt` (`id` INT NOT NULL, `user_id_cipher` VARCHAR(100) NOT NULL, `user_id` VARCHAR(100) NOT NULL, "
                        + "`user_id_like` VARCHAR(100) NOT NULL, `order_id` VARCHAR(30) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
        DecoratedEncryptShowCreateTableMergedResult actual = createDecoratedEncryptShowCreateTableMergedResult(mergedResult,
                mockEncryptRule(Collections.singletonList(new EncryptColumnRuleConfiguration("user_id", "user_id_cipher", "", "user_id_like", "user_id", null, null, null, false))));
        assertTrue(actual.next());
        assertThat(actual.getValue(2, String.class),
                is("CREATE TABLE `t_encrypt` (`id` INT NOT NULL, `user_id` VARCHAR(100) NOT NULL, `order_id` VARCHAR(30) NOT NULL,"
                        + " PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"));
    }
    
    private EncryptRule mockEncryptRule(final Collection<EncryptColumnRuleConfiguration> encryptColumnRuleConfigurations) {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable = new EncryptTable(new EncryptTableRuleConfiguration("t_encrypt", encryptColumnRuleConfigurations, false));
        when(result.findEncryptTable("t_encrypt")).thenReturn(Optional.of(encryptTable));
        return result;
    }
    
    @Test
    public void assertGetValueWhenConfigMultiColumn() throws SQLException {
        when(mergedResult.next()).thenReturn(true).thenReturn(false);
        when(mergedResult.getValue(2, String.class)).thenReturn(
                "CREATE TABLE `t_encrypt` (`id` INT NOT NULL, `user_id_cipher` VARCHAR(100) NOT NULL, `user_id` VARCHAR(100) NOT NULL, `user_id_like` VARCHAR(100) NOT NULL, "
                        + "`order_id_cipher` VARCHAR(30) NOT NULL, `order_id_like` VARCHAR(30) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
        Collection<EncryptColumnRuleConfiguration> columns = new LinkedList<>();
        columns.add(new EncryptColumnRuleConfiguration("user_id", "user_id_cipher", "", "user_id_like", "user_id", null, null, null, false));
        columns.add(new EncryptColumnRuleConfiguration("order_id", "order_id_cipher", "", "order_id_like", "user_id", null, null, null, false));
        DecoratedEncryptShowCreateTableMergedResult actual = createDecoratedEncryptShowCreateTableMergedResult(mergedResult, mockEncryptRule(columns));
        assertTrue(actual.next());
        assertThat(actual.getValue(2, String.class),
                is("CREATE TABLE `t_encrypt` (`id` INT NOT NULL, `user_id` VARCHAR(100) NOT NULL, `order_id` VARCHAR(30) NOT NULL,"
                        + " PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"));
    }
    
    @Test
    public void assertWasNull() throws SQLException {
        assertFalse(createDecoratedEncryptShowCreateTableMergedResult(mergedResult, mock(EncryptRule.class)).wasNull());
    }
    
    private DecoratedEncryptShowCreateTableMergedResult createDecoratedEncryptShowCreateTableMergedResult(final MergedResult mergedResult, final EncryptRule encryptRule) {
        ShowCreateTableStatementContext sqlStatementContext = mock(ShowCreateTableStatementContext.class);
        IdentifierValue identifierValue = new IdentifierValue("t_encrypt");
        TableNameSegment tableNameSegment = new TableNameSegment(1, 4, identifierValue);
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(tableNameSegment);
        when(sqlStatementContext.getAllTables()).thenReturn(Collections.singletonList(simpleTableSegment));
        return new DecoratedEncryptShowCreateTableMergedResult(mergedResult, sqlStatementContext, encryptRule);
    }
}
