/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.dbtest;

import io.shardingjdbc.core.constant.DatabaseType;
import lombok.Getter;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

/**
 * Integrate test running environment.
 *
 * @author zhangliang
 */
@Getter
public final class IntegrateTestRunningEnvironment {
    
    private static final IntegrateTestRunningEnvironment INSTANCE = new IntegrateTestRunningEnvironment();
    
    private final boolean initialized;
    
    private final String assertPath;
    
    private final Collection<DatabaseType> databaseTypes;
    
    private IntegrateTestRunningEnvironment() {
        Properties prop = new Properties();
        try {
            prop.load(StartTest.class.getClassLoader().getResourceAsStream("integrate/env.properties"));
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        initialized = Boolean.valueOf(prop.getProperty("initialized", Boolean.FALSE.toString()));
        assertPath = prop.getProperty("assert.path");
        databaseTypes = new LinkedList<>();
        for (String each : prop.getProperty("databases", DatabaseType.H2.name()).split(",")) {
            databaseTypes.add(DatabaseType.valueOf(each.trim()));
        }
    }
    
    /**
     * Get instance.
     * 
     * @return singleton instance
     */
    public static IntegrateTestRunningEnvironment getInstance() {
        return INSTANCE;
    }
}
