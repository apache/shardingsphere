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

package io.shardingsphere.transaction.saga;

import java.util.List;
import java.util.UUID;

/**
 * Saga soft transaction.
 * 
 * @author zhangyonglun
 */
public abstract class SagaSoftTransaction {
    
    private String transactionId;
    
    private List<SQLPair> sqlPairs;
    
    /**
     * Begin transaction.
     *
     */
    public final void begin() {
        transactionId = UUID.randomUUID().toString();
    }
    
    /**
     * End transaction.
     * 
     */
    public final void end() {
    
    }
    
    /**
     * Set sql and compensation.
     *
     * @param sql sql
     * @param compensation compensation
     */
    public void setSQLAndCompensation(final String sql, final String compensation) {
        SQLPair sqlPair = new SQLPair(sql, compensation);
        sqlPairs.add(sqlPair);
    }
}
