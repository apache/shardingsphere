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

package org.apache.shardingsphere.core.parse.hook;

import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.spi.NewInstanceServiceLoader;

import java.util.Collection;

/**
 * Parsing hook for SPI.
 *
 * @author zhangliang
 */
public final class SPIParsingHook implements ParsingHook {
    
    private final Collection<ParsingHook> parsingHooks = NewInstanceServiceLoader.newServiceInstances(ParsingHook.class);
    
    static {
        NewInstanceServiceLoader.register(ParsingHook.class);
    }
    
    @Override
    public void start(final String sql) {
        for (ParsingHook each : parsingHooks) {
            each.start(sql);
        }
    }
    
    @Override
    public void finishSuccess(final SQLStatement sqlStatement) {
        for (ParsingHook each : parsingHooks) {
            each.finishSuccess(sqlStatement);
        }
    }
    
    @Override
    public void finishFailure(final Exception cause) {
        for (ParsingHook each : parsingHooks) {
            each.finishFailure(cause);
        }
    }
}
