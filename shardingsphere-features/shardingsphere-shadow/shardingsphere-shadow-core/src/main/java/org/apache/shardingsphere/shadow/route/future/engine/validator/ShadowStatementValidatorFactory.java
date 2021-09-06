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

package org.apache.shardingsphere.shadow.route.future.engine.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.shadow.route.future.engine.validator.dml.ShadowInsertStatementValidator;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;

import java.util.Optional;

/**
 * Shadow statement validator factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowStatementValidatorFactory {
    
    /**
     * New instance of shadow statement validator.
     *
     * @param sqlStatement sql statement
     * @return instance of shadow statement validator
     */
    public static Optional<ShadowStatementValidator> newInstance(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof DMLStatement) {
            return getDMLStatementValidator(sqlStatement);
        }
        return Optional.empty();
    }
    
    private static Optional<ShadowStatementValidator> getDMLStatementValidator(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof InsertStatement) {
            return Optional.of(new ShadowInsertStatementValidator());
        }
        return Optional.empty();
    }
}
