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

package org.apache.shardingsphere.singletable.route.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.singletable.route.validator.ddl.SingleTableDropSchemaMetadataValidator;
import org.apache.shardingsphere.singletable.route.validator.ddl.SingleTableDropTableValidator;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;

import java.util.Optional;

/**
 * Single table metadata validator factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SingleTableMetadataValidatorFactory {
    
    /**
     * New instance of single table metadata validator.
     * 
     * @param sqlStatement SQL statement
     * @return created instance
     */
    @SuppressWarnings("rawtypes")
    public static Optional<SingleTableMetadataValidator> newInstance(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof DropSchemaStatement) {
            return Optional.of(new SingleTableDropSchemaMetadataValidator());
        }
        if (sqlStatement instanceof DropTableStatement) {
            return Optional.of(new SingleTableDropTableValidator());
        }
        return Optional.empty();
    }
}
