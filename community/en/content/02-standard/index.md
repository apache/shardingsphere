+++
date = "2018-03-05T16:06:17+08:00"
title = "How to contribute"
weight = 0
prev = "/00-organization/"
next = "/03-company/"
chapter = true
+++

# Development conventions


### Development Concept

 - Write code carefully to extract real non-functional requirements.
 - the code is neat and clean to the extreme，please refer to *Refactoring-Imporving Design of Existing Code* and *Clean Code*
 - Minimalist code, high reuse, no duplication of code and configuration
 - Code should be at the same level of abstraction.
 - When modifying the function, consider the influence surface more, and do not leave the incomplete part.
 - When there is only one requirement, there is no extensibility. When two similar requirements are used, the scalability is refined.

### Development Behavior Specification

 - Determine the module's test suite before submitting it, and use the test coverage tool to check that coverage cannot be lower than the master branch coverage.
 - Check the code with Checkstyle,violating the validation rules needs  special reasons.The template location is in sharding-jdbc/src/resources/dd_checks.xml.
 - mvn clean install can be tested and compiled
 - Delete unused code in time.
 
### Code Conventions

 - Look at the system's existing code before you write it, keeping the style consistent with the way you use it.
 - Variable naming is meaningful. If the method has only one return value, it uses result to name the return value. In the loop, each is used to name the loop variable, and entry is used instead of each in map.
 - The nested loop is refined as method as far as possible.
 - Replace Nested Conditional with Guard Clauses.
 - Configuration named by Camel-Case,the first letter of file is lowercase.
 - The access permissions for classes and methods are minimal, for example, if you can set it private, you don't need public.
 - The private method used in this method should follow this method closely,if there are multiple private methods,The private method should be written in the same order as the private method in the original method.
 - Use guava instead of apache Commons, for example: use Strings instead of StringUtils.
 - Use lombok instead of the constructor, get, set, and log methods.
 - use linux line feeds.
 - Indent (including blank lines) is consistent with the previous line.
 - There should be no meaningless space.
 - Method entry and return values are not allowed to be null, if there is a special case to be annotated.
 - The code that needs to be annotated is as small as possible, explained in the method name, and the annotations should contain only javadoc and todo, fixme, and so on.
 - static import is forbidden.
 - The No-public class should be put in the internal package,and the class in the package is private as far as possible.
 - All logs are in English.
 - Use the annotation to get the spring's business bean.
 - If there is a common entry point in the module, the pointcut package should be created at the module first level path.
 - Attribute configuration items need to be added to the constant enumeration of each module.

### Unit Test Specification

 - Test code and production code follow the same code specification.
 - If there is no special reason, the test needs to be fully covered.
 - Separation of code and test code for environment preparation.
 - Single data assert, should use assertTrue, assertFalse, assertNull, assertNotNull.
 - Multi data assert, should use assertThat.
 - Exact assert, Try not to use not, containsString assert.
 - The variable that invokes the business method., should named as actualXXX, The expected value should be called expectedXXX.
 - Only junit assertXXX, hamcrest, mocktio related can use static import.

### Compiling Code

The Sharding-JDBC code compilation requires [Maven](http://maven.apache.org/)，please ensure that it is properly configured in the IDE. All dependencies used in the code can be downloaded from the public network. Please choose a reasonable mirror according to your network situation.

The code uses [Lombok](https://projectlombok.org/download.html) to generate access methods for class attributes, constructors, and so on. Therefore, please use the above link content to get the appropriate solution for your IDE.

### Document Generation

The document USES the blog generation engine [HUGO](https://gohugo.io/)，please install the environment according to the document. All documents are in the directory sharding-jdbc/sharding-jdbc-doc/public.

### Contribution Method

Please follow the specification  to contribute code, examples and documents.

 - All the problems and new features are managed with [Issue Page](https://github.com/shardingjdbc/sharding-jdbc/issues).
 - Anyone wants to develop any function, please reply to the Issue associated with that function first, indicating that you are currently working on this Issue. And in response, set a deadline for yourself, and add in the reply content.
 - At the core contributors you should find a mentor(shepherd), who gives immediate feedback on design and functional implementation.
 - You should create a new branch to start your work, which is the name of the current dev/issue eid. The function name is determined after discussion with the shepherd.
 - When finished, send a pull request to the dev version of the current development of shardingjdbc, please do not submit PR to the master branch. Then the shepherd makes CodeReview, and then he will discuss some details with you (including design, implementation, performance, etc.). When all the team members are satisfied with this change, the mentor will submit the submission to the branch of the current development version.
 - Finally, congratulations that you have become the official contributor to Sharding-JDBC!
