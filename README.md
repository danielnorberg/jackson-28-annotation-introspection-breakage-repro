Jackson 2.8.8 Annotation Introspection Issue Repro
==================================================

A repro of an annotation introspection issue in jackson 2.8.8.


* Jackson 2.8.8:

```
mvn -Djackson.version=2.8.8 compile exec:java -Dexec.mainClass=Repro
```

```
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building jackson-28-annotation-introspection-breakage-repro 1.0-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO]
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ jackson-28-annotation-introspection-breakage-repro ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dano/projects/jackson-28-annotation-introspection-breakage-repro/src/main/resources
[INFO]
[INFO] --- maven-compiler-plugin:3.6.2:compile (default-compile) @ jackson-28-annotation-introspection-breakage-repro ---
[INFO] Nothing to compile - all classes are up to date
[INFO]
[INFO] --- exec-maven-plugin:1.6.0:java (default-cli) @ jackson-28-annotation-introspection-breakage-repro ---
[WARNING]
com.fasterxml.jackson.databind.JsonMappingException: Can not construct instance of Repro$FoobarImpl: no suitable constructor found, can not deserialize from Object value (missing default constructor or creator, or perhaps need to add/enable type information?)
 at [Source: {"bar":"bar", "foo":"foo"}; line: 1, column: 2]
	at com.fasterxml.jackson.databind.JsonMappingException.from(JsonMappingException.java:270)
	at com.fasterxml.jackson.databind.DeserializationContext.instantiationException(DeserializationContext.java:1456)
	at com.fasterxml.jackson.databind.DeserializationContext.handleMissingInstantiator(DeserializationContext.java:1012)
	at com.fasterxml.jackson.databind.deser.BeanDeserializerBase.deserializeFromObjectUsingNonDefault(BeanDeserializerBase.java:1206)
	at com.fasterxml.jackson.databind.deser.BeanDeserializer.deserializeFromObject(BeanDeserializer.java:314)
	at com.fasterxml.jackson.databind.deser.BeanDeserializer.deserialize(BeanDeserializer.java:148)
	at com.fasterxml.jackson.databind.ObjectMapper._readMapAndClose(ObjectMapper.java:3798)
	at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.java:2842)
	at Repro.main(Repro.java:167)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.codehaus.mojo.exec.ExecJavaMojo$1.run(ExecJavaMojo.java:282)
	at java.lang.Thread.run(Thread.java:745)
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 1.559 s
[INFO] Finished at: 2017-08-29T21:59:09+02:00
[INFO] Final Memory: 13M/300M
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.codehaus.mojo:exec-maven-plugin:1.6.0:java (default-cli) on project jackson-28-annotation-introspection-breakage-repro: An exception occured while executing the Java class. Can not construct instance of Repro$FoobarImpl: no suitable constructor found, can not deserialize from Object value (missing default constructor or creator, or perhaps need to add/enable type information?)
[ERROR]  at [Source: {"bar":"bar", "foo":"foo"}; line: 1, column: 2]
[ERROR] -> [Help 1]
[ERROR]
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR]
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoExecutionException
```

* Jackson 2.8.6:

```
mvn -Djackson.version=2.8.6 compile exec:java -Dexec.mainClass=Repro
```

```
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building jackson-28-annotation-introspection-breakage-repro 1.0-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO]
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ jackson-28-annotation-introspection-breakage-repro ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dano/projects/jackson-28-annotation-introspection-breakage-repro/src/main/resources
[INFO]
[INFO] --- maven-compiler-plugin:3.6.2:compile (default-compile) @ jackson-28-annotation-introspection-breakage-repro ---
[INFO] Nothing to compile - all classes are up to date
[INFO]
[INFO] --- exec-maven-plugin:1.6.0:java (default-cli) @ jackson-28-annotation-introspection-breakage-repro ---
FoobarImpl{foo='foo', bar='bar'}
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 1.507 s
[INFO] Finished at: 2017-08-29T21:59:35+02:00
[INFO] Final Memory: 11M/210M
[INFO] ------------------------------------------------------------------------
```


# `jackson-databind` bisect

* b30845f71 (2.8.7): Fail
* 749edaaa0: Fail
* 997261885: \<build failure\>
* 0f9a4a5f2: Pass
* 95f1df137: Fail

Culprits: [997261885](https://github.com/FasterXML/jackson-databind/commit/997261885) +
          [95f1df137](https://github.com/FasterXML/jackson-databind/commit/95f1df137)

