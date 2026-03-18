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

package org.apache.shardingsphere.test.e2e.sql.framework;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;

/**
 * SQL E2E IT settings extension.
 */
@Slf4j
public final class SQLE2EITSettingsExtension implements TestWatcher {
    
    @Override
    public void testFailed(final ExtensionContext context, final Throwable cause) {
        log.error("Error case: {}, message: {}", context.getDisplayName(), getStackTrace(cause));
    }
    
    private String getStackTrace(final Throwable cause) {
        if (null == cause) {
            return "";
        }
        try (
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                PrintStream printStream = new PrintStream(out)) {
            cause.printStackTrace(printStream);
            printStream.flush();
            return out.toString();
            // CHECKSTYLE:OFF
        } catch (final Exception ignored) {
            // CHECKSTYLE:ON
            return "";
        }
    }
    
    @Override
    public void testDisabled(final ExtensionContext context, final Optional<String> reason) {
        log.info("Disable case: {}, reason: {}", context.getDisplayName(), reason.orElse(""));
    }
    
    @Override
    public void testSuccessful(final ExtensionContext context) {
        log.info("Success case: {}", context.getDisplayName());
    }
    
    @Override
    public void testAborted(final ExtensionContext context, final Throwable cause) {
        log.info("Abort case: {}, message: {}", context.getDisplayName(), getStackTrace(cause));
    }
}
