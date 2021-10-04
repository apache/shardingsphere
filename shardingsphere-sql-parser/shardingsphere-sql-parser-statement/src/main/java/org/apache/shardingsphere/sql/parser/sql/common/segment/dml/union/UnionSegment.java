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

package org.apache.shardingsphere.sql.parser.sql.common.segment.dml.union;

import lombok.Getter;
import org.apache.shardingsphere.sql.parser.sql.common.constant.UnionType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

@Getter
public class UnionSegment implements SQLSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final UnionType unionType;
    
    private final SelectStatement selectStatement;
    
    public UnionSegment(final UnionType unionType, final SelectStatement selectStatement, final int startIndex, final int stopIndex) {
        this.unionType = unionType;
        this.selectStatement = selectStatement;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
    }
}
