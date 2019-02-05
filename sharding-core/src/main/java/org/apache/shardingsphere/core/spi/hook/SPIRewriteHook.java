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

package org.apache.shardingsphere.core.spi.hook;

import org.apache.shardingsphere.core.routing.SQLUnit;
import org.apache.shardingsphere.core.routing.type.TableUnit;
import org.apache.shardingsphere.core.spi.NewInstanceServiceLoader;
import org.apache.shardingsphere.spi.hook.RewriteHook;

import java.util.Collection;

/**
 * Rewrite hook for SPI.
 *
 * @author yangyi
 */
public final class SPIRewriteHook implements RewriteHook {
    
    private final Collection<RewriteHook> rewriteHooks = NewInstanceServiceLoader.newServiceInstances(RewriteHook.class);
    
    static {
        NewInstanceServiceLoader.register(RewriteHook.class);
    }
    
    @Override
    public void start(final TableUnit tableUnit) {
        for (RewriteHook each : rewriteHooks) {
            each.start(tableUnit);
        }
    }
    
    @Override
    public void finishSuccess(final SQLUnit sqlUnit) {
        for (RewriteHook each : rewriteHooks) {
            each.finishSuccess(sqlUnit);
        }
    }
    
    @Override
    public void finishFailure(final Exception cause) {
        for (RewriteHook each : rewriteHooks) {
            each.finishFailure(cause);
        }
    }
}
