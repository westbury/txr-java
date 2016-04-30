# txr-java
An implementation of the TXR text matching language as a library for the JVM

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
    ./gradlew installApp         (on Linux / Mac OS)
    gradlew installApp           (on Windows)
    
    To use Eclipse for developing txr-java, first build the Eclipse project files
    (.project etc):
    
    ./gradlew eclipse         (on Linux / Mac OS)
    gradlew eclipse           (on Windows)
    
    Note that this project has just started so you will have quite a bit to add before it will be usable for you.
    
