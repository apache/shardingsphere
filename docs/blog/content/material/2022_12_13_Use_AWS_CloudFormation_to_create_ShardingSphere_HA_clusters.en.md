+++
title = "Use AWS CloudFormation to create ShardingSphere HA clusters"
weight = 83
chapter = true 

+++

# What's AWS CloudFormation?

[AWS CloudFormation](https://aws.amazon.com/cloudformation/) is an infrastructure as code (IaC) service that allows you to easily model, provision, and manage all the cloud services provided by [AWS](https://aws.amazon.com/) by custom templates.

Traditionally, when we create a new architecture, we need to manually configure it in AWS step by step which can cause some errors such forgetting some steps.

Now with CloudFormation, we can use declarative configuration to define resources and then create and manage them without worrying about the dependency order of the resources.

In real-world scenarios, we often need to create duplicate architectures. For example, we can build a set of MQ clusters in a pre-release environment whose architecture and production environment are consistent, and create an `AutoScalingGroup` with the same configuration in each availability zone (AZ).

Through CloudFormation, those repeated architectures can then be expressed in the form of code and stored in the code repository for management. This way, they can be easily integrated into existing CI/CD pipelines to achieve the change of infrastructure, in accordance with the current DevOps process. As a result, the infrastructure change is more transparent, repeatable, testable and auditable, simplifying the management of cloud systems.

# What's Apache ShardingSphere?

[Apache ShardingSphere](https://shardingsphere.apache.org/) is a distributed database ecosystem that can transform any database into a distributed database system, and enhance it with sharding, elastic scaling, encryption features & more.

Apache ShardingSphere follows the [Database Plus](https://medium.com/faun/whats-the-database-plus-concepand-what-challenges-can-it-solve-715920ba65aa?source=your_stories_page-------------------------------------) concept, designed to build an ecosystem on top of fragmented heterogeneous databases. It focuses on how to fully use the computing and storage capabilities of databases rather than creating a brand-new database.

[ShardingSphere-Proxy](https://shardingsphere.apache.org/document/current/en/overview/) is a transparent database proxy that supports any client that uses [MySQL](https://www.mysql.com/), [PostgreSQL](https://www.postgresql.org/) and [openGauss](https://opengauss.org/en/).

# Deploy with CloudFormation

As an important part of the data infrastructure, the ShardingSphere Proxy cluster is significant for high availability. For these reasons, our community hopes that you can manage it through IaC to enjoy the benefits brought by IaC.

In the following sections we'll use CloudFormation to create a ShardingSphere-Proxy cluster in multi-AZ. Before creating CloudFormation templates, we have to understand the architecture diagram of a ShardingSphere-Proxy cluster.

![img](https://shardingsphere.apache.org/blog/img/2022_12_13_Use_AWS_CloudFormation_to_create_ShardingSphere_HA_clusters1.png)

Please note that we use [Zookeeper](https://zookeeper.apache.org/) as the Governance Center.

As you can see, ShardingSphere-Proxy itself is a stateless application. In production scenarios, it only needs to offer a LoadBalancer, which can flexibly distribute the traffic between instances.

To ensure the HA of the ZooKeeper cluster and the ShardingSphere-Proxy cluster, we use the following architecture.

![img](https://shardingsphere.apache.org/blog/img/2022_12_13_Use_AWS_CloudFormation_to_create_ShardingSphere_HA_clusters2.png)

# Define CloudFormation Parameters

[The CloudFormation template](https://aws.amazon.com/cloudformation/resources/templates/?nc1=h_ls) is a yaml or json file in which you can define all of the infrastructure. [The CloudFormation parameter](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/parameters-section-structure.html) allows you to inject custom values into the templates. You can then [reference](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-ref.html) these parameters when defining resources. Of course, we also provide default values, which can be overridden if needed.

We use [cfndsl](https://github.com/cfndsl/cfndsl) to write CloudFormation templates: cfndsl is a tool written in [ruby](https://www.ruby-lang.org/en/), allowing you to generate CloudFormation templates through DSL configuration.

```ruby
Parameter("ZookeeperInstanceType") {
  String
  Default "t2.nano"
}

Parameter("ShardingSphereInstanceType") {
  String
  Default "t2.micro"
}

Parameter("KeyName") {
  String
  Default "test-tf"
  Description "The ssh keypair for remote connetcion"
}

Parameter("ImageId") {
  Type "AWS::EC2::Image::Id"
  Default "ami-094bbd9e922dc515d"
}

Parameter("VpcId") {
  String
  Default "vpc-0ef2b7440d3ade8d5"
  Description "The id of your VPC"
}

....
```

For more parameters, please refer to this [link](https://github.com/apache/shardingsphere-on-cloud/blob/main/cloudformation/multi-az/cf.rb#L21).

# Define CloudFormation Resources

Note that AWS's region usually includes more than two AZs, so we created three instances.

# Zookeeper Clusters

First, we deploy a Zookeeper EC2 instance in each AZ to ensure the HA of the ZooKeeper cluster.

Then we create an internal domain name for each Zookeeper instance, and ShardingSphere-Proxy uses these domain names.

When the ZooKeeer instance starts, cloud-init is used to automatically deploy the Zookeeper service. You can view the cloud-init configuration [here](https://github.com/apache/shardingsphere-on-cloud/blob/main/cloudformation/multi-az/zookeeper-cloud-init.yml).

```yaml
(0..2).each do |i| 
    name = "ZK#{i+1}"
    EC2_Instance(name) {
      AvailabilityZone FnSelect(i, FnGetAZs(Ref("AWS::Region")))
      InstanceType Ref("ZookeeperInstanceType")
      ImageId Ref("ImageId")
      KeyName Ref("KeyName")
      SubnetId FnSelect(i, Ref("Subnets"))
      SecurityGroupIds Ref("SecurityGroupIds")
      Tags [ 
        Tag do 
          Key "Name"
          Value "ZK-#{i+1}"
        end
      ]

      server = "server.%{idx}=zk-%{idx}.${HostedZoneName}:2888:3888"
      UserData FnBase64(
        FnSub(
          IO.read("./zookeeper-cloud-init.yml"), 
          :SERVERS => FnSub((0..2).map{|i| i == 0 ? server %{:idx => i+1} : ("#{server}" %{:idx => i+1}).insert(0, " " * 4)}.join("\n")), 
          :VERSION => Ref("ZookeeperVersion"),
          :ZK_HEAP => Ref("ZookeeperHeap"),
          :INDEX => i+1,
        )
      )
    }

    domain = "zone#{name}"
    Route53_RecordSet(domain) {
      HostedZoneId Ref("HostedZoneId")
      Name FnSub("zk-#{i+1}.${HostedZoneName}")
      Type "A"
      ResourceRecords [FnGetAtt(name, "PrivateIp")]
      TTL "60"
    }
  end
```

# ShardingSphere-Proxy Clusters

## LaunchTemplate

Next, we deploy an AutoScalingGroup in each AZ to ensure the HA of a ShardingSphere-Proxy cluster.

Before creating the AutoScalingGroup, we need to create a LaunchTemplate in each AZ for the ShardingSphere-Proxy instance.

Similarly, when the instance starts, cloud-init is used to automatically deploy the ShardingSphere-Proxy service. You can view the cloud-init configuration [here](https://github.com/apache/shardingsphere-on-cloud/blob/main/cloudformation/multi-az/shardingsphere-cloud-init.yml).

```yaml
(0..2).each do |i| 
    name = "launchtemplate#{i}"
    EC2_LaunchTemplate(name) {
      LaunchTemplateName FnSub("shardingsphere-${TMPL_NAME}", :TMPL_NAME => FnSelect(i, FnGetAZs(Ref('AWS::Region'))))
      LaunchTemplateData do 
        ImageId Ref("ImageId")
        InstanceType Ref("ShardingSphereInstanceType")
        KeyName Ref("KeyName")

        MetadataOptions do
          HttpEndpoint "enabled"
          HttpTokens   "required"
          InstanceMetadataTags "enabled"
        end

        Monitoring do
          Enabled  true
        end

        NetworkInterfaces [
          {
            :DeleteOnTermination => false,
            :DeviceIndex => 0,
            :NetworkInterfaceId => FnGetAtt("networkiface#{i}", "Id")
          }
        ]
        
        TagSpecifications [
          {
            :ResourceType => "instance",
            :Tags => [
              {
                :Key => "Name",
                :Value => "shardingsphere-#{i+1}"
              }
            ]
          }
        ]

        UserData FnBase64(
          FnSub(
            IO.read("./shardingsphere-cloud-init.yml"), 
            :ZK_SERVERS => FnSub((0..2).map{|i| "zk-#{i+1}.${HostedZoneName}:2181" }.join(",")), 
            :VERSION => Ref("ShardingSphereVersion"),
            :JAVA_MEM_OPTS => Ref("ShardingSphereJavaMemOpts")
          )
        )
      end
    }
  end
```

## TargetGroup

As we use [ELB](https://aws.amazon.com/elasticloadbalancing/) to load traffic between each ShardingSphere-Proxy, ELB should be used in combination with [TargetGroup](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-resource-elasticloadbalancingv2-targetgroup.html).

Instances created by AutoScalingGroup are automatically registered to TargetGroup. ELB will then forward the traffic to TargetGroup.

```yaml
ElasticLoadBalancingV2_TargetGroup("sslbtg") {
    Name "shardingsphere-lb-tg"
    Port Ref("ShardingSpherePort")
    Protocol "TCP"
    VpcId Ref("VpcId")
    TargetGroupAttributes [
      TargetGroupAttribute do
        Key "preserve_client_ip.enabled"
        Value "false"
      end
    ]
    Tags [
      Tag do
        Key "Name"
        Value "shardingsphere"
      end
    ]
  }
```

## AutoScalingGroup

After creating the aforementioned resources, now we can create an AutoScalingGroup.

```yaml
(0..2).each do |i| 
    name = "autoscaling#{i}"
    AutoScaling_AutoScalingGroup(name) {
      AutoScalingGroupName "shardingsphere-#{i}" 
      AvailabilityZones [FnSelect(i, FnGetAZs(Ref("AWS::Region")))]
      DesiredCapacity "1"
      MaxSize "1"
      MinSize "1"
      HealthCheckGracePeriod  60
      HealthCheckType "EC2"

      TargetGroupARNs [ Ref("sslbtg")]

      LaunchTemplate do
        LaunchTemplateName  FnSub("shardingsphere-${TMPL_NAME}", :TMPL_NAME => FnSelect(i, FnGetAZs(Ref('AWS::Region'))))
        Version FnGetAtt("launchtemplate#{i}", "LatestVersionNumber")
      end
    }
  end
```

## LoadBalancer & Listener

Create an internal LoadBalancer and Listener for the external service of the ShardingSphere-Proxy cluster.

```yaml
ElasticLoadBalancingV2_LoadBalancer("ssinternallb") {
    Name "shardingsphere-internal-lb"
    Scheme "internal"
    Type "network"
    
    mappings = (0..2).map { |x| 
        SubnetMapping do
          SubnetId FnSelect(x, Ref("Subnets"))
        end
    }
    SubnetMappings mappings
    Tags [
      Tag do
        Key "Name"
        Value "shardingsphere"
      end
    ]
  }
  
  ElasticLoadBalancingV2_Listener("sslblistener") {
    Port Ref("ShardingSpherePort")
    LoadBalancerArn Ref("ssinternallb")
    Protocol "TCP"
    DefaultActions [
      {
        :Type => "forward",
        :TargetGroupArn => Ref("sslbtg")
      }
    ]
  }
```

## Internal Domain Names

Finally, we create the internal domain names for the external service of the ShardingSphere-Prxoy cluster. Those domain names point to the internal LoadBalancer.

```yaml
Route53_RecordSet("ssinternaldomain") {
    HostedZoneId Ref("HostedZoneId")
    Name FnSub("proxy.${HostedZoneName}")
    Type "A"
    AliasTarget do 
      HostedZoneId FnGetAtt("ssinternallb", "CanonicalHostedZoneID")
      DNSName FnGetAtt("ssinternallb", "DNSName")
      EvaluateTargetHealth true
    end
  }
```

# Deployment

Use the command `cfndsl cf.rb -o cf.json --pretty` to generate the final [configuration](https://github.com/apache/shardingsphere-on-cloud/blob/main/cloudformation/multi-az/cf.json).

Create Stack on the UI page and select the config file we generated.

![img](https://shardingsphere.apache.org/blog/img/2022_12_13_Use_AWS_CloudFormation_to_create_ShardingSphere_HA_clusters3.png)

After a few minutes, you will find that all the resources have been created.

![img](https://shardingsphere.apache.org/blog/img/2022_12_13_Use_AWS_CloudFormation_to_create_ShardingSphere_HA_clusters4.png)

You can find the complete code [here](https://github.com/apache/shardingsphere-on-cloud/tree/main/cloudformation/multi-az), or visit our [website](https://shardingsphere.apache.org/oncloud/current/en/operation-guide/cloudformation-multi-az/) for more information.

# Test

The test is designed to ensure that the clusters we created are workable. A simple case is illustrated below.

Use DistSQL (Distributed SQL) to add two data sources and create a simple sharding rule, and then insert data. We can see that the query returns correct results.

By default, when we use CloudFormation, an internal domain name `proxy.shardingsphere.org` will be created. The username and password of the ShardingSphere-Proxy cluster are both `root`.

![img](https://shardingsphere.apache.org/blog/img/2022_12_13_Use_AWS_CloudFormation_to_create_ShardingSphere_HA_clusters5.png)

**Note:** [DistSQL (Distributed SQL)](https://shardingsphere.apache.org/document/current/en/user-manual/shardingsphere-proxy/distsql/) is ShardingSphere's SQL-like operating language. It's used the same way as standard SQL, and is designed to provide incremental SQL operation capability.

# Conclusion

AWS CloudFormation is an incredibly powerful service that is really helpful for the iteration of ShardingSphere-Proxy clusters. With this new addition, it is now easier than ever to get started with Apache ShardingSphere.
