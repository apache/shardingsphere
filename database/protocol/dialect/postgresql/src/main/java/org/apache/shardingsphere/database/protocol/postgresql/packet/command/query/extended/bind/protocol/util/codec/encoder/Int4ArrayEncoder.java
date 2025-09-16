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
 * Int4ArrayEncoder.
 */
public final class Int4ArrayEncoder extends AbstractArrayEncoder<Integer> {
    
    public static final Int4ArrayEncoder INSTANCE = new Int4ArrayEncoder();
    
    private Int4ArrayEncoder() {
        super(Oid.INT4);
    }
    
    @Override
    public void write(final Integer item, final ByteArrayOutputStream bout, final Charset charset) {
        bout.write(0);
        bout.write(0);
        bout.write(0);
        bout.write(4);
        bout.write((byte) (item >>> 24));
        bout.write((byte) (item >>> 16));
        bout.write((byte) (item >>> 8));
        bout.write(item.byteValue());
    }
    
    @Override
    public String toString(final Integer item) {
        if (item == null) {
            return "NULL";
        }
        return "\"" + item + "\"";
    }
}
