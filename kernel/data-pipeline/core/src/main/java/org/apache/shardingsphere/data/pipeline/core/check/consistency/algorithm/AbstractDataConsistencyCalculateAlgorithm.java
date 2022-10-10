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

package org.apache.shardingsphere.data.pipeline.core.check.consistency.algorithm;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCalculateAlgorithm;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;

/**
 * Abstract data consistency calculate algorithm.
 */
@Slf4j
public abstract class AbstractDataConsistencyCalculateAlgorithm implements DataConsistencyCalculateAlgorithm {
    
    @Getter(AccessLevel.PROTECTED)
    private volatile boolean canceling;
    
    private volatile Statement currentStatement;
    
    protected <T extends Statement> T setCurrentStatement(final T statement) {
        this.currentStatement = statement;
        return statement;
    }
    
    @Override
    public void cancel() throws SQLException {
        canceling = true;
        Statement statement = currentStatement;
        if (null == statement || statement.isClosed()) {
            log.info("cancel, statement is null or closed");
            return;
        }
        long startTimeMillis = System.currentTimeMillis();
        try {
            statement.cancel();
        } catch (final SQLFeatureNotSupportedException ex) {
            log.info("cancel is not supported: {}", ex.getMessage());
        } catch (final SQLException ex) {
            log.info("cancel failed: {}", ex.getMessage());
        }
        log.info("cancel cost {} ms", System.currentTimeMillis() - startTimeMillis);
    }
}
