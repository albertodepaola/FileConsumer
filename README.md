# FileConsumer
Simple java app to watch a directory, process its contents and parses the configured files with [LogParser](https://github.com/albertodepaola/logparser).

# Installation
To run the code [java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) is required. To build it, [ant](http://ant.apache.org/) is used.

With Ant installed, clone the repository, and execute the ant build:
```bash
 git clone https://github.com/albertodepaola/FileConsumer.git FileConsumer;
 cd FileConsumer;
 ant build-jar
 ```
 
 The resulting jar file, located in dist/FileConsumer.jar, is ready for use with a simple command line utility or to be embedded in another java application.

# License
The content of this repository is licensed under a [Creative Commons Attribution License](https://creativecommons.org/licenses/by/3.0/us/) 
