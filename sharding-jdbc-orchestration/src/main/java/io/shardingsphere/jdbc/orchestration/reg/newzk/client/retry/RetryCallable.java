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

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.retry;

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IProvider;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.Connection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;

/*
 * Sync retry call.
 *
 * @author lidongbo
 */
@Slf4j
public abstract class RetryCallable {
    
    @Getter(value = AccessLevel.PROTECTED)
    private final DelayPolicyExecutor delayPolicyExecutor;
    
    @Getter(value = AccessLevel.PROTECTED)
    private final IProvider provider;
    
    public RetryCallable(final IProvider provider, final DelayRetryPolicy delayRetryPolicy) {
        this.delayPolicyExecutor = new DelayPolicyExecutor(delayRetryPolicy);
        this.provider = provider;
    }
    
    /**
     * Call the action.
     *
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    public abstract void call() throws KeeperException, InterruptedException;
    
    /**
     * Call without result.
     *
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    public void exec() throws KeeperException, InterruptedException {
        try {
            call();
        } catch (final KeeperException ex) {
            log.warn("exec KeeperException:{}", ex.getMessage());
            delayPolicyExecutor.next();
            if (Connection.needReset(ex)) {
                provider.resetConnection();
            }
            execDelay();
        }
    }
    
    private void execDelay() throws KeeperException, InterruptedException {
        for (;;) {
            long delay = delayPolicyExecutor.getNextTick() - System.currentTimeMillis();
            if (delay > 0) {
                log.debug("exec delay:{}", delay);
                Thread.sleep(delay);
            } else {
                if (delayPolicyExecutor.hasNext()) {
                    log.debug("exec hasNext");
                    exec();
                }
                break;
            }
        }
    }
}
