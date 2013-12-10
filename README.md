# The Diio Hamcrest-SQL Library

## Overview

The hamcrest-sql library is a Java library used to match SQL statements via hamcrest APIs. The library's matchers can match either entire SQL statements or individual clauses.

This library depends on the [FoundationDB SQL Parser library](https://github.com/FoundationDB/sql-parser) to parse SQL statements. The FoundationDB SQL Parser is itself derived from the Apache Derby parser.

## Building from source

[Maven](http://maven.apache.org) is used to build, test and deploy the library.

Run tests and build jars:

```sh
$ mvn package
```

The resulting jar files are in `target/`.

Generate the documentation:

```sh
$ mvn javadoc:javadoc
```

The resulting HTML files are in `target/site/apidocs/`.

To install the jar file into your local repository for reuse by other Maven projects:

```sh
$ mvn install
```

## Using From Maven

The hamcrest-sql library is not currently in the standard Maven Central repository. Accordingly,
it should be installed locally via ```mvn install``` as per the "Building from source" section above. Another option
for making the library accessible for maven is to install the jar into a 3rd party maven repository, such as Nexus.

The appropriate Maven `pom.xml` entry for the library is:

```xml
<dependencies>
  <dependency>
    <groupId>com.diio</groupId>
    <artifactId>hamcrest-sql</artifactId>
    <version>0.1</version>
  </dependency>
</dependencies>
```

## Working With hamcrest-sql

The hamcrest-sql library combines the power and readability of hamcrest-style assertions with the robustness of a production SQL parser. This should result in more reliable tests against the contents of SQL statements than the use of built-in JUnit assertions and substring matching.

### A simple example

```java
package com.diio.query.matcher;

import static com.diio.query.matcher.AggregateMatcher.aggregate;
import static com.diio.query.matcher.ColumnMatcher.column;
import static com.diio.query.matcher.QueryHasMatcher.hasInQuery;
import static com.diio.query.matcher.UnderNodeMatcher.result;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.StatementNode;

public class SimpleExampleTest {

    @Test
    public void matchJustAggregateFunction() throws StandardException {
        String simpleSql = "SELECT foo, SUM(bar) FROM MyTable";
        StatementNode statement = new SQLParser().parseStatement(simpleSql);
        assertThat(statement, hasInQuery(result(aggregate("SUM", column("bar")))));
    }    
}
```

## License

Apache License, Version 2.0
Copyright (c) 2013 Diio, LLC  
It is free software and may be redistributed under the terms specified
in the LICENSE.txt and NOTICE files.
