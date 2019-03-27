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

package org.apache.shardingsphere.core.parse.antlr.sql.segment.table;

import com.google.common.base.Optional;
import lombok.Getter;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.SQLSegment;
import org.apache.shardingsphere.core.parse.parser.token.TableToken;

/**
 * Table segment.
 * 
 * @author duhongjun
 * @author panjuan
 */
@Getter
public class TableSegment implements SQLSegment {
    
    private final TableToken token;
    
    private final String schemaName;
    
    private final String alias;
    
    public TableSegment(final TableToken token, final String schemaName, final String alias) {
        this.token = token;
        this.schemaName = schemaName;
        this.alias = alias;
    }
    
    /**
     * Get table name.
     *
     * @return table name
     */
    public String getName() {
        return token.getTableName();
    }
    
    /**
     * Get schema name.
     *
     * @return schema name
     */
    public Optional<String> getSchemaName() {
        return Optional.fromNullable(schemaName);
    }
    
    /**
     * Get table alias.
     * 
     * @return table alias
     */
    public Optional<String> getAlias() {
        return Optional.fromNullable(alias);
    }
}
