JAVAC=javac
JAR=search.jar
MAIN=Main

clean:
	rm -f *.class
	rm -f $(JAR)

compile:
	$(JAVAC) *.java

jar: compile
	jar cvfe $(JAR) $(MAIN) *.class
