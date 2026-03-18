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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info;

/**
 * Firebird info packet type.
 */
public interface FirebirdInfoPacketType {
    
    /**
     * Get code of the info packet type.
     *
     * @return numeric code representing the packet type
     */
    int getCode();
    
    /**
     * Determine whether this info packet type is common.
     *
     * @return true if the packet type is common, false otherwise
     */
    boolean isCommon();
}
