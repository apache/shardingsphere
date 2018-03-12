+++
toc = true
title = "Contribute Code"
weight = 4
prev = "/00-overview/news/"
next = "/01-start/"

+++

You can report bugs, submit a new feature enhancement recommendation, or submit an improved patch directly to the above.

## Reporting Bugs

This section guides you through submitting a bug report for Sharding-JDBC.

### Before Submitting A Bug Report

 - Please make sure this is not a repeated bug.
   Check [Issue Page](https://github.com/shardingjdbc/sharding-jdbc/issues) list，search if the bug you want to submit has been reported.
 - Please make sure the bug exists in the latest version.We will not continue to maintain all release versions, and all changes are based only on the current version.
 - Please make sure the bug can be reproduced，Please try to provide a complete reproduction step，and provide a project demo code that can be duplicated on github.
 

### How Do I Submit A (Good) Bug Report?

Bugs are tracked as [Issue Page](https://github.com/shardingjdbc/sharding-jdbc/issues).

 - Use a clear and descriptive title for the issue to identify the problem.
 - A detailed description of the steps for a recurring bug. This includes the SQL you use, the configuration, the expected results, and the actual results. Additional TRACE logs are attached.
 - In GitHub, the project demo code is provided for the reoccurrence of the problem.
 - If the program throws an exception, attach the complete stack log.
 - If possible,include screenshots and animated GIFs which show you following the described steps and clearly demonstrate the problem.
 - If performance issues are involved, attach a Profile screenshot of the CPU, memory, or network disk IO.
 - Explain the applicable version, only the release version of the bug can be submitted, and should be the latest version.
 - What's the name and version of the OS you're using
 - Use the bug Label (Label) to mark the issue.

The following is the markdown template for bug,please fill in the issue according to the template.

```
[[Simple description of the problem]

**Problem reiteration steps:**

1. [The first step]]
2. [The Second step]
3. [Other steps...]

**Expected performance:**

[Please describe expected performance here]

**Observed performance:**

[Please describe Observed performance here]

**Screen screenshot and animated GIFs**

![Screen screenshot and animated GIFs of reiteration steps](the url of picture)

**the version of Sharding-JDBC:** [please input the version of Sharding-JDBC]
**Operating system and version:** [please input the operating system and version]

```

## Suggesting Enhancements

This section guides you through submitting an enhancement suggestion for Sharding-JDBC.

### Before Submitting An Enhancement Suggestion
 
 - Please first check the [detailed list of features](/01-start/features/)。
 - Please make sure this is not a repeated feature enhancement recommendation.
   Check [Issue Page](https://github.com/shardingjdbc/sharding-jdbc/issues) list，to see if the enhancement has already been suggested. If it has, add a comment to the existing issue instead of opening a new one.

### How Do I Submit A (Good) Enhancement Suggestion?

Enhancement suggestions are tracked as [Issue Page](https://github.com/shardingjdbc/sharding-jdbc/issues).

 - Use a clear and descriptive title for the issue to identify the suggestion.
 - Provide a step-by-step description of the suggested enhancement in as many details as possible.
 - Explain why this enhancement would be useful to most Sharding-jdbc users.New functions should be widely applicable.
 - If possible，list similar features already available in other databases.Both commercial and open source software are available.
 - Use the enhancement Label (Label) to mark the issue.

The following is the markdown template for enhancement suggestion,please fill in the issue according to the template.

```
[Simple suggestion description]

**Proposed new functional behavior**

[Describe the behavior pattern of the new function]

**Why is this new feature useful to most users**

[Explain why is this new feature useful to most users]

[List whether other database middleware contains the function and how it is implemented]

```

## Contribute Patch

This section introduces development specifications, environments, samples, and documentation to contributors.

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
 
