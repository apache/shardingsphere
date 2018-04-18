/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.parsing.parser.sql.dml.insert;

import com.google.common.base.Optional;
import io.shardingjdbc.core.parsing.parser.context.condition.GeneratedKeyCondition;
import io.shardingjdbc.core.parsing.parser.context.condition.Column;
import io.shardingjdbc.core.parsing.parser.context.condition.Conditions;
import io.shardingjdbc.core.parsing.parser.sql.dml.DMLStatement;
import io.shardingjdbc.core.parsing.parser.token.GeneratedKeyToken;
import io.shardingjdbc.core.parsing.parser.token.SQLToken;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert statement.
 *
 * @author zhangliang
 */
@Getter
@Setter
@ToString(callSuper = true)
public final class InsertStatement extends DMLStatement {
    
    private final Collection<Column> columns = new LinkedList<>();
    
    private final List<Conditions> multipleConditions = new LinkedList<>();
    
    private int columnsListLastPosition;
    
    private int generateKeyColumnIndex = -1;
    
    private int afterValuesPosition;
    
    private int valuesListLastPosition;
    
    private GeneratedKeyCondition generatedKeyCondition;
    
    /**
     * Find generated key token.
     * 
     * @return generated key token
     */
    public Optional<GeneratedKeyToken> findGeneratedKeyToken() {
        for (SQLToken each : getSqlTokens()) {
            if (each instanceof GeneratedKeyToken) {
                return Optional.of((GeneratedKeyToken) each);
            }
        }
        return Optional.absent();
    }
}
