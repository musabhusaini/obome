#!/bin/bash

rm -rf ~/.m2/repository/edu/sabanciuniv/dataMining/ontologyLearner
mvn install:install-file -Dfile=ontologyLearner.jar -DpomFile=ontologyLearner-pom.xml -Dsources=ontologyLearner-src.jar

rm -rf ~/.m2/repository/edu/sabanciuniv/dataMining/opinionMining
mvn install:install-file -Dfile=opinionMining.jar -DpomFile=opinionMining-pom.xml -Dsources=opinionMining-src.jar

rm -rf ~/.m2/repository/org/apache/lucene/lucene-hunspell
mvn install:install-file -Dfile=lucene-hunspell-0.2.jar -DgroupId=org.apache.lucene -DartifactId=lucene-hunspell -Dversion=0.2 -Dpackaging=jar

rm -rf ~/.m2/repository/org/owlapi/owlapi
mvn install:install-file -Dfile=owlapi-bin.jar -DgroupId=owlapi -DartifactId=owlapi -Dversion=3.2.4 -Dpackaging=jar

cd..
cd obomeApp
play mvn:up
