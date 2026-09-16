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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.protocol.binary.BinaryCell;
import org.apache.shardingsphere.database.protocol.binary.BinaryRow;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;

import java.util.ArrayList;
import java.util.List;

/**
 * Firebird binary row builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FirebirdBinaryRowBuilder {
    
    /**
     * Build binary row.
     *
     * @param row query response row
     * @param parameterTypes column types
     * @return binary row
     */
    public static BinaryRow build(final QueryResponseRow row, final List<FirebirdBinaryColumnType> parameterTypes) {
        List<BinaryCell> result = new ArrayList<>(row.getCells().size());
        for (int i = 0; i < row.getCells().size(); i++) {
            result.add(new BinaryCell(parameterTypes.get(i), row.getCells().get(i).getData()));
        }
        return new BinaryRow(result);
    }
}
