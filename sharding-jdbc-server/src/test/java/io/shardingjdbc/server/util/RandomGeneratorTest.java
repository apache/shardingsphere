package io.shardingjdbc.server.util;

import io.shardingjdbc.server.packet.handshake.RandomGenerator;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class RandomGeneratorTest {
    
    @Test
    public void assertGenerateRandomBytes() {
        assertThat(RandomGenerator.getInstance().generateRandomBytes(8).length, is(8));
        assertThat(RandomGenerator.getInstance().generateRandomBytes(12).length, is(12));
    }
}