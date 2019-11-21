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

package io.shardingsphere.core.parsing.antlr.rule.registry.statement;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.SQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.optimizer.SQLStatementOptimizer;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.LinkedList;

/**
 * SQL statement rule.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class SQLStatementRule {
    
    private final String contextName;
    
    private final Class<? extends SQLStatement> sqlStatementClass;
    
    private final Collection<SQLSegmentExtractor> extractors = new LinkedList<>();
    
    private final SQLStatementOptimizer optimizer;
    
    /**
     * Get SQL statement optimizer.
     * 
     * @return SQL statement optimizer
     */
    public Optional<SQLStatementOptimizer> getOptimizer() {
        return Optional.fromNullable(optimizer);
    }
}
