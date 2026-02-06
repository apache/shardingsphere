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

import lombok.SneakyThrows;
import org.postgresql.core.Oid;
import org.postgresql.jdbc.TimestampUtils;
import org.postgresql.util.PSQLException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Date;

/**
 * DateArrayEncoder.
 */
public final class DateArrayEncoder extends AbstractArrayEncoder<Date> {
    
    public static final DateArrayEncoder INSTANCE = new DateArrayEncoder();
    
    private DateArrayEncoder() {
        super(Oid.DATE);
    }
    
    @SneakyThrows({PSQLException.class, IOException.class})
    @Override
    public void write(final Date item, final ByteArrayOutputStream bout, final Charset charset) {
        bout.write(0);
        bout.write(0);
        bout.write(0);
        bout.write(4);
        byte[] binaryDate = new byte[4];
        new TimestampUtils(false, null).toBinDate(null, binaryDate, item);
        bout.write(binaryDate);
    }
    
    @Override
    public String toString(final Date item) {
        if (item == null) {
            return "NULL";
        }
        return new TimestampUtils(false, null).toString(null, item);
    }
}
