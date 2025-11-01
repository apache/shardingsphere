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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.util.codec.encoder;

import org.postgresql.core.Oid;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

/**
 * Int8ArrayEncoder.
 */
public final class Int8ArrayEncoder extends AbstractArrayEncoder<Long> {
    
    public static final Int8ArrayEncoder INSTANCE = new Int8ArrayEncoder();
    
    private Int8ArrayEncoder() {
        super(Oid.INT8);
    }
    
    @Override
    public void write(final Long item, final ByteArrayOutputStream bout, final Charset charset) {
        bout.write(0);
        bout.write(0);
        bout.write(0);
        bout.write(8);
        bout.write((byte) (item >>> 56));
        bout.write((byte) (item >>> 48));
        bout.write((byte) (item >>> 40));
        bout.write((byte) (item >>> 32));
        bout.write((byte) (item >>> 24));
        bout.write((byte) (item >>> 16));
        bout.write((byte) (item >>> 8));
        bout.write(item.byteValue());
    }
    
    @Override
    public String toString(final Long item) {
        if (item == null) {
            return "NULL";
        }
        return "\"" + item + "\"";
    }
}
