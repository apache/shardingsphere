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
 * Float8ArrayEncoder.
 */
public class Float8ArrayEncoder extends AbstractArrayEncoder<Double> {
    
    public static final Float8ArrayEncoder INSTANCE = new Float8ArrayEncoder();
    
    public Float8ArrayEncoder() {
        super(Oid.FLOAT8);
    }
    
    @Override
    public void write(final Double item, final ByteArrayOutputStream bout, final Charset charset) {
        long l = Double.doubleToRawLongBits(item);
        bout.write(0);
        bout.write(0);
        bout.write(0);
        bout.write(8);
        bout.write((byte) (l >>> 56));
        bout.write((byte) (l >>> 48));
        bout.write((byte) (l >>> 40));
        bout.write((byte) (l >>> 32));
        bout.write((byte) (l >>> 24));
        bout.write((byte) (l >>> 16));
        bout.write((byte) (l >>> 8));
        bout.write((byte) l);
    }
    
    @Override
    public String toString(final Double item) {
        if (item == null) {
            return "NULL";
        }
        return "\"" + item + "\"";
    }
}
