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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Firebird return column packet.
 */
@RequiredArgsConstructor
@Getter
public final class FirebirdReturnBinaryColumn {
    
    private final FirebirdBinaryColumnType type;
    
    private final int length;
    
    private final int subtype;
    
    private final int scale;
    
    public FirebirdReturnBinaryColumn(final FirebirdBinaryColumnType type) {
        if (null == type) {
            this.type = FirebirdBinaryColumnType.LONG;
            this.length = FirebirdBinaryColumnType.LONG.getLength();
            this.subtype = FirebirdBinaryColumnType.LONG.getSubtype();
        } else {
            this.type = type;
            this.length = type.getLength();
            this.subtype = type.getSubtype();
        }
        this.scale = 0;
    }
    
    public FirebirdReturnBinaryColumn(final FirebirdBinaryColumnType type, final int length) {
        if (null == type) {
            this.type = FirebirdBinaryColumnType.LONG;
            this.subtype = FirebirdBinaryColumnType.LONG.getSubtype();
        } else {
            this.type = type;
            this.subtype = type.getSubtype();
        }
        this.length = length;
        this.scale = 0;
    }
    
    public FirebirdReturnBinaryColumn(final FirebirdBinaryColumnType type, final int length, final int subtype) {
        if (null == type) {
            this.type = FirebirdBinaryColumnType.LONG;
        } else {
            this.type = type;
        }
        this.length = length;
        this.subtype = subtype;
        this.scale = 0;
    }
}
