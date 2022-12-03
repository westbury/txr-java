[![Gitpod Ready-to-Code](https://img.shields.io/badge/Gitpod-Ready--to--Code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/westbury/txr-java) 

# txr-java
An implementation of the TXR text matching language as a library for the JVM

[![Gitpod - code now](https://img.shields.io/badge/Gitpod-code%20now-blue.svg?longCache=true)](https://gitpod.io#https://github.com/westbury/txr-java)

TXR is a powerful language for extracting data from text documents.  It can be thought of as a pattern matcher but
it operates at a file level.  For example if you want to extract order information
from a confirmation e-mail sent to you by an online retailer then TXR is the tool for the job.  The documentation
and reference implementation for TXR is available at http://www.nongnu.org/txr/.

The reference implementation is written in C++ and is packaged as a command-line tool.  If you want to call TXR from
a Java program or other program running in the JVM then you must use JNI and it can be tricky and not very satisfactory
to pass in the input data and get output such as the extracted data.  To make it far easier to call TXR code from
the JVM an implementation is being written in Java.

This implementation will mostly follow the TXR standard.  It will differ in a few areas.  For example extracted data will
be returned in maps instead of being written to an output stream.

To checkout code and build:

    git clone https://github.com/westbury/txr-java
    cd txr-java        
    ./gradlew jar         (on Linux / Mac OS)
    gradlew jar           (on Windows)
    
    You will see that there are four projects here.
    
    1. txr-java
    
    This is the core TXR interpreter. It is a plain old Jar and does not include any files specific to the Eclipse IDE. If you are using Eclipse IDE then you can generate the files (.project etc) as follows:
    
    cd txr-java							(go into the second level folder of the same name)
    ./gradlew eclipse         (on Linux / Mac OS)
    gradlew eclipse           (on Windows)
    
    2. - txr-java-debug
    
        This is an Eclipse project. You may use this project in your Eclipse application if you wish to allow the user to open the TXR debugger as a view within your application.
        
    3. - txr-java-debug-3x
    
    This is an Eclipse project. You may use this project together with txr-java-debug if you wish to use the TXR Debug view in an Eclipse 3.x application.
    
    4. - txr-java-testframework
    
    An Eclipe E4 application that can be used as a standalone TXR debugger.
    
Although only a small part of the entire TXR specification has been implemented,
the features needed for decent extraction of data from typical multi-line text is all there.
You should have no problem extracting data from automated e-mail (e.g. order confirmation messages)
or text copied from web pages (ctrl-A, ctrl-C from the browser generally is easier and works better
than trying to extract from the HTML).

The following directives are supported:

@(collect)
@(maybe)
@(cases)
@(skip)
@(assert)
@(none)

Most of the parameters to these directives are not supported.

To use in your own product you will need to build the txr-java.jar and copy it into
your build.  No release to Maven Central has yet been made.

Currently features are being implemented only on an 'as required' basis.  If you need
a feature of the TXR specification just let us know and it may get implemented.  We are also
interested to know of anything where the matcher does not behave the same as the reference TXR matcher.

Work going forward: It would be nice if the bindings went directly into Java setter methods.
For example, pass a Java object into the matcher and, if the object has a setFoo(String) method
then @foo bindings will bind to the method.     
