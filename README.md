Translator
----------

Team Sea Lions:
 - William Shi
 - Ricardo Guntur
 - Huynh Nguyen
 - Arthur Chang
 - Evan Johnson

Currently, the translator supports methods including println(), field declarations, simple functions, as well as
inheritance and method overriding.

To run the translator, clone the repository to your directory of choice, and then run the sbt command in your terminal.

Once inside sbt, you can use the command runxtc along with the following options

-printPhase1Ast
-printPhase2Ast
-makeCppHeader
-printPhase4Ast
-printPhase5
-translate

along with the location of the file you wish to translate. There are test files in the project, and the translator
currently supports up to Test005.java.

An example if you wish to translate end to end from Java to C++ on Test005.java:

```runxtc -translate src/test/java/inputs/Test005/Test005.java ```

To compare output of java and c++ translation run test_output.command.
To allow permissions for test_output.command, navigate to the directory through the terminal and use the command ```chmod u+x test_output.command```
You only need to run java_compile.command once to compile all inputs.  Only needs to run again when new inputs are added.
After each major change, run translate_inputs.command to test if the translator outputs correctly.

Project Map
-----------
```
├── README.md
│
├── build.sbt (managed library dependencies and c++ compilation configuration)
│
├── .sbtrc (like bash aliases but for sbt)
│
├── .gitignore (prevent certain files from being commmited to the git repo)
│
├── lib (unmanaged library dependencies, like xtc and its source) 
│
├── logs (logger output)
│   └── xtc.log 
│
├── output (target c++ source & supporting java_lang library)
│   ├── java_lang.cpp
│   ├── java_lang.h
│   ├── main.cpp
│   ├── output.cpp
│   └── output.h
│
├── project (sbt configuration, shouldn't need to be touched)
│
├── schema (ast schema & examples)
│   ├── cpp.ast
│   └── inheritance.ast
│
└── src 
    ├── main
    │   ├── java
    │   │   └── edu (translator source code)
    │   └── resources
    │       └── xtc.properties (translator properties file)
    └── test
        └── java
            ├── edu (translator unit tests)
            └── inputs (translator test inputs)
```
