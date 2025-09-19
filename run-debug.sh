#!/bin/bash

# Path to the selenium jars
SELENIUM_PATH="/Users/chetansmac/Desktop/UWindsor/COMP Advanced Computing Concepts/Assignment 1/selenium-java-4.35.0"

# Compile the Java file with debug info
echo "Compiling HertzScraper.java..."
javac -g -cp ".:$SELENIUM_PATH/*:$SELENIUM_PATH/lib/*" HertzScraper.java

if [ $? -eq 0 ]; then
  echo "Compilation successful. Running HertzScraper..."
  # Run the Java program with increased memory
  java -Xmx1024m -cp ".:$SELENIUM_PATH/*:$SELENIUM_PATH/lib/*" HertzScraper "$@"
else
  echo "Compilation failed."
fi
