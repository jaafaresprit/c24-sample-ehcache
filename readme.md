# C24 and Ehcache

This is a sample project that shows the storage and querying capabilities in ehcache using java objects serialised in C24-io's binary format (SDO).

# Get the sample project

The project is available from the C24 public Git repository

To clone it:

``` git clone https://github.com/C24-Technologies/c24-sample-ehcache ```

# Build the project with Maven

To build the project execute the following command from the root directory:

``` mvn clean install ```

## The maven build file (pom.xml)

The build file relies on the C24 public repository for the required library dependencies

    <repositories>
        <repository>
            <id>c24-nexus</id>
            <name>C24 Nexus</name>
            <url>http://repo.c24io.net/nexus/content/groups/public</url>
        </repository>
    </repositories>
    
The dependencies are on Ehcache and C24 runtime libraries

        <dependency>
            <groupId>biz.c24.io</groupId>
            <artifactId>c24-io-api</artifactId>
            <version>${c24.io.api.version}</version>
        </dependency>

        <dependency>
            <groupId>net.sf.ehcache</groupId>
            <artifactId>ehcache</artifactId>
            <version>2.8.2</version>
        </dependency>
        
The C24 maven plugin will generate the java bindings from the Xml Schema file in src/main/c24

            <plugin>
                <groupId>biz.c24.io.maven</groupId>
                <artifactId>c24-maven-plugin</artifactId>
                <version>${c24.io.maven.version}</version>
                .........
                ........
            </plugin>
            
## The Data model

The sample data model is in src/main/c24
It is a schema that represents the data structure for a Legal Entity Identifier used throughout financial institutions.
As part of the maven build process, using the C24 maven plugin, java classes will be generated from this schema and written to target/generated-sources.
Sdo classes will also be generated due to the configuration property being set:

                                    <deployAsSDO>true</deployAsSDO>

The data used for the tests is derived from the lei_sample.xml file in src/main/resources
It will be used as a template for adding object instances to the cache.

## Cdo Capacity Test

The purpose of this test is to show the capacity of the cache when it is filled with object instances that are just POJOs wrapped in C24 Complex Data Object instances.
First the sample instance is parsed using the C24 runtime fluent API:

``` LEIDirectory cdo = C24.parse(LEIDirectory.class).from(new File("/lei_sample.xml")); ```

Next we iterate until memory is exhausted placing instances in the cache after first generating unique ids for each instance:

```
    for(i = 0; i < Integer.MAX_VALUE; i++) {
                   cache.put(new Element(i, Utils.cloneWithIdentifier(cdo.getLEIRegistrations().getLEIRegistration(0), i)));
```

You can run the test in the IDE of your choice.

To run it from the command line execute the following command:

``` mvn exec:java -Dexec.mainClass="biz.c24.io.demo.ehcache.CdoCapacityTest" ```

## Sdo Capacity Test

This is similar to the Cdo Capacity test except that instead of putting POJOs in the cache we put the Sdo instances in the cache. 
The LeiDirectory class is in a sdo sub package org.plei.prelei_schema.xsd.sdo.LEIDirectory and instances are generated in the same way:

``` org.plei.prelei_schema.xsd.sdo.LEIDirectory sdo = C24.parse(org.plei.prelei_schema.xsd.sdo.LEIDirectory.class).from(new File("/lei_sample.xml")); ```

The Sdo instance has the same getters as the Cdo instance and can be treated the same as a POJO but is a LOT smaller than the Cdo instance.
The test iterates until memory is exhausted, as in the above test, placing Sdo instances in the cache after generating unique ids for each one:

```
for(i = 0; i < Integer.MAX_VALUE; i++) {
                cache.put(new Element(i, Utils.cloneSdoWithIdentifier(sdo.getLEIRegistrations().getLEIRegistration(0), i)));
```

To run it from the command line execute the following command:

``` mvn exec:java -Dexec.mainClass="biz.c24.io.demo.ehcache.SdoCapacityTest" ```

You may find it helpful to increase the heap size before running the capacity tests:

``` MAVEN_OPTS=-Xmx4G mvn exec:java -Dexec.mainClass="biz.c24.io.demo.ehcache.SdoCapacityTest" ```

## Sdo Query Test

To demonstrate that Sdo instances can be queried in the cache as normal POJOs this test places a number of instances in the cache and then executes queries to find single instances.

First the template instance is loaded:

``` org.plei.prelei_schema.xsd.sdo.LEIDirectory sdo = C24.parse(org.plei.prelei_schema.xsd.sdo.LEIDirectory.class).from(new File("/lei_sample.xml")); ```

Next a million instances are created and cached (each one with a unique id):

```
    for(i = 0; i < QUERY_SIZE; i++) {
                cache.put(new Element(i, Utils.cloneSdoWithIdentifier(sdo.getLEIRegistrations().getLEIRegistration(0), i)));
                
```

The time taken to complete 10 queries is calculated:

```
            long time = System.currentTimeMillis();
            for(i = 0; i < 10; i++) {
                Results results = cache.createQuery().includeValues().addCriteria(identifier.eq("0000000000000000099" + i)).execute();
                if(results.size() != 1) {
                    throw new RuntimeException();
                }
            }
            time = System.currentTimeMillis() - time;
            System.out.println("10 in " + time);
```

# Links

* [C24 IO] (c24.io)
* [C24 Git Hub Repository] (https://github.com/C24-Technologies)
* [C24 white paper on Sdos and Ehcache] (https://ref.c24.biz/whitepapers/C24-SDOs-and-Ehcache.pdf)
