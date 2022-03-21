## Generate examples using shardingsphere-example-generator

shardingsphere-example-generator is an example code generation module based on the freemarker template engine. Data and template can generate the required code examples.

#### Template

shardingsphere-example-generator has defined relevant templates, users do not need to pay attention when using them. The relevant templates are in the template folder under the `resources` directory. If there are any errors, please correct them.

#### Data

Data is the core of template generation. Based on data, we inject soul into our template and generate running code. The specific data model is as follows:

| property            | Description        | Reference                                                                                                                         |
|:--------------------|--------------------|:----------------------------------------------------------------------------------------------------------------------------------|
| product             | product            | jdbc、proxy                                                                                                                        |
| mode                | operating mode     | memory、cluster-zookeeper、cluster-etcd、standalone-file                                                                             |
| transaction         | transaction type   | local                                                                                                                             |
| features            | feature set        | sharding、readwrite-splitting、encrypt、db-discovery                                                                                 |
| frameworks          | framework set      | jdbc、spring-boot-starter-jdbc、spring-boot-starter-jpa、spring-boot-starter-mybatis、spring-namespace-jpa、spring-namespace-mybatis   |
| host                | database host      | localhost                                                                                                                         |
| port                | database port      | 3306                                                                                                                              |
| username            | database username  | root                                                                                                                              |
| password            | database password  | root                                                                                                                              |

#### Steps

1、Configure the data model

In the resource directory of the `shardingsphere-example-generator` module, configure the `data-model.yaml` file.

2、Generate code

Run `ExampleGeneratorMain` to generate the corresponding module code in the `target/generated-sources/shardingsphere-${product}-sample` directory.

3、Compile

Mark the generated code as a maven project, and in the relevant directory file, run `mvn clean compile`.

4、Run

Run the `Example#main` method in the java directory of the generated module to run the relevant code examples.