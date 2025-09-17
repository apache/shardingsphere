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
import org.postgresql.jdbc.TimestampUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.sql.Time;
import java.util.TimeZone;

/**
 * TimeArrayEncoder.
 */
public class TimeArrayEncoder extends AbstractArrayEncoder<Time> {
    
    public static final TimeArrayEncoder INSTANCE = new TimeArrayEncoder();
    
    public TimeArrayEncoder() {
        super(Oid.TIME);
    }
    
    @Override
    public void write(final Time item, final ByteArrayOutputStream bout, final Charset charset) {
        bout.write(0);
        bout.write(0);
        bout.write(0);
        bout.write(8);
        long time = item.getTime() * 1000 + TimeZone.getDefault().getRawOffset() * 1000L;
        bout.write((byte) (time >>> 56));
        bout.write((byte) (time >>> 48));
        bout.write((byte) (time >>> 40));
        bout.write((byte) (time >>> 32));
        bout.write((byte) (time >>> 24));
        bout.write((byte) (time >>> 16));
        bout.write((byte) (time >>> 8));
        bout.write((byte) time);
    }
    
    @Override
    public String toString(final Time item) {
        if (item == null) {
            return "NULL";
        }
        // todo in setArray it use Timestamp.toString but in setObject it use TimestampUtils what is right?
        return new TimestampUtils(false, null).toString(null, item);
    }
}
