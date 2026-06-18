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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol;

import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

/**
 * Binary protocol value for Firebird.
 */
public interface FirebirdBinaryProtocolValue {
    
    /**
     * Read binary protocol value.
     *
     * @param payload payload operation for Firebird packet
     * @return binary value result
     */
    Object read(FirebirdPacketPayload payload);
    
    /**
     * Write binary protocol value.
     *
     * @param payload payload operation for Firebird packet
     * @param value value to be written
     */
    void write(FirebirdPacketPayload payload, Object value);
    
    /**
     * Get type length.
     *
     * @param payload payload operation for Firebird packet
     * @return type length
     */
    int getLength(FirebirdPacketPayload payload);
}
