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

package org.apache.shardingsphere.orchestration.internal.keygen;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.orchestration.center.api.DistributedLockManagement;
import org.apache.shardingsphere.orchestration.center.configuration.InstanceConfiguration;
import org.apache.shardingsphere.spi.keygen.ShardingKeyGenerator;

import java.util.Calendar;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Key generator implemented by leaf snowflake algorithms.
 */
public final class LeafSnowflakeKeyGenerator implements ShardingKeyGenerator {
    
    public static final long EPOCH;
    
    private static final long SEQUENCE_BITS = 12L;
    
    private static final long WORKER_ID_BITS = 10L;
    
    private static final long SEQUENCE_MASK = (1 << SEQUENCE_BITS) - 1;
    
    private static final long WORKER_ID_LEFT_SHIFT_BITS = SEQUENCE_BITS;
    
    private static final long TIMESTAMP_LEFT_SHIFT_BITS = WORKER_ID_LEFT_SHIFT_BITS + WORKER_ID_BITS;
    
    private static final int MAX_TOLERATE_TIME_DIFFERENCE_MILLISECONDS = 10000;

    private static final int DEFAULT_VIBRATION_VALUE = 1;
    
    private static final String SERVICE_ID_REGULAR_PATTERN = "^((?!/).)*$";
    
    private static final String DEFAULT_NAMESPACE = "leaf_snowflake";
    
    private static final String DEFAULT_REGISTRY_CENTER = "zookeeper";
    
    private static final String PARENT_NODE = "/leaf_snowflake";
    
    private static final String TIME_NODE = "/time";
    
    private static final String CURRENT_MAX_WORK_ID_NODE = "/current-max-work-id";
    
    private static final String CURRENT_MAX_WORK_ID_DIRECTORY = PARENT_NODE + CURRENT_MAX_WORK_ID_NODE;
    
    private static final String WORK_ID_NODE = "/work-id";
    
    private static final String SLANTING_BAR = "/";
    
    private final TimeService timeService = new TimeService();
    
    @Getter
    @Setter
    private Properties properties = new Properties();

    private DistributedLockManagement distributedLockManagement;
    
    private int sequenceOffset = -1;
    
    private long sequence;
    
    private long lastMilliseconds;
    
    private long workId;
    
    private long lastUpdateTime;
    
    private long maxTolerateTimeDifference;

    private int maxVibrationOffset;
    
    static {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2016, Calendar.NOVEMBER, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        EPOCH = calendar.getTimeInMillis();
    }
    
    @Override
    public String getType() {
        return "LEAF_SNOWFLAKE";
    }
    
    @Override
    public synchronized Comparable<?> generateKey() {
        initializeLeafSnowflakeKeyGeneratorIfNeed();
        return getKey();
    }
    
    @SneakyThrows
    private void initializeLeafSnowflakeKeyGeneratorIfNeed() {
        if (needToBeInitialized()) {
            maxTolerateTimeDifference = initializeMaxTolerateTimeDifference();
            maxVibrationOffset = initializeMaxVibrationOffset();
            distributedLockManagement = initializeDistributedLockManagement();
            initializeTimeNodeIfNeed(maxTolerateTimeDifference, distributedLockManagement);
            initializeCurrentMaxWorkIdNodeIfNeed(distributedLockManagement);
            workId = initializeWorkIdNodeIfNeed(distributedLockManagement);
            scheduledUpdateTimeNode(distributedLockManagement);
        }
    }
    
    private Comparable<?> getKey() {
        long currentMilliseconds = getCurrentMilliseconds();
        long sequence = getSequence(currentMilliseconds);
        Comparable<?> result = getSnowflakeId(currentMilliseconds, sequence);
        updateLastMilliseconds(currentMilliseconds);
        return result;
    }
    
    @SneakyThrows
    private long getCurrentMilliseconds() {
        long result = timeService.getCurrentMillis();
        if (lastMilliseconds > result) {
            long timeDifferenceMilliseconds = lastMilliseconds - result;
            Preconditions.checkState(timeDifferenceMilliseconds < maxTolerateTimeDifference,
                    "Clock is moving backwards, last time is %d milliseconds, current time is %d milliseconds", lastMilliseconds, result);
            Thread.sleep(timeDifferenceMilliseconds);
            result = timeService.getCurrentMillis();
        } else if (lastMilliseconds == result) {
            if (0L == ((sequence + 1) & SEQUENCE_MASK)) {
                do {
                    result = timeService.getCurrentMillis();
                } while (result <= lastMilliseconds);
            }
        }
        return result;
    }
    
    private long getSequence(final long currentMilliseconds) {
        if (lastMilliseconds == currentMilliseconds) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
        } else {
            vibrateSequenceOffset();
            sequence = sequenceOffset;
        }
        return sequence;
    }
    
    private Comparable<?> getSnowflakeId(final long currentMilliseconds, final long sequence) {
        return ((currentMilliseconds - EPOCH) << TIMESTAMP_LEFT_SHIFT_BITS) | (workId << WORKER_ID_LEFT_SHIFT_BITS) | sequence;
    }
    
    private void updateLastMilliseconds(final long currentMilliseconds) {
        lastMilliseconds = currentMilliseconds;
    }
    
    private boolean needToBeInitialized() {
        return null == distributedLockManagement || workId <= 0;
    }
    
    private DistributedLockManagement initializeDistributedLockManagement() {
        InstanceConfiguration leafConfiguration = getDistributedLockManagementConfiguration();
        return new DistributedLockManagementServiceLoader().load(leafConfiguration);
    }
    
    private String getTimeDirectoryWithServiceId() {
        String serviceId = properties.getProperty("service.id");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(serviceId));
        Preconditions.checkArgument(serviceId.matches(SERVICE_ID_REGULAR_PATTERN));
        return PARENT_NODE + SLANTING_BAR + serviceId + TIME_NODE;
    }
    
    private String getWorkIdDirectoryWithServiceId() {
        String serviceId = properties.getProperty("service.id");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(serviceId));
        Preconditions.checkArgument(serviceId.matches(SERVICE_ID_REGULAR_PATTERN));
        return PARENT_NODE + SLANTING_BAR + serviceId + WORK_ID_NODE;
    }
    
    @SneakyThrows
    private void initializeTimeNodeIfNeed(final long maxTolerateTimeDifference, final DistributedLockManagement distributedLockManagement) {
        String timeDirectory = getTimeDirectoryWithServiceId();
        String lastTimeInDistributedLockManagement = distributedLockManagement.get(timeDirectory);
        if (!Strings.isNullOrEmpty(lastTimeInDistributedLockManagement)) {
            long currentTime = timeService.getCurrentMillis();
            long timeDifference = Long.parseLong(lastTimeInDistributedLockManagement) - currentTime;
            if (timeDifference > 0) {
                Preconditions.checkState(timeDifference < maxTolerateTimeDifference,
                        "Clock is moving backwards, last time is %d milliseconds, current time is %d milliseconds", lastTimeInDistributedLockManagement, currentTime);
                Thread.sleep(timeDifference);
            }
        } else {
            long currentTime = timeService.getCurrentMillis();
            distributedLockManagement.persist(timeDirectory, String.valueOf(currentTime));
        }
    }
    
    @SneakyThrows
    private void initializeCurrentMaxWorkIdNodeIfNeed(final DistributedLockManagement distributedLockManagement) {
        String value = distributedLockManagement.get(CURRENT_MAX_WORK_ID_DIRECTORY);
        if (Strings.isNullOrEmpty(value)) {
            distributedLockManagement.persist(CURRENT_MAX_WORK_ID_DIRECTORY, "0");
        }
    }
    
    @SneakyThrows
    private Long initializeWorkIdNodeIfNeed(final DistributedLockManagement distributedLockManagement) {
        String workIdDirectory = getWorkIdDirectoryWithServiceId();
        String workIdInString = distributedLockManagement.get(workIdDirectory);
        if (!Strings.isNullOrEmpty(workIdInString)) {
            return Long.parseLong(workIdInString);
        } else {
            Long result = updateCurrentMaxWorkIdInRegisterCenter();
            distributedLockManagement.persist(workIdDirectory, String.valueOf(result));
            return result;
        }
    }
    
    @SneakyThrows
    private long updateCurrentMaxWorkIdInRegisterCenter() {
        distributedLockManagement.initLock(CURRENT_MAX_WORK_ID_DIRECTORY);
        boolean lockIsAcquired = distributedLockManagement.tryLock();
        Preconditions.checkState(lockIsAcquired, "Try lock fail");
        String id = distributedLockManagement.get(CURRENT_MAX_WORK_ID_DIRECTORY);
        long result = Long.parseLong(id);
        distributedLockManagement.persist(CURRENT_MAX_WORK_ID_DIRECTORY, String.valueOf(result++));
        distributedLockManagement.tryRelease();
        return result;
    }
    
    @SneakyThrows
    private void scheduledUpdateTimeNode(final DistributedLockManagement distributedLockManagement) {
        final String timeDirectory = getTimeDirectoryWithServiceId();
        Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            
            @Override
            public Thread newThread(final Runnable runnable) {
                Thread thread = new Thread(runnable, "schedule-upload-time");
                thread.setDaemon(true);
                return thread;
            }
        }).scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                updateNewData(distributedLockManagement, timeDirectory);
            }
        }, 1L, 3L, TimeUnit.SECONDS);
    }
    
    @SneakyThrows
    private void updateNewData(final DistributedLockManagement distributedLockManagement, final String path) {
        if (timeService.getCurrentMillis() < lastUpdateTime) {
            return;
        }
        distributedLockManagement.persist(path, String.valueOf(timeService.getCurrentMillis()));
        lastUpdateTime = timeService.getCurrentMillis();
    }
    
    private void vibrateSequenceOffset() {
        sequenceOffset = sequenceOffset >= maxVibrationOffset ? 0 : sequenceOffset + 1;
    }
    
    private long initializeMaxTolerateTimeDifference() {
        String maxTimeDifference = properties.getProperty("max.tolerate.time.difference.milliseconds", String.valueOf(MAX_TOLERATE_TIME_DIFFERENCE_MILLISECONDS));
        long result = Long.valueOf(maxTimeDifference);
        Preconditions.checkArgument(result >= 0L && result < Long.MAX_VALUE);
        return result;
    }

    private int initializeMaxVibrationOffset() {
        int result = Integer.parseInt(properties.getProperty("max.vibration.offset", String.valueOf(DEFAULT_VIBRATION_VALUE)));
        Preconditions.checkArgument(result >= 0 && result <= SEQUENCE_MASK, "Illegal max vibration offset");
        return result;
    }
    
    private InstanceConfiguration getDistributedLockManagementConfiguration() {
        InstanceConfiguration result = new InstanceConfiguration(getDistributedLockManagementType(), properties);
        result.setNamespace(DEFAULT_NAMESPACE);
        result.setServerLists(getServerList());
        return result;
    }
    
    private String getDistributedLockManagementType() {
        return properties.getProperty("distributedLockManagementType", DEFAULT_REGISTRY_CENTER);
    }
    
    private String getServerList() {
        String result = properties.getProperty("server.list");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(result));
        return result;
    }
}
