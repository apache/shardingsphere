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

public class BooleanArrayEncoder extends AbstractArrayEncoder<Boolean> {
    
    public static final BooleanArrayEncoder INSTANCE = new BooleanArrayEncoder();
    
    public BooleanArrayEncoder() {
        super(Oid.BOOL);
    }
    
    @Override
    public void write(Boolean item, ByteArrayOutputStream baos, Charset charset) {
        baos.write(0);
        baos.write(0);
        baos.write(0);
        baos.write(1);
        baos.write(item ? (byte) 1 : (byte) 0);
    }
    
    @Override
    public String toString(Boolean item) {
        if (item == null) {
            return "NULL";
        }
        return item ? "1" : "0";
    }
}
