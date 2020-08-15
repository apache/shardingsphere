package org.apache.shardingsphere.db.protocol.postgresql.packet.handshake;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PostgreSQLRandomGeneratorTest {
    
    @Test
    public void t() {
        PostgreSQLRandomGenerator generator = PostgreSQLRandomGenerator.getInstance();
        for (int i = 1; i < 13; i++) {
            assertThat(generator.generateRandomBytes(i).length, is(i));
        }
    }
}
