Platinum
========

Swing based GUI Toolkit Contains the new 2.0 VBrowser API.

See here for the Wiki:

- https://github.com/NLeSC/Platinum/wiki

Java 11 notice
---
This project has been upgraded to a Java 11 project.


Build
---
Default maven build:

    mvn clean package verify

After build, see the assembled distribution in module `ptk-zipdist` :

    ptk-zipdist/target/ptk-zipdist-${VERSION}-dist.zip

Start
---
Start the (default) vbrowser.sh from the `bin` directory:

    ${DIST}/bin/vbrowser.sh
