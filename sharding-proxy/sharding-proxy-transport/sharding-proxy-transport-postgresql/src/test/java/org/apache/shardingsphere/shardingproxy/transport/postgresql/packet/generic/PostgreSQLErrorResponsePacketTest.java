package org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.generic;

import org.apache.shardingsphere.shardingproxy.transport.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PostgreSQLErrorResponsePacketTest {

    @Mock
    private PostgreSQLPacketPayload payload;

    @Test
    public void assertWrite() {
        PostgreSQLErrorResponsePacket responsePacket = new PostgreSQLErrorResponsePacket();
        responsePacket.addField(PostgreSQLErrorResponsePacket.FIELD_TYPE_SEVERITY, "FATAL");
        responsePacket.addField(PostgreSQLErrorResponsePacket.FIELD_TYPE_CODE, "3D000");
        responsePacket.addField(PostgreSQLErrorResponsePacket.FIELD_TYPE_MESSAGE, "database \"test\" does not exist");
        responsePacket.write(payload);
        verify(payload).writeInt1(PostgreSQLErrorResponsePacket.FIELD_TYPE_SEVERITY);
        verify(payload).writeStringNul("FATAL");
        verify(payload).writeInt1(PostgreSQLErrorResponsePacket.FIELD_TYPE_CODE);
        verify(payload).writeStringNul("3D000");
        verify(payload).writeInt1(PostgreSQLErrorResponsePacket.FIELD_TYPE_MESSAGE);
        verify(payload).writeStringNul("database \"test\" does not exist");
        verify(payload).writeInt1(0);
    }
}
