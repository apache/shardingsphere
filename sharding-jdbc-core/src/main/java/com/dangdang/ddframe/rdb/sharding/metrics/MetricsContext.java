/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.ai.raptor.dal.metrics;

import com.ai.raptor.dal.config.ShardingProperties;
import com.ai.raptor.dal.config.ShardingPropertiesConstant;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;
import java.util.concurrent.TimeUnit;
import org.slf4j.LoggerFactory;

/**
 * 度量上下文持有者.
 *
 * <p>
 * 多个ShardingDataSource使用静态度量上下文会造成数据污染, 所以将度量上下文对象绑定到ThreadLocal中.
 * </p>
 *
 * @author gaohongtao
 * @author zhangliang
 */
public final class MetricsContext {

  private MetricsContext() {
  }

  private static final MetricRegistry METRIC_REGISTRY = new MetricRegistry();
  private static ScheduledReporter reporter = null;
  private static final Object LOCK = new Object();

  /**
   * 初始化度量上下文持有者.
   *
   * @param shardingProperties Sharding-JDBC的配置属性
   */
  public static void init(final ShardingProperties shardingProperties) {
    boolean metricsEnabled = shardingProperties.getValue(ShardingPropertiesConstant.METRICS_ENABLE);
    if (!metricsEnabled) {
      return;
    }
    long period = shardingProperties
        .getValue(ShardingPropertiesConstant.METRICS_MILLISECONDS_PERIOD);
    String loggerName = shardingProperties.getValue(ShardingPropertiesConstant.METRICS_LOGGER_NAME);
    // build a static reporter (only need one)
    if (null == reporter) {
      synchronized (LOCK) {
        if (null == reporter) {
          reporter = Slf4jReporter.forRegistry(METRIC_REGISTRY)
              .outputTo(LoggerFactory.getLogger(loggerName))
              .convertRatesTo(TimeUnit.SECONDS)
              .convertDurationsTo(TimeUnit.MILLISECONDS)
              .withLoggingLevel(Slf4jReporter.LoggingLevel.DEBUG)
              .build();
          reporter.start(period, TimeUnit.MILLISECONDS);
        }
      }
    }
  }

  /**
   * 开始计时.
   *
   * @param name 度量目标名称
   * @return 计时上下文
   */
  public static Timer.Context start(final String name) {
    return METRIC_REGISTRY.timer(MetricRegistry.name(name)).time();
  }

  /**
   * 停止计时.
   *
   * @param context 计时上下文
   */
  public static void stop(final Timer.Context context) {
    if (null != context) {
      context.stop();
    }
  }

  /**
   * 清理数据.
   */
  public static void clear() {
    // do nothing
  }
}
