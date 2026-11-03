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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.Parameter;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.TransactionParameterBufferImpl;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebirdRawTransactionParameterBufferTest {
    
    @ParameterizedTest
    @MethodSource("rawBuffers")
    void assertWireBytesAreUnchanged(final byte[] raw) throws IOException {
        FirebirdRawTransactionParameterBuffer actual = new FirebirdRawTransactionParameterBuffer(raw);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        XdrOutputStream xdr = new XdrOutputStream(stream);
        xdr.writeTyped(actual);
        xdr.flush();
        byte[] wire = stream.toByteArray();
        assertThat(ByteBuffer.wrap(wire).getInt(), is(raw.length));
        assertThat(Arrays.copyOfRange(wire, 4, 4 + raw.length), is(raw));
        assertThat(actual.toBytesWithType(), is(raw));
        assertThat(actual.deepCopy().toBytesWithType(), is(raw));
    }
    
    private static Stream<byte[]> rawBuffers() {
        return Stream.of(new byte[]{1}, new byte[]{3}, new byte[]{3, 8, 15, 17, 7}, new byte[]{3, 15, 17, 9, 6, 21, 1, 1},
                new byte[]{3, 2, 23, 8, 1, 0, 0, 0, 0, 0, 0, 0}, new byte[]{3, 21, 4, 1},
                new byte[]{(byte) 255, 9, 8, 21, 0}, new byte[]{3, 10, 3, 'T', '_', '1', 4});
    }
    
    @Test
    void assertArgumentsAndIndependentCopy() throws IOException {
        byte[] raw = {3, 10, 3, 'A', 16, 'B', 6, 16};
        FirebirdRawTransactionParameterBuffer actual = new FirebirdRawTransactionParameterBuffer(raw);
        raw[0] = 1;
        assertThat(actual.getType(), is(3));
        assertThat(actual.size(), is(3));
        assertTrue(actual.hasArgument(16));
        TransactionParameterBuffer actualCopy = actual.deepCopy();
        actual.removeArgument(16);
        assertFalse(actual.hasArgument(16));
        assertTrue(actualCopy.hasArgument(16));
        assertThat(actual.toBytesWithType(), is(new byte[]{3, 10, 3, 'A', 16, 'B', 6}));
        actual.addArgument(16);
        assertThat(actual.toBytesWithType(), is(actualCopy.toBytesWithType()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        actual.writeArgumentsTo(out);
        assertThat(out.toByteArray(), is(actual.toBytes()));
        Iterator<Parameter> iterator = actual.iterator();
        TransactionParameterBuffer roundTrip = new TransactionParameterBufferImpl();
        while (iterator.hasNext()) {
            iterator.next().copyTo(roundTrip, null);
        }
        assertThat(roundTrip.toBytes(), is(actual.toBytes()));
    }
    
    @Test
    void assertAddingAndReadingValues() {
        FirebirdRawTransactionParameterBuffer actual = new FirebirdRawTransactionParameterBuffer(new byte[]{3});
        actual.addArgument(10, "table");
        actual.addArgument(11, "table", EncodingFactory.getPlatformEncoding());
        actual.addArgument(12, (byte) 9);
        actual.addArgument(21, 42);
        actual.addArgument(23, 123456789L);
        actual.addArgument(25, new byte[]{'s'});
        assertThat(actual.getArgumentAsString(10), is("table"));
        assertThat(actual.getArgumentAsInt(21), is(42));
        assertThat(actual.getArgumentAsInt(99), is(0));
        assertThat(actual.size(), is(6));
        actual.removeArgument(99);
        assertThat(actual.size(), is(6));
    }
    
    @Test
    void assertMalformedDataCanBeForwardedButNotInterpreted() {
        FirebirdRawTransactionParameterBuffer missingLength = new FirebirdRawTransactionParameterBuffer(new byte[]{3, 21});
        assertThat(missingLength.toBytesWithType(), is(new byte[]{3, 21}));
        assertThrows(IllegalStateException.class, () -> missingLength.hasArgument(21));
        FirebirdRawTransactionParameterBuffer truncated = new FirebirdRawTransactionParameterBuffer(new byte[]{3, 21, 2, 1});
        assertThat(truncated.toBytesWithType(), is(new byte[]{3, 21, 2, 1}));
        assertThrows(IllegalStateException.class, truncated::size);
        assertThrows(IllegalArgumentException.class, () -> new FirebirdRawTransactionParameterBuffer(new byte[0]));
    }
}
