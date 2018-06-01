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

package io.shardingsphere.jdbc.orchestration.reg.new_zk.client.retry;

/*
 * delay policy
 *
 * @author lidongbo
 */
public class DelayRetryPolicy {
    private static final long BASE_DELAY = 10;
    private static final int BASE_COUNT = 3;
    private static final int RETRY_COUNT_BOUND = 29;
    
    private final int retryCount;
    private final long baseDelay;
    private final long delayUpperBound;
    
    /*
    * Millis
    */
    public DelayRetryPolicy(long baseDelay) {
        this(RETRY_COUNT_BOUND, baseDelay, Integer.MAX_VALUE);
    }
    
    public DelayRetryPolicy(int retryCount, long baseDelay, long delayUpperBound) {
        this.retryCount = retryCount;
        this.baseDelay = baseDelay;
        this.delayUpperBound = delayUpperBound;
    }
    
    public int getRetryCount() {
        return retryCount;
    }
    
    public long getBaseDelay() {
        return baseDelay;
    }
    
    public long getDelayUpperBound() {
        return delayUpperBound;
    }
    
    public static DelayRetryPolicy newNoInitDelayPolicy(){
        return new DelayRetryPolicy(BASE_COUNT, BASE_DELAY, Integer.MAX_VALUE);
    }
}
