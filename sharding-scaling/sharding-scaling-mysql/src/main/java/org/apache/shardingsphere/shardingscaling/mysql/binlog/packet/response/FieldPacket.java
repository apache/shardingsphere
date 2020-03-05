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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.response;

import org.apache.shardingsphere.shardingscaling.mysql.binlog.codec.DataTypesCodec;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

/**
 * MySQL field packet.
 *
 * <p>
 *     MySQL Internals Manual  /  MySQL Client/Server Protocol  /  Text Protocol  /  COM_QUERY  /  COM_QUERY Response
 *     https://dev.mysql.com/doc/internals/en/com-query-response.html#packet-Protocol::ColumnDefinition41
 * </p>
 */
@Getter
public final class FieldPacket extends AbstractPacket {
    
    private String catalog;
    
    private String schema;
    
    private String table;
    
    private String originalTable;
    
    private String fieldName;
    
    private String originalFieldName;
    
    private int character;
    
    private long length;
    
    private short type;
    
    private int flags;
    
    private short decimals;
    
    private String definition;
    
    @Override
    public void fromByteBuf(final ByteBuf data) {
        catalog = DataTypesCodec.readLengthCodedString(data);
        schema = DataTypesCodec.readLengthCodedString(data);
        table = DataTypesCodec.readLengthCodedString(data);
        originalTable = DataTypesCodec.readLengthCodedString(data);
        fieldName = DataTypesCodec.readLengthCodedString(data);
        originalFieldName = DataTypesCodec.readLengthCodedString(data);
        character = DataTypesCodec.readUnsignedInt2LE(data);
        length = DataTypesCodec.readUnsignedInt4LE(data);
        type = DataTypesCodec.readUnsignedInt1(data);
        flags = DataTypesCodec.readUnsignedInt2LE(data);
        decimals = DataTypesCodec.readUnsignedInt1(data);
        // fill
        data.readerIndex(data.readerIndex() + 2);
        definition = DataTypesCodec.readLengthCodedString(data);
    }
}
