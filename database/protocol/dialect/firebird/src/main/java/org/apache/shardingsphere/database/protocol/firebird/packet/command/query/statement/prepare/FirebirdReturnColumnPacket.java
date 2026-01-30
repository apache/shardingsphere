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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;

import java.util.Collection;

/**
 * Firebird allocate statement packet.
 */
@RequiredArgsConstructor
public final class FirebirdReturnColumnPacket extends FirebirdPacket {

    private final Collection<FirebirdSQLInfoPacketType> requestedItems;

    private final int index;

    private final ShardingSphereTable table;

    private final ShardingSphereColumn column;

    private final String tableAlias;

    private final String columnAlias;

    private final String owner;

    private final Integer columnLength;

    private final boolean blobColumn;

    private final Integer blobSubType;

    @Override
    protected void write(final FirebirdPacketPayload payload) {
        FirebirdBinaryColumnType columnType = blobColumn ? FirebirdBinaryColumnType.BLOB : FirebirdBinaryColumnType.valueOfJDBCType(column.getDataType());
        for (FirebirdSQLInfoPacketType requestedItem : requestedItems) {
            switch (requestedItem) {
                case SQLDA_SEQ:
                    FirebirdPrepareStatementReturnPacket.writeInt(FirebirdSQLInfoPacketType.SQLDA_SEQ, index, payload);
                    break;
                case TYPE:
                    int type = columnType.getValue() + 1;
                    FirebirdPrepareStatementReturnPacket.writeInt(FirebirdSQLInfoPacketType.TYPE, type, payload);
                    break;
                case SUB_TYPE:
                    int subType = columnType.getSubtype();
                    if (columnType == FirebirdBinaryColumnType.BLOB && null != blobSubType) {
                        subType = blobSubType;
                    }
                    FirebirdPrepareStatementReturnPacket.writeInt(FirebirdSQLInfoPacketType.SUB_TYPE, subType, payload);
                    break;
                case SCALE:
                    FirebirdPrepareStatementReturnPacket.writeInt(FirebirdSQLInfoPacketType.SCALE, 0, payload);
                    break;
                case LENGTH:
                    if (columnType == FirebirdBinaryColumnType.VARYING
                            || columnType == FirebirdBinaryColumnType.LEGACY_VARYING
                            || columnType == FirebirdBinaryColumnType.TEXT
                            || columnType == FirebirdBinaryColumnType.LEGACY_TEXT) {
                        FirebirdPrepareStatementReturnPacket.writeInt(FirebirdSQLInfoPacketType.LENGTH, columnLength, payload);
                    } else {
                        int length = columnType.getLength();
                        FirebirdPrepareStatementReturnPacket.writeInt(FirebirdSQLInfoPacketType.LENGTH, length, payload);
                    }
                    break;
                case FIELD:
                    FirebirdPrepareStatementReturnPacket.writeString(FirebirdSQLInfoPacketType.FIELD, column.getName(), payload);
                    break;
                case ALIAS:
                    FirebirdPrepareStatementReturnPacket.writeString(FirebirdSQLInfoPacketType.ALIAS, columnAlias, payload);
                    break;
                case RELATION:
                    FirebirdPrepareStatementReturnPacket.writeString(FirebirdSQLInfoPacketType.RELATION, table.getName(), payload);
                    break;
                case RELATION_ALIAS:
                    FirebirdPrepareStatementReturnPacket.writeString(FirebirdSQLInfoPacketType.RELATION_ALIAS, tableAlias, payload);
                    break;
                case OWNER:
                    FirebirdPrepareStatementReturnPacket.writeString(FirebirdSQLInfoPacketType.OWNER, owner, payload);
                    break;
                case DESCRIBE_END:
                    FirebirdPrepareStatementReturnPacket.writeCode(FirebirdSQLInfoPacketType.DESCRIBE_END, payload);
                    break;
                default:
                    throw new FirebirdProtocolException("Unknown statement info request type %d", requestedItem);
            }
        }
    }
}
