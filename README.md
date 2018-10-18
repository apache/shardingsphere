# sharding-sphere-example

Sharding-Sphere example.

Example for 1.x please see tags in `https://github.com/sharding-sphere/sharding-sphere/tree/${tag}/sharding-jdbc-example`

Example for 2.x or 3.x please see tags in `https://github.com/sharding-sphere/sharding-sphere-example/tree/${tag}`

Please do not use `dev` branch to run your example, example of `dev` branch is not released yet. 

The manual schema initial script is in `https://github.com/sharding-sphere/sharding-sphere-example/blob/dev/src/resources/manual_schema.sql`, 
please execute it before you first run the example.

Please make sure master-slave data sync on MySQL is running correctly. Otherwise this example will query empty data from slave.
