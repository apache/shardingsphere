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

package io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.fixture;

import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.BinaryStatementRegistry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BinaryStatementRegistryUtil {
    
    /**
     * Reset {@code BinaryStatementRegistry}.
     */
    @SneakyThrows
    public static void reset() {
        Field statementIdAssignerField = BinaryStatementRegistry.class.getDeclaredField("statementIdAssigner");
        statementIdAssignerField.setAccessible(true);
        ((Map) statementIdAssignerField.get(BinaryStatementRegistry.getInstance())).clear();
        Field binaryStatementsField = BinaryStatementRegistry.class.getDeclaredField("binaryStatements");
        binaryStatementsField.setAccessible(true);
        ((Map) binaryStatementsField.get(BinaryStatementRegistry.getInstance())).clear();
        Field sequenceField = BinaryStatementRegistry.class.getDeclaredField("sequence");
        sequenceField.setAccessible(true);
        ((AtomicInteger) sequenceField.get(BinaryStatementRegistry.getInstance())).set(0);
    }
}
