+++
title = "ElasticJob 3.0.2 is released including failover optimization, scheduling stability, and Java 19 compatibility"
weight = 78
chapter = true 
+++

![img](https://shardingsphere.apache.org/blog/img/2022_10_27_ElasticJob_3.0.2_is_released_with_failover_optimization,_scheduling_stability,_and_Java_19_compatibility1.png)

[ElasticJob](https://shardingsphere.apache.org/elasticjob), one of the sub-projects of the [Apache ShardingSphere](https://github.com/apache/shardingsphere) community, is a distributed scheduling solution oriented towards Internet applications and massive tasks.

Since ElasticJob 3.0.1 was released, we’ve received a lot of feedback from users on [GitHub](https://github.com/apache/shardingsphere-elasticjob). After merging the code committed by the contributors, ElasticJob has been significantly optimized.

The [resulting ElasticJob 3.0.2](https://github.com/apache/shardingsphere-elasticjob/blob/master/RELEASE-NOTES.md) has been improved in terms of **Failover, scheduling stability, job API, and Java 19 compatibility.**

# Release 3.0.2 Key Features

## Failover Optimization

Failover is an important ElasticJob feature.

**In ElasticJob 3.0.1, the Failover feature had some limitations and disadvantages:**

- Failover could take effect with at least two job instances. Assume that there’s only one job instance executing tasks and a fault occurs, Failover cannot take effect even if the instance recovers or a new job instance is started after the fault occurs. In this case, the old Failover feature required redundant resources. Particularly, if sharding was not needed for the job, the backup instance would be idle for a long time and continuously occupy resources.
- Failover was sensitive. Even if the job was not in execution, as long as one instance goes offline, failover of another instance would still be triggered to immediately execute the sharding owned by the offline instance. In other words, failover was triggered by the offline instance whether it is a normal offline or a fault occurs, which is disturbing for users.

**ElasticJob 3.0.2 optimized the Failover feature.**

- Suppose a single job instance is executing the tasks and it exits due to a fault in the execution process. **After a fault occurs, a new job instance is started, which can detect the failed sharding and trigger Failover immediately.** For example, we deploy a job instance with one copy in the Kubernetes environment. If the job instance exits unexpectedly due to a fault, Kubernetes automatically starts a new job instance. By this time, Failover will be triggered and continue to execute the previously failed job.
- **The optimized Failover takes effect only for ongoing jobs.** If no job is being executed when a fault occurs, Failover is not triggered. In this case, Failover will not be triggered when the job instance goes offline normally.

## Separating the event notification threads of different jobs to avoid an endless loop.

ElasticJob achieves distributed coordination through [ZooKeeper](https://zookeeper.apache.org/). In practical scenarios, users may start multiple jobs in the same project simultaneously, all of which use the same [Apache Curator](https://curator.apache.org/) client. There are certain risks due to the nature of ZooKeeper and the [callback method of Curator in a single event thread](https://cwiki.apache.org/confluence/display/CURATOR/TN1).

- Callbacks will increase accordingly if a huge number of jobs are triggered frequently. The processing capability of Curator event threads may reach an upper limit, resulting in delayed triggering or failure to trigger.
- If one job blocks the event thread accidentally, other jobs will also be affected, leading to delayed triggering or failure to trigger.

**In ElasticJob 3.0.2, the callback method of each job will be executed on the thread to which the job belongs, preventing jobs from affecting each other.**

Moreover, **ElasticJob 3.0.2 modified the code which may lead to an endless loop.** When a job instance is online and the server nodes in ZooKeeper change (such as being deleted), ElasticJob is stuck in determining whether the server is enabled. This problem has been tackled.

## Java 19 Support

Java 19 was released on September 20, 2022. ElasticJob’s code closely follows Java versions and currently can be built and used through Java 19. **Elasticjob now supports Java 8 through 19.**

# Release Notes

## Bug Fixes

- Fix itemErrorMessages not cleared after the job finished.
- Fix Curator notify thread may be blocked and avoid probably endless loop in ServerService.
- Fix the problem that NPE may occur in the deserialization of job instance ID and job configuration.
- Fix failover to sensitive.

## Enhancements

- Script Job exception’s stack was ignored.
- Support using different event trace data sources when using Spring Boot.
- Supports building projects with Java 19.

Please refer to [GitHub ElasticJob Milestone](https://github.com/apache/shardingsphere-elasticjob/milestone/5?closed=1) for details.

# Useful Links

**🔗** [**ElasticJob Milestone**](https://github.com/apache/shardingsphere-elasticjob/milestone/5?closed=1)

**🔗** [**Release Notes**](https://github.com/apache/shardingsphere-elasticjob/blob/master/RELEASE-NOTES.md)

**🔗** [**ShardingSphere ElasticJob GitHub Address**](https://github.com/apache/shardingsphere-elasticjob)

**🔗** [**ShardingSphere Project Address**](https://github.com/apache/shardingsphere)

**🔗** [**ShardingSphere ElasticJob Official Website**](https://shardingsphere.apache.org/elasticjob)

# Author

Wu Weijie, is an infrastructure R&D engineer at [SphereEx](https://www.sphere-ex.com/), and an Apache ShardingSphere PMC.

He now focuses on the R&D of the Apache ShardingSphere access port and the sub-project ElasticJob.
