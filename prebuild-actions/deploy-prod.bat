@echo off

call rmdir /S /Q %USERPROFILE%\.m2\repository\edu\sabanciuniv\dataMining\ontologyLearner
call mvn install:install-file -Dfile=ontologyLearner.jar -DpomFile=ontologyLearner-pom.xml -Dsources=ontologyLearner-src.jar

call rmdir /S /Q %USERPROFILE%\.m2\repository\edu\sabanciuniv\dataMining\opinionMining
call mvn install:install-file -Dfile=opinionMining.jar -DpomFile=opinionMining-pom.xml -Dsources=opinionMining-src.jar

call rmdir /S /Q %USERPROFILE%\.m2\repository\org\apache\lucene\lucene-hunspell
call mvn install:install-file -Dfile=lucene-hunspell-0.2.jar -DgroupId=org.apache.lucene -DartifactId=lucene-hunspell -Dversion=0.2 -Dpackaging=jar

call rmdir /S /Q %USERPROFILE%\.m2\repository\org\owlapi\owlapi
call mvn install:install-file -Dfile=owlapi-bin.jar -DgroupId=owlapi -DartifactId=owlapi -Dversion=3.2.4 -Dpackaging=jar

cd..
cd obomeApp
call play mvn:up