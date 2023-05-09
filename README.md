Platinum
========

Swing based GUI Toolkit containing the new VBrowser 2.0 API.
From 2013 to 2015 this was the follow-up from the VL-e's VBrowser project.

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

