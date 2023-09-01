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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.calculator;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract single table inventory calculator.
 */
@Slf4j
public abstract class AbstractSingleTableInventoryCalculator implements SingleTableInventoryCalculator {
    
    private final AtomicBoolean canceling = new AtomicBoolean(false);
    
    private final AtomicReference<Statement> currentStatement = new AtomicReference<>();
    
    protected final void setCurrentStatement(final Statement statement) {
        currentStatement.set(statement);
    }
    
    @SneakyThrows(SQLException.class)
    @Override
    public void cancel() {
        canceling.set(true);
        Statement statement = currentStatement.get();
        if (null == statement || statement.isClosed()) {
            log.info("cancel, statement is null or closed");
            return;
        }
        long startTimeMillis = System.currentTimeMillis();
        try {
            statement.cancel();
        } catch (final SQLFeatureNotSupportedException ex) {
            log.info("cancel is not supported: {}", ex.getMessage());
            // CHECKSTYLE:OFF
        } catch (final SQLException | RuntimeException ex) {
            // CHECKSTYLE:ON
            log.info("cancel failed: {}", ex.getMessage());
        }
        log.info("cancel cost {} ms", System.currentTimeMillis() - startTimeMillis);
    }
    
    /**
     * Is canceling.
     *
     * @return is canceling or not
     */
    public final boolean isCanceling() {
        return canceling.get();
    }
}
