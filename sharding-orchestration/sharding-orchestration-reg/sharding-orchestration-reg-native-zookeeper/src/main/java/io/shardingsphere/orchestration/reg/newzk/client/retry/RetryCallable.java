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

package io.shardingsphere.orchestration.reg.newzk.client.retry;

import io.shardingsphere.orchestration.reg.newzk.client.action.IProvider;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.section.Connection;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.zookeeper.KeeperException;

/**
 * Sync retry call.
 *
 * @author lidongbo
 */
@Getter(value = AccessLevel.PROTECTED)
public abstract class RetryCallable {
    
    private final IProvider provider;
    
    private final DelayPolicyExecutor delayPolicyExecutor;
    
    public RetryCallable(final IProvider provider, final DelayRetryPolicy delayRetryPolicy) {
        this.provider = provider;
        delayPolicyExecutor = new DelayPolicyExecutor(delayRetryPolicy);
    }
    
    /**
     * Call the action.
     *
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    public abstract void call() throws KeeperException, InterruptedException;
    
    /**
     * Call without result.
     *
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    public void exec() throws KeeperException, InterruptedException {
        try {
            call();
        } catch (final KeeperException ex) {
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
                Thread.sleep(delay);
            } else {
                if (delayPolicyExecutor.hasNext()) {
                    exec();
                }
                break;
            }
        }
    }
}
