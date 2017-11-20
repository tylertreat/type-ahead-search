# type-ahead-search

Simple type-ahead search implementation.

## Build

There is a makefile included for building an executable JAR in the `src`
directory. The following command shows how to build and run the JAR:

```
$ cd src
$ make jar
$ java -jar search.jar
```

We can also just compile the classes and execute this way:

```
$ cd src
$ make compile
$ java Main
```

Clean up artifacts with the following command:

```
$ make clean
```
