/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy.transport.mysql.packet.command.query.binary.prepare;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.ShardingConstant;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.parsing.SQLParsingEngine;
import io.shardingsphere.core.parsing.parser.context.selectitem.CommonSelectItem;
import io.shardingsphere.core.parsing.parser.context.table.Table;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowTablesStatement;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.proxy.config.ProxyContext;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.frontend.common.FrontendHandler;
import io.shardingsphere.proxy.transport.common.packet.DatabasePacket;
import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.query.ColumnDefinition41Packet;
import io.shardingsphere.proxy.transport.mysql.packet.command.query.binary.fixture.BinaryStatementRegistryUtil;
import io.shardingsphere.proxy.transport.mysql.packet.generic.EofPacket;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ComStmtPreparePacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private FrontendHandler frontendHandler;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        setProxyContextRuleRegistryMap();
        setFrontendHandlerSchema();
    }
    
    @Before
    @After
    public void reset() throws ReflectiveOperationException {
        BinaryStatementRegistryUtil.reset();
    }
    
    private void setProxyContextRuleRegistryMap() throws ReflectiveOperationException {
        RuleRegistry ruleRegistry = mock(RuleRegistry.class);
        ShardingMetaData metaData = mock(ShardingMetaData.class);
        when(ruleRegistry.getMetaData()).thenReturn(metaData);
        Map<String, RuleRegistry> ruleRegistryMap = new HashMap<>();
        ruleRegistryMap.put(ShardingConstant.LOGIC_SCHEMA_NAME, ruleRegistry);
        Field field = ProxyContext.class.getDeclaredField("ruleRegistryMap");
        field.setAccessible(true);
        field.set(ProxyContext.getInstance(), ruleRegistryMap);
    }
    
    private void setFrontendHandlerSchema() {
        when(frontendHandler.getCurrentSchema()).thenReturn(ShardingConstant.LOGIC_SCHEMA_NAME);
    }
    
    @Test
    public void assertWrite() {
        when(payload.readStringEOF()).thenReturn("SELECT id FROM tbl WHERE id=?");
        ComStmtPreparePacket actual = new ComStmtPreparePacket(1, payload, frontendHandler);
        assertThat(actual.getSequenceId(), is(1));
        actual.write(payload);
        verify(payload).writeStringEOF("SELECT id FROM tbl WHERE id=?");
    }
    
    @Test
    public void assertExecuteForQueryWithParameters() throws ReflectiveOperationException {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setParametersIndex(1);
        selectStatement.getTables().add(new Table("tbl", Optional.<String>absent()));
        selectStatement.getItems().addAll(Collections.singletonList(new CommonSelectItem("id", Optional.<String>absent())));
        Optional<CommandResponsePackets> actual = getComStmtPreparePacketWithMockedSQLParsingEngine("SELECT id FROM tbl WHERE id=?", selectStatement).execute();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getPackets().size(), is(3));
        Iterator<DatabasePacket> packets = actual.get().getPackets().iterator();
        ComStmtPrepareOKPacket comStmtPrepareOKPacket = (ComStmtPrepareOKPacket) packets.next();
        assertThat(comStmtPrepareOKPacket.getSequenceId(), is(1));
        ColumnDefinition41Packet columnDefinition41Packet = (ColumnDefinition41Packet) packets.next();
        assertThat(columnDefinition41Packet.getSequenceId(), is(2));
        assertThat(columnDefinition41Packet.getName(), is(""));
        assertThat(columnDefinition41Packet.getColumnType(), is(ColumnType.MYSQL_TYPE_VARCHAR));
        EofPacket eofPacket = (EofPacket) packets.next();
        assertThat(eofPacket.getSequenceId(), is(3));
    }
    
    @Test
    public void assertExecuteForQueryWithoutParameters() throws ReflectiveOperationException {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getTables().add(new Table("tbl", Optional.<String>absent()));
        selectStatement.getItems().addAll(Collections.singletonList(new CommonSelectItem("1", Optional.<String>absent())));
        Optional<CommandResponsePackets> actual = getComStmtPreparePacketWithMockedSQLParsingEngine("SELECT 1", selectStatement).execute();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getPackets().size(), is(1));
        assertThat(actual.get().getHeadPacket(), CoreMatchers.<DatabasePacket>instanceOf(ComStmtPrepareOKPacket.class));
        assertThat(actual.get().getHeadPacket().getSequenceId(), is(1));
    }
    
    @Test
    public void assertExecuteForInsertWithoutParameters() throws ReflectiveOperationException {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.getTables().add(new Table("tbl", Optional.<String>absent()));
        Optional<CommandResponsePackets> actual = getComStmtPreparePacketWithMockedSQLParsingEngine("INSERT INTO tbl VALUES(1)", insertStatement).execute();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getPackets().size(), is(1));
        assertThat(actual.get().getHeadPacket(), CoreMatchers.<DatabasePacket>instanceOf(ComStmtPrepareOKPacket.class));
        assertThat(actual.get().getHeadPacket().getSequenceId(), is(1));
    }
    
    @Test
    public void assertExecuteForDALWithoutParameters() throws ReflectiveOperationException {
        ShowTablesStatement showTablesStatement = new ShowTablesStatement();
        Optional<CommandResponsePackets> actual = getComStmtPreparePacketWithMockedSQLParsingEngine("SHOW TABLES", showTablesStatement).execute();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getPackets().size(), is(1));
        assertThat(actual.get().getHeadPacket(), CoreMatchers.<DatabasePacket>instanceOf(ComStmtPrepareOKPacket.class));
        assertThat(actual.get().getHeadPacket().getSequenceId(), is(1));
    }
    
    private ComStmtPreparePacket getComStmtPreparePacketWithMockedSQLParsingEngine(final String sql, final SQLStatement sqlStatement) throws ReflectiveOperationException {
        when(payload.readStringEOF()).thenReturn(sql);
        ComStmtPreparePacket result = new ComStmtPreparePacket(1, payload, frontendHandler);
        SQLParsingEngine sqlParsingEngine = mock(SQLParsingEngine.class);
        when(sqlParsingEngine.parse(true)).thenReturn(sqlStatement);
        Field field = ComStmtPreparePacket.class.getDeclaredField("sqlParsingEngine");
        field.setAccessible(true);
        field.set(result, sqlParsingEngine);
        return result;
    }
}
