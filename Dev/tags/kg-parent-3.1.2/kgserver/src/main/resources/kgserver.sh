#!/bin/bash
java -Xmx1024m -cp kgserver-1.0.7-jar-with-dependencies.jar fr.inria.edelweiss.kgramserver.webservice.EmbeddedJettyServer $@
