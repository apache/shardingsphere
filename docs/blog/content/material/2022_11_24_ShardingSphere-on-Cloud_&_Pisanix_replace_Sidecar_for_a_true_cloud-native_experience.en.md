+++
title = "ShardingSphere-on-Cloud & Pisanix replace Sidecar for a true cloud-native experience"
weight = 81
chapter = true 
+++

# Background

For a while, many of our blog posts have shown that [**ShardingSphere**](https://shardingsphere.apache.org/) **consists of three independent products:** [**ShardingSphere-JDBC**](https://shardingsphere.apache.org/document/current/en/overview/#shardingsphere-jdbc)**,** [**ShardingSphere-Proxy**](https://shardingsphere.apache.org/document/current/en/quick-start/shardingsphere-proxy-quick-start/)**, and ShardingSphere-Sidecar.**

As ShardingSphere has become increasingly popular, JDBC and Proxy have been used in numerous production environments, but Sidecar's status has remaining as "**under planning"**. you may have noticed this on our GitHub `READ ME` or our website.

With cloud-native gaining momentum, many enterprises are choosing to use databases on the cloud or cloud native databases. This represents an excellent opportunity for ShardingSphere-Sidecar, which is positioned as a **cloud-native database proxy in** [**Kubernetes**](https://kubernetes.io/).

However, some of you may have noticed that any mention of Sidecar has disappeared from ShardingSphere's latest documentation. Has ShardingSphere-Sidecar been canceled? What will ShardingSphere's cloud native future look like? Here's what's coming.

# What's ShardingSphere's plan for the cloud?

## Stop the R&D of ShardingSphere-Sidecar

As far as ShardingSphere users are concerned, ShardingSphere-JDBC and ShardingSphere-Proxy can already meet most requirements. ShardingSphere-Sidecar's only difference from them is in the deployment form. JDBC and Proxy are functionally identical, but each has unique advantages.

ShardingSphere-JDBC is positioned as a lightweight Java framework that provides additional services at theJava JDBC layer. With the client connecting directly to the database, it provides services in the form of `jar` and requires no extra deployment and dependence. It can be viewed as an enhanced JDBC driver, fully compatible with JDBC and all kinds of [ORM](https://stackoverflow.com/questions/1279613/what-is-an-orm-how-does-it-work-and-how-should-i-use-one) frameworks. It's targeted at developers and boasts higher performance.

ShardingSphere-Proxy is a transparent database proxy, supporting heterogeneous languages through the implementation of database binary protocol. Currently, [MySQL](https://www.mysql.com/) and [PostgreSQL](https://www.postgresql.org/) protocols are provided. Nevertheless, Proxy adds a gateway between the database and the front-end application layer, which will partially lower the performance.

**Our community recommends users to adopt a hybrid deployment mode allowing JDBC and Proxy to complement each other, maximizing ShardingSphere's advantages in terms of performance, availability and heterogeneous support.**

![img](https://shardingsphere.apache.org/blog/img/2022_11_24_ShardingSphere-on-Cloud_&_Pisanix_replace_Sidecar_for_a_true_cloud-native_experience1.png)

As you can see, it's a bit awkward for ShardingSphere-Sidecar: JDBC and Proxy are applicable to most scenarios, environments and businesses, and can complement each other, leaving little room for Sidecar to innovate. From the viewpoint of the community and its users, Sidecar is more like an extension in deployment mode, and it is not capable of enhancing ShardingSphere as a whole.

**Taking the above factors into consideration, it is more efficient to develop a patch tool for ShardingSphere that can be easily used and run in a Kubernetes environment. This way, users can deploy and use ShardingSphere in cloud native environments, while saving R&D time for the ShardingSphere community.**

## ShardingSphere's cloud solution: [ShardingSphere-On-Cloud](https://github.com/apache/shardingsphere-on-cloud)

> ShardingSphere-on-Cloud is a comprehensive system upgrade based on ShardingSphere.

ShardingSphere-Sidecar was born when Kubernetes thrived. Back then, more and more enterprises were trying to adopt cloud-native concepts. The ShardingSphere community is not an exception.

We proposed ShardingSphere-Sidecar to promote cloud-native transformation in the data field. However, since JDBC and Proxy are mature enough to deal with data governance in most scenarios, it's unnecessary to make ShardingSphere entirely cloud native.

Sidecar can indeed play a big role in certain scenarios, but it doesn't mean that we have to create a Sidecar version for each component. ShardingSphere is working on how to come up with a solution based on real cloud-native scenarios after fully integrating the cloud computing concept. That's how ShardingSphere-on-Cloud was born.

[ShardingSphere-on-Cloud](https://github.com/apache/shardingsphere-on-cloud) is capable of deploying and migrating ShardingSphere in a Kubernetes environment. With the help of [AWS CloudFormation](https://aws.amazon.com/cloudformation/), [Helm](https://helm.sh/), Operator, and Terraform (coming soon) and other tools, it provides best practices with quick deployment, higher observability, security and migration, and high availability deployment in a cloud native environment.

Please refer to [***Database Plus Embracing the Cloud: ShardingSphere-on-Cloud Solution Released***](https://medium.com/codex/database-plus-embracing-the-cloud-shardingsphere-on-cloud-solution-released-29916290ad06?source=your_stories_page-------------------------------------) for details.

## Achieving the vision of ShardingSphere-Sidecar through [Pisanix](https://www.pisanix.io/)

**Why did we develop a new open source project oriented towards data governance in cloud native scenarios?**

Our community has been contemplating the position of ShardingSphere and [Database Mesh](https://medium.com/faun/database-mesh-2-0-database-governance-in-a-cloud-native-environment-ac24080349eb?source=your_stories_page-------------------------------------) concept.

Within the community we hold different viewpoints on Sidecar at different stages. In the beginning, the community wanted to use Sidecar to manage cloud data issues. As the community gained a deeper understanding of cloud native and cloud data management processes, the limitations of ShardingSphere-Sidecar have been gradually exposed.

ShardingSphere-Sidecar is only a deployment mode of ShardingSphere in cloud native environments, so it can only solve a single problem. It is incapable of helping ShardingSphere develop a mature and cloud-native solution for enterprises.

**Therefore, we needed to redesign an open source product with higher adaptability, availability and agility in a cloud native system - in order to make up for ShardingSphere's limitations on cloud data governance.**

That is why some of our community members at [SphereEx](https://www.sphere-ex.com/) developed [Pisanix, a cloud-native data governance tool](https://www.sphere-ex.com/news/43/), based on the Database Mesh concept. It can provide capabilities such as SQL-aware traffic governance, runtime resource-oriented management and DBRE.

## Is Pisanix the same with ShardingSphere-Sidecar?

ShardingSphere-Sidecar and Pisanix convey different understandings of Database Mesh. They are different in the following aspects.

- **Different design concepts:** the design philosophy of JDBC & Proxy is Database Plus, which adds an interface on top of multiple data sources for unified governance and enhancement. Pisanix represents the specific practice of Database Mesh concept, leading to efficient and smooth DevOps of databases for applications in cloud native scenarios.
- **Different language ecosystems:** JDBC is developed solely in Java. As Java is popular with numerous enterprise-grade community users and developers, JDBC can be easily applied to Java-developed applications. In comparison, Pisanix is developed in [Rust](https://www.rust-lang.org/) to improve the reliability and efficiency of the access layer.

Despite small differences, both of them are oriented towards cloud-native data infrastructure. That is also what Database Mesh expects in the long term by implementing cloud native DBRE.

In terms of deployment mode, Pisanix and ShardingSphere-Sidecar can both be deployed with business applications in the form of Sidecar, providing standard protocol access for developers. **Moreover, Pisanix is highly compatible with the ShardingSphere ecosystem. You can connect Pisanix to ShardingSphere-Proxy in the same way that ShardingSphere-Proxy connects to MySQL.**

In short, ShardingSphere presents a complete database form for developers and applications under the Database Plus concept. Pisanix is designed for the same purpose. Through Pisanix, the entrance to cloud data traffic, users can use ShardingSphere-Proxy as a database and explore the collaboration mode in a cloud native environment.

However, they belong to independent product lines. Pisanix followed the Database Mesh concept from the very beginning and achieved high-performance expansion through four aspects, including **local database, unified configuration and management, multi-protocol support and cloud native architecture**.

Pisanix is only the first step towards unifying database types on the cloud, and Sidecar is only a deployment form.

> The protocols and DevOps features of different databases vary, and the point lies in abstracting the standard governance behavior.

Unlike Pisanix, the ShardingSphere ecosystem is not only accessible through protocols, and ShardingSphere-JDBC can be used more conveniently by Java applications.

Its natural compatibility maintains functional integrity, optimizes resource utilization and provides ultimate performance, with which developers can configure data governance at their will from the perspective of businesses. Meanwhile, by combining ShardingSphere and the underlying databases, users can deploy strong computing capability on the application side and transform the original monolithic database into a distributed database with high performance, optimizing resource allocation and offering a cost-effective solution.

In conclusion, ShardingSphere and Pisanix together offer two solutions for community users. **For users who'd like to deploy ShardingSphere in a Kubernetes environment, ShardingSphere-on-Cloud is enough, and ShardingSphere's other features are exactly the same as when used locally.**

**For users looking to achieve unified traffic governance on the upper-layer database in cloud native scenarios, Pisanix is a better choice.**

Compared with ShardingSphere-Sidecar, ShardingSphere-on-Cloud combined with Pisanix is more effective and convenient.

[**ShardingSphere Official Website**](https://shardingsphere.apache.org/)

[**Database Mesh Official Website**](https://www.database-mesh.io/)

[**Pisanix Official Website**](https://www.pisanix.io/)
