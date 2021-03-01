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

package org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.MySQLBinaryStatementRegistry;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BinaryStatementRegistryUtil {
    
    /**
     * Reset {@code MySQLBinaryStatementRegistry}.
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static void reset() {
        Field statementIdAssignerField = MySQLBinaryStatementRegistry.class.getDeclaredField("statementIdAssigner");
        statementIdAssignerField.setAccessible(true);
        ((Map) statementIdAssignerField.get(MySQLBinaryStatementRegistry.getInstance())).clear();
        Field binaryStatementsField = MySQLBinaryStatementRegistry.class.getDeclaredField("binaryStatements");
        binaryStatementsField.setAccessible(true);
        ((Map) binaryStatementsField.get(MySQLBinaryStatementRegistry.getInstance())).clear();
        Field sequenceField = MySQLBinaryStatementRegistry.class.getDeclaredField("sequence");
        sequenceField.setAccessible(true);
        ((AtomicInteger) sequenceField.get(MySQLBinaryStatementRegistry.getInstance())).set(0);
    }
}
