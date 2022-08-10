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

package org.apache.shardingsphere.infra.session;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.session.cursor.CursorSQLSession;

import java.util.Optional;

/**
 * SQL session.
 */
public final class SQLSession implements AutoCloseable {
    
    @Getter
    private final CursorSQLSession cursorSQLSession = new CursorSQLSession();
    
    @Setter
    private String trafficInstanceId;
    
    /**
     * Get traffic instance id.
     * 
     * @return traffic instance id
     */
    public Optional<String> getTrafficInstanceId() {
        return Optional.ofNullable(trafficInstanceId);
    }
    
    @Override
    public void close() {
        clearTrafficInstance();
        clearCursorSQLSession();
    }
    
    /**
     * Clear traffic instance.
     */
    public void clearTrafficInstance() {
        trafficInstanceId = null;
    }
    
    /**
     * Clear cursor sql session.
     */
    public void clearCursorSQLSession() {
        cursorSQLSession.close();
    }
}
