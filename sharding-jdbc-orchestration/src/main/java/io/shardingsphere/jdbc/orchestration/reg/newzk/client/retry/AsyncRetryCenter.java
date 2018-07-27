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

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.base.BaseOperation;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.DelayQueue;

/*
 * Async retry center.
 *
 * @author lidongbo
 */
@Slf4j
public enum AsyncRetryCenter {
    
    INSTANCE;
    
    private final DelayQueue<BaseOperation> queue = new DelayQueue<>();
    
    private final RetryThread retryThread = new RetryThread(queue);
    
    private boolean started;
    
    private DelayRetryPolicy delayRetryPolicy;
    
    /**
     * init.
     *
     * @param delayRetryPolicy delayRetryPolicy
     */
    public void init(final DelayRetryPolicy delayRetryPolicy) {
        log.debug("delayRetryPolicy init");
        if (delayRetryPolicy == null) {
            log.warn("delayRetryPolicy is null and auto init with DelayRetryPolicy.defaultDelayPolicy");
            this.delayRetryPolicy = DelayRetryPolicy.defaultDelayPolicy();
            return;
        }
        this.delayRetryPolicy = delayRetryPolicy;
    }
    
    /**
     * start.
     */
    public synchronized void start() {
        if (started) {
            return;
        }
        retryThread.setName("retry-thread");
        retryThread.start();
        this.started = true;
    }
    
    /**
     * add async operation.
     *
     * @param operation operation
     */
    public void add(final BaseOperation operation) {
        if (delayRetryPolicy == null) {
            log.warn("delayRetryPolicy no init and auto init with DelayRetryPolicy.defaultDelayPolicy");
            delayRetryPolicy = DelayRetryPolicy.defaultDelayPolicy();
        }
        operation.setDelayPolicyExecutor(new DelayPolicyExecutor(delayRetryPolicy));
        queue.offer(operation);
        log.debug("enqueue operation:{}", operation.toString());
    }
}
