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

package io.shardingsphere.proxy.backend.jdbc.transaction;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.TCLType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Abstract transaction engine.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
@Getter
@Setter
public abstract class TransactionEngine {
    
    private final String sql;
    
    // TODO move to TCLParser
    protected Optional<TCLType> parseSQL() {
        switch (sql.toUpperCase()) {
            case "BEGIN": 
            case "START TRANSACTION":
                // TODO SET AUTOCOMMIT=0 is not transaction begin flag? 
            case "SET AUTOCOMMIT=0":
                return Optional.of(TCLType.BEGIN);
            case "COMMIT":
                return Optional.of(TCLType.COMMIT);
            case "ROLLBACK":
                return Optional.of(TCLType.ROLLBACK);
            default:
                return Optional.absent();
        }
    }
    
    /**
     * Execute transaction with binding transaction manager.
     *
     * @return skip or not skip access backend databases 
     * @throws Exception exception
     */
    public abstract boolean execute() throws Exception;
}
