SrcMixins4J / Maven
===================

Maven Plugin for SrcMixins4J

Installation
------------

Add the following code to the "plugins" section of your Maven POM:

```xml
<plugin>
    <groupId>org.fuin.srcmixins4j</groupId>
    <artifactId>srcmixins4j-maven-plugin</artifactId>
    <version>0.2.0</version>
    <executions>
        <execution>
            <id>Process Mixins</id>
            <phase>process-sources</phase>
            <goals>
                <goal>process-mixins</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

If you use Eclipse M2E you'll have to add a configuration for the above plugin the the "pluginManagement" section.
