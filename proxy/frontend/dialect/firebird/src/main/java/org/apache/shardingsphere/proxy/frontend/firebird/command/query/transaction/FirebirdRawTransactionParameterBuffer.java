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

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.Parameter;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.TransactionParameterBufferImpl;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.impl.wire.Xdrable;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Supplies a client's transaction parameter buffer to Jaybird without re-encoding it.
 */
@HighFrequencyInvocation
final class FirebirdRawTransactionParameterBuffer implements TransactionParameterBuffer {
    
    private final int type;
    
    private byte[] arguments;
    
    FirebirdRawTransactionParameterBuffer(final byte[] raw) {
        if (0 == raw.length) {
            throw new IllegalArgumentException("An empty TPB requires separate handling");
        }
        type = raw[0] & 0xFF;
        arguments = Arrays.copyOfRange(raw, 1, raw.length);
    }
    
    @Override
    public int getType() {
        return type;
    }
    
    @Override
    public TransactionParameterBuffer deepCopy() {
        return new FirebirdRawTransactionParameterBuffer(toBytesWithType());
    }
    
    @Override
    public void addArgument(final int argumentType) {
        append(buffer -> buffer.addArgument(argumentType));
    }
    
    @Override
    public void addArgument(final int argumentType, final String value) {
        append(buffer -> buffer.addArgument(argumentType, value));
    }
    
    @Override
    public void addArgument(final int argumentType, final String value, final Encoding encoding) {
        append(buffer -> buffer.addArgument(argumentType, value, encoding));
    }
    
    @Override
    public void addArgument(final int argumentType, final byte value) {
        append(buffer -> buffer.addArgument(argumentType, value));
    }
    
    @Override
    public void addArgument(final int argumentType, final int value) {
        append(buffer -> buffer.addArgument(argumentType, value));
    }
    
    @Override
    public void addArgument(final int argumentType, final long value) {
        append(buffer -> buffer.addArgument(argumentType, value));
    }
    
    @Override
    public void addArgument(final int argumentType, final byte[] content) {
        append(buffer -> buffer.addArgument(argumentType, content));
    }
    
    private void append(final Consumer<TransactionParameterBuffer> action) {
        TransactionParameterBuffer addition = new TransactionParameterBufferImpl();
        action.accept(addition);
        byte[] added = addition.toBytes();
        int oldLength = arguments.length;
        arguments = Arrays.copyOf(arguments, oldLength + added.length);
        System.arraycopy(added, 0, arguments, oldLength, added.length);
    }
    
    @Override
    public void removeArgument(final int argumentType) {
        for (Field each : fields()) {
            if (argumentType == each.type) {
                byte[] result = new byte[arguments.length - (each.end - each.start)];
                System.arraycopy(arguments, 0, result, 0, each.start);
                System.arraycopy(arguments, each.end, result, each.start, arguments.length - each.end);
                arguments = result;
                return;
            }
        }
    }
    
    @Override
    public @Nullable String getArgumentAsString(final int argumentType) {
        for (Field each : fields()) {
            if (argumentType == each.type) {
                if (!each.sized) {
                    throw new UnsupportedOperationException("The TPB argument has no value");
                }
                return new String(arguments, each.start + 2, each.end - each.start - 2, Charset.defaultCharset());
            }
        }
        return null;
    }
    
    @Override
    public int getArgumentAsInt(final int argumentType) {
        for (Field each : fields()) {
            if (argumentType == each.type) {
                if (!each.sized || each.end - each.start > 6) {
                    throw new UnsupportedOperationException("The TPB argument is not an integer");
                }
                int result = 0;
                for (int i = each.start + 2; i < each.end; i++) {
                    result |= (arguments[i] & 0xFF) << ((i - each.start - 2) * Byte.SIZE);
                }
                return result;
            }
        }
        return 0;
    }
    
    @Override
    public boolean hasArgument(final int argumentType) {
        for (Field each : fields()) {
            if (argumentType == each.type) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Iterator<Parameter> iterator() {
        TransactionParameterBuffer copy = new TransactionParameterBufferImpl();
        for (Field each : fields()) {
            if (each.sized) {
                copy.addArgument(each.type, Arrays.copyOfRange(arguments, each.start + 2, each.end));
            } else {
                copy.addArgument(each.type);
            }
        }
        return copy.iterator();
    }
    
    @Override
    public void writeArgumentsTo(final OutputStream outputStream) throws IOException {
        outputStream.write(arguments);
    }
    
    @Override
    public Xdrable toXdrable() {
        byte[] copy = arguments.clone();
        return new Xdrable() {
            
            @Override
            public int getLength() {
                return copy.length;
            }
            
            @Override
            public void read(final XdrInputStream inputStream, final int length) {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public void write(final XdrOutputStream outputStream) throws IOException {
                outputStream.write(copy);
            }
        };
    }
    
    @Override
    public byte[] toBytes() {
        return arguments.clone();
    }
    
    @Override
    public byte[] toBytesWithType() {
        byte[] result = new byte[arguments.length + 1];
        result[0] = (byte) type;
        System.arraycopy(arguments, 0, result, 1, arguments.length);
        return result;
    }
    
    @Override
    public int size() {
        return fields().size();
    }
    
    private List<Field> fields() {
        List<Field> result = new ArrayList<>();
        for (int offset = 0; offset < arguments.length;) {
            int start = offset;
            int tag = arguments[offset++] & 0xFF;
            boolean sized = 10 == tag || 11 == tag || 12 == tag || 13 == tag || 21 == tag || 23 == tag || 25 == tag;
            if (sized) {
                if (offset == arguments.length) {
                    throw new IllegalStateException("Incomplete TPB argument");
                }
                offset += 1 + (arguments[offset] & 0xFF);
                if (offset > arguments.length) {
                    throw new IllegalStateException("Incomplete TPB argument");
                }
            }
            result.add(new Field(tag, start, offset, sized));
        }
        return result;
    }
    
    @HighFrequencyInvocation
    private static final class Field {
        
        private final int type;
        
        private final int start;
        
        private final int end;
        
        private final boolean sized;
        
        private Field(final int type, final int start, final int end, final boolean sized) {
            this.type = type;
            this.start = start;
            this.end = end;
            this.sized = sized;
        }
    }
}
