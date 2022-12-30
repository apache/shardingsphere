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

package org.apache.shardingsphere.sharding.rewrite.token.pojo;

import lombok.Getter;
import org.apache.shardingsphere.infra.context.ConnectionContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.Substitutable;
import org.apache.shardingsphere.sql.parser.sql.common.enums.DirectionType;

/**
 * Fetch direction token.
 */
public final class FetchDirectionToken extends SQLToken implements Substitutable {
    
    @Getter
    private final int stopIndex;
    
    private final DirectionType directionType;
    
    private final long fetchCount;
    
    private final String cursorName;
    
    private final ConnectionContext connectionContext;
    
    public FetchDirectionToken(final int startIndex, final int stopIndex, final DirectionType directionType, final long fetchCount,
                               final String cursorName, final ConnectionContext connectionContext) {
        super(startIndex);
        this.stopIndex = stopIndex;
        this.directionType = directionType;
        this.fetchCount = fetchCount;
        this.cursorName = cursorName;
        this.connectionContext = connectionContext;
    }
    
    @Override
    public String toString() {
        long actualFetchCount = Math.max(fetchCount - connectionContext.getCursorConnectionContext().getMinGroupRowCounts().getOrDefault(cursorName, 0L), 0);
        if (DirectionType.isForwardCountDirectionType(directionType)) {
            return " FORWARD " + actualFetchCount + " ";
        }
        if (DirectionType.isBackwardCountDirectionType(directionType)) {
            return " BACKWARD " + actualFetchCount + " ";
        }
        if (DirectionType.ABSOLUTE_COUNT.equals(directionType)) {
            return " ABSOLUTE " + actualFetchCount + " ";
        }
        if (DirectionType.RELATIVE_COUNT.equals(directionType)) {
            return " RELATIVE " + actualFetchCount + " ";
        }
        return directionType.getName();
    }
}
