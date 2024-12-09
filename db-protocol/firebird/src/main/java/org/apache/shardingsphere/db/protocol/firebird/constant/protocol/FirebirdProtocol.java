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

package org.apache.shardingsphere.db.protocol.firebird.constant.protocol;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import org.apache.shardingsphere.db.protocol.firebird.constant.FirebirdArchType;

@Getter
public final class FirebirdProtocol {

    private final FirebirdProtocolVersion version;
    private final FirebirdArchType arch;
    private final int minType;
    private final int maxType;
    private final int weight;

    public FirebirdProtocol(ByteBuf buffer) {
        this.version = FirebirdProtocolVersion.valueOf(buffer.readInt());
        this.arch = FirebirdArchType.valueOf(buffer.readInt());
        this.minType = buffer.readInt();
        this.maxType = buffer.readInt();
        this.weight = buffer.readInt();
    }
}
