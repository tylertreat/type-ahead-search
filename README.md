# type-ahead-search

Toy type-ahead search implementation using a ternary search tree.

## Build

This project uses Maven for managing builds and testing. First, ensure
[Maven](https://maven.apache.org/) is installed.

To run the unit tests and build the executable JAR, run the following from the
root project directory:

```
$ mvn clean install
```

This will produce an executable JAR in the `target` directory, which can then
be run with the following:

```
$ java -jar target/type-ahead-search-1.0-SNAPSHOT-jar-with-dependencies.jar
```

You can also execute the compiled class files, located in `target/classes`,
using the following:

```
$ java -cp target/classes Main
```

Clean up artifacts with the following command:

```
$ mvn clean
```
