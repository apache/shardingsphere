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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.codec;

/**
 * MySQL protocol constants.
 */
public final class PacketConstants {
    
    public static final byte PROTOCOL_VERSION = 0x0a;
    
    public static final byte OK_PACKET_MARK = 0x00;
    
    public static final byte EOF_PACKET_MARK = (byte) 0xfe;
    
    public static final byte ERR_PACKET_MARK = (byte) 0xff;
}
