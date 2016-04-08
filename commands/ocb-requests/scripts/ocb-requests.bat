@ECHO OFF

CALL java -jar "%OCBTOOLS_HOME%bin\${project.artifactId}-${project.version}-jar-with-dependencies.jar" %*
