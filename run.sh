#!/bin/bash

# Compile the Java file
javac -cp ".:/Users/chetansmac/Desktop/UWindsor/COMP Advanced Computing Concepts/Assignment 1/selenium-java-4.35.0/*:/Users/chetansmac/Desktop/UWindsor/COMP Advanced Computing Concepts/Assignment 1/selenium-java-4.35.0/lib/*" HertzScraper.java

# Run the Java program
java -cp ".:/Users/chetansmac/Desktop/UWindsor/COMP Advanced Computing Concepts/Assignment 1/selenium-java-4.35.0/*:/Users/chetansmac/Desktop/UWindsor/COMP Advanced Computing Concepts/Assignment 1/selenium-java-4.35.0/lib/*" HertzScraper "$@"
