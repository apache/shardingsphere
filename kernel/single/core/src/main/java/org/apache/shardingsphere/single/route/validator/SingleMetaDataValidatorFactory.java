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

package org.apache.shardingsphere.single.route.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.single.route.validator.ddl.SingleDropSchemaMetaDataValidator;
import org.apache.shardingsphere.single.route.validator.ddl.SingleDropTableValidator;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;

import java.util.Optional;

/**
 * Single meta data validator factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SingleMetaDataValidatorFactory {
    
    /**
     * New instance of single meta data validator.
     * 
     * @param sqlStatement SQL statement
     * @return created instance
     */
    public static Optional<SingleMetaDataValidator> newInstance(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof DropSchemaStatement) {
            return Optional.of(new SingleDropSchemaMetaDataValidator());
        }
        if (sqlStatement instanceof DropTableStatement) {
            return Optional.of(new SingleDropTableValidator());
        }
        return Optional.empty();
    }
}
