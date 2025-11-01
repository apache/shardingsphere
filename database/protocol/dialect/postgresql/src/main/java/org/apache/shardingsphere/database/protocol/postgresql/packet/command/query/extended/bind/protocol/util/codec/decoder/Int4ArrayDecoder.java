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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.util.codec.decoder;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Int4ArrayDecoder.
 */
public class Int4ArrayDecoder extends AbstractObjectArrayDecoder<Integer> {
    
    public Int4ArrayDecoder() {
        super(Integer.class);
    }
    
    @Override
    public Integer parseValue(final int length, final ByteBuffer bytes, final Charset charset) {
        return bytes.getInt();
    }
}
