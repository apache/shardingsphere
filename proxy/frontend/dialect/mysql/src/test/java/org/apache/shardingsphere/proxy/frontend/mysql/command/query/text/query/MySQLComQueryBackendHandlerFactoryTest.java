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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.text.query;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.admin.MySQLComSetOptionPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.text.query.MySQLComQueryPacket;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.sqltranslator.rule.builder.DefaultSQLTranslatorRuleConfigurationBuilder;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.ConstructionMockSettings;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyContext.class, ProxyBackendHandlerFactory.class})
@ConstructionMockSettings(MySQLMultiStatementsProxyBackendHandler.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MySQLComQueryBackendHandlerFactoryTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Mock
    private ProxyBackendHandler proxyBackendHandler;
    
    @Mock
    private MySQLComQueryPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setUp() throws SQLException {
        when(packet.getSQL()).thenReturn("");
        when(packet.findOriginalSQLBytes()).thenReturn(Optional.empty());
        when(packet.getHintValueContext()).thenReturn(new HintValueContext());
        when(ProxyBackendHandlerFactory.newInstance(eq(databaseType), anyString(), any(SQLStatement.class), eq(connectionSession), any())).thenReturn(proxyBackendHandler);
        when(ProxyBackendHandlerFactory.newInstance(eq(databaseType), any(QueryContext.class), eq(connectionSession), anyBoolean())).thenReturn(proxyBackendHandler);
        mockProxyContext();
    }
    
    @Test
    void assertCreateStandardSQLHandler() throws SQLException {
        when(packet.getSQL()).thenReturn("SELECT 1");
        ProxyBackendHandler actual = MySQLComQueryBackendHandlerFactory.newInstance(packet, connectionSession);
        assertThat(actual, is(proxyBackendHandler));
    }
    
    @Test
    void assertCreateMultiStatementsHandlerForUpdate() throws SQLException {
        enableMultiStatements();
        when(packet.getSQL()).thenReturn("UPDATE t SET v=v+1 WHERE id=1;UPDATE t SET v=v+1 WHERE id=2");
        ProxyBackendHandler actual = MySQLComQueryBackendHandlerFactory.newInstance(packet, connectionSession);
        assertThat(actual, isA(MySQLMultiStatementsProxyBackendHandler.class));
    }
    
    @Test
    void assertCreateMultiStatementsHandlerForInsertOnDuplicateKey() throws SQLException {
        enableMultiStatements();
        when(packet.getSQL()).thenReturn("INSERT INTO t (id, v) VALUES(1,1) ON DUPLICATE KEY UPDATE v=2;INSERT INTO t (id, v) VALUES(2,1) ON DUPLICATE KEY UPDATE v=3");
        ProxyBackendHandler actual = MySQLComQueryBackendHandlerFactory.newInstance(packet, connectionSession);
        assertThat(actual, isA(MySQLMultiStatementsProxyBackendHandler.class));
    }
    
    private void enableMultiStatements() {
        when(connectionSession.getAttributeMap().hasAttr(MySQLConstants.OPTION_MULTI_STATEMENTS_ATTRIBUTE_KEY)).thenReturn(true);
        when(connectionSession.getAttributeMap().attr(MySQLConstants.OPTION_MULTI_STATEMENTS_ATTRIBUTE_KEY).get()).thenReturn(MySQLComSetOptionPacket.MYSQL_OPTION_MULTI_STATEMENTS_ON);
    }
    
    @Test
    void assertCreatePreparedHandlerForBinaryLiteral() throws SQLException {
        when(packet.getSQL()).thenReturn("INSERT INTO t (id, v) VALUES (1, _binary'�')");
        when(packet.findOriginalSQLBytes()).thenReturn(Optional.of(sql(
                "/* SHARDINGSPHERE_HINT: WRITE_ROUTE_ONLY=true */ INSERT INTO t (id, v) VALUES (1, _binary'", (byte) 0xFF, "')")));
        when(connectionSession.getAttributeMap().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get()).thenReturn(StandardCharsets.UTF_8);
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
        when(connectionSession.getConnectionContext().getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        ArgumentCaptor<QueryContext> queryContextCaptor = ArgumentCaptor.forClass(QueryContext.class);
        when(ProxyBackendHandlerFactory.newInstance(eq(databaseType), queryContextCaptor.capture(), eq(connectionSession), eq(true))).thenReturn(proxyBackendHandler);
        MySQLComQueryBackendHandlerFactory.newInstance(packet, connectionSession);
        QueryContext actual = queryContextCaptor.getValue();
        assertThat(actual.getSql(), is("INSERT INTO t (id, v) VALUES (1, ?)"));
        assertThat(actual.getParameters().size(), is(1));
        Blob blob = (Blob) actual.getParameters().get(0);
        assertArrayEquals(new byte[]{(byte) 0xFF}, blob.getBytes(1, (int) blob.length()));
    }
    
    @Test
    void assertCreateMultiStatementsHandlerForMalformedBinaryLiteral() throws SQLException {
        enableMultiStatements();
        when(packet.getSQL()).thenReturn("UPDATE t SET v = _binary'�' WHERE id = 1; UPDATE t SET v = _binary'bar' WHERE id = 2");
        when(packet.findOriginalSQLBytes()).thenReturn(Optional.of(sql(
                "UPDATE t SET v = _binary'", (byte) 0xFF, "' WHERE id = 1; UPDATE t SET v = _binary'bar' WHERE id = 2")));
        when(connectionSession.getAttributeMap().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get()).thenReturn(StandardCharsets.UTF_8);
        ProxyBackendHandler actual = MySQLComQueryBackendHandlerFactory.newInstance(packet, connectionSession);
        assertThat(actual, isA(MySQLMultiStatementsProxyBackendHandler.class));
    }
    
    @Test
    void assertCreateStandardHandlerForStructuralBinaryLiteral() throws SQLException {
        when(packet.getSQL()).thenReturn("SHOW TABLES LIKE _binary'�'");
        when(packet.findOriginalSQLBytes()).thenReturn(Optional.of(sql("SHOW TABLES LIKE _binary'", (byte) 0xFF, "'")));
        when(connectionSession.getAttributeMap().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get()).thenReturn(StandardCharsets.UTF_8);
        ProxyBackendHandler actual = MySQLComQueryBackendHandlerFactory.newInstance(packet, connectionSession);
        assertThat(actual, is(proxyBackendHandler));
    }
    
    private static byte[] sql(final String prefix, final byte content, final String suffix) {
        byte[] prefixBytes = prefix.getBytes(StandardCharsets.UTF_8);
        byte[] suffixBytes = suffix.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[prefixBytes.length + 1 + suffixBytes.length];
        System.arraycopy(prefixBytes, 0, result, 0, prefixBytes.length);
        result[prefixBytes.length] = content;
        System.arraycopy(suffixBytes, 0, result, prefixBytes.length + 1, suffixBytes.length);
        return result;
    }
    
    private void mockProxyContext() {
        ContextManager contextManager = mock(ContextManager.class);
        MetaDataContexts metaDataContexts = mockMetaDataContexts();
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
    }
    
    private MetaDataContexts mockMetaDataContexts() {
        MetaDataContexts result = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = createDatabase();
        when(result.getMetaData().getDatabase("foo_db")).thenReturn(database);
        when(result.getMetaData().getDatabase(new IdentifierValue("foo_db"))).thenReturn(database);
        RuleMetaData globalRuleMetaData = new RuleMetaData(
                Arrays.asList(new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build()), new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build())));
        when(result.getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE.getKey(), "1");
        when(result.getMetaData().getProps()).thenReturn(new ConfigurationProperties(props));
        when(result.getMetaData().containsDatabase("foo_db")).thenReturn(true);
        when(result.getMetaData().containsDatabase(new IdentifierValue("foo_db"))).thenReturn(true);
        return result;
    }
    
    private ShardingSphereDatabase createDatabase() {
        ShardingSphereTable table = new ShardingSphereTable("t", Arrays.asList(new ShardingSphereColumn("id", Types.BIGINT, true, false, false, false, true, false),
                new ShardingSphereColumn("v", Types.INTEGER, false, false, false, false, true, false)), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_db", databaseType, Collections.singleton(table), Collections.emptyList());
        return new ShardingSphereDatabase("foo_db", databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.singleton(schema),
                new ConfigurationProperties(new Properties()));
    }
}
