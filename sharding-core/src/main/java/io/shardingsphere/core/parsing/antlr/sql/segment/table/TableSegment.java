/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.antlr.sql.segment.table;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.parser.token.TableToken;
import lombok.Getter;
import lombok.Setter;

/**
 * Table segment.
 * 
 * @author duhongjun
 */
@Getter
@Setter
public class TableSegment implements SQLSegment {
    
    private final String name;
    
    private final TableToken token;
    
    private String schemaName;
    
    private String alias;
    
    private int aliasStartPosition = -1;
    
    public TableSegment(final TableToken token) {
        name = token.getTableName();
        this.token = token;
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
