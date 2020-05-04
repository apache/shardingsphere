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

package org.apache.shardingsphere.dbtest.engine.util;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Integrate test watcher.
 */
public final class IntegrateTestWatcher extends TestWatcher {
    
    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    base.evaluate();
                } catch (final AssertionError err) {
                    AssertionError assertionError = new AssertionError(description.getDisplayName() + err.getMessage());
                    assertionError.setStackTrace(err.getStackTrace());
                    throw assertionError;
                    // CHECKSTYLE:OFF
                } catch (final Exception ex) {
                    // CHECKSTYLE:ON
                    Exception exception = new Exception(description.getDisplayName() + ex.getMessage());
                    exception.setStackTrace(ex.getStackTrace());
                    throw exception;
                }
            }
        };
    }
}
