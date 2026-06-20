# README #

This repository contains samples demonstrating the use of the EasyGIMAPI Java interface for querying SMP/E.

## Requirements

- Java 17 or later
- GIMAPI load module available at runtime, i.e. it must run on z/OS

## General notes

- Development can be done off mainframe using a regular IDE, but you can't run/debug/test off mainframe.

## Samples

See [SAMPLES.md](./SAMPLES.md) for detailed descriptions of the included samples.

## Usage

The samples are all designed to be run using the Java single file source code feature, 
so a separate compile step is not required. You can simply edit/save/run the source code.

The jar file must be in the Java classpath. Either specify the classpath in the java command:
```
java -cp /home/andrewr/java/bhs-gimapi-0.9.1.jar ListEntry.java ...
```
or add it to the CLASSPATH environment variable.
```
export CLASSPATH=/home/andrewr/java/bhs-gimapi-0.9.1.jar
```

**Note:** Java 25 requires EBCDIC source files to be tagged e.g.:
```
chtag -t -c IBM1047 ListEntry.java
```
Git on z/OS will tag the java files in this repository automatically, however if you create new programs they need to be tagged manually.

## Dependencies

If you build this project using Maven, dependencies including the bhs-gimapi jar will be copied to the **target/lib** subdirectory.
```
mvn clean package
```

## JSON Generation

Samples are provided to demonstrate generation of JSON from SMP/E entries and information.

The JSON report samples require Jackson 3 on the classpath (`jackson-databind` and `jackson-core` 3.1.3, `jackson-annotations` 2.21). 

```
java -cp /home/andrewr/java/bhs-gimapi-0.9.1.jar:/home/andrewr/java/jackson-databind-3.1.3.jar:/home/andrewr/java/jackson-core-3.1.3.jar:/home/andrewr/java/jackson-annotations-2.21.jar MaintenanceLevel2Json.java ...
```

## Common errors

### error: no class declared in source file

This unhelpful message is issued when the source file is in EBCDIC but is not tagged. There are probably other causes, but this is the most confusing.

### WARNING: A restricted method in java.lang.System has been called
```
WARNING: A restricted method in java.lang.System has been called
WARNING: java.lang.System::load has been called by com.blackhillsoftware.gimapi.GimApi in an unnamed module (file:/home/andrewr/java/bhs-gimapi-0.9.1-SNAPSHOT.jar)
WARNING: Use --enable-native-access=ALL-UNNAMED to avoid a warning for callers in this module
WARNING: Restricted methods will be blocked in a future release unless native access is enabled
```
Recent versions of Java restrict usage of native access used to call GIMAPI. This is intended to make
sure the caller of the program knows when native access is being used.

 Add the --enable-native-access=ALL-UNNAMED option to the Java command, e.g.
```
java --enable-native-access=ALL-UNNAMED ListEntry.java ...
```

### Cannot find symbol
```
import com.blackhillsoftware.gimapi.SmpeQuery;
                                   ^
ListEntry.java:12: error: cannot find symbol
        var entries = SmpeQuery.csi(args[0])
                      ^
  symbol:   variable SmpeQuery
  location: class ListEntry
```
Either there is an error in the symbol name, or the bhs-gimapi jar can't be found at compile time. Check the CLASSPATH is set and refers to the correct name and path.

### java.lang.NoClassDefFoundError
```
Exception in thread "main" java.lang.NoClassDefFoundError: com.blackhillsoftware.gimapi.SmpeQuery
        at ListEntry.main(ListEntry.java:12)
Caused by: java.lang.ClassNotFoundException: com.blackhillsoftware.gimapi.SmpeQuery
        at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:754)
        at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:1042)
        ... 1 more
```
The bhs-gimapi jar can't be found at run time. Check the CLASSPATH is set and refers to the correct name and path.