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

package org.apache.shardingsphere.parser.distsql.parser.statement.updatable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.UpdatableGlobalRuleRALStatement;
import org.apache.shardingsphere.parser.distsql.parser.segment.CacheOptionSegment;

/**
 * Alter SQL parser rule statement.
 */
@RequiredArgsConstructor
@Getter
public final class AlterSQLParserRuleStatement extends UpdatableGlobalRuleRALStatement {
    
    private final Boolean sqlCommentParseEnable;
    
    private final CacheOptionSegment parseTreeCache;
    
    private final CacheOptionSegment sqlStatementCache;
}
