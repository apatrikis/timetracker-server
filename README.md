# Time Tracker
Time Tracker is a self educational application for timesheet tracking. The application mainly has three parts:
- a JEE7 REST server, connecting to a database for storing users, projects and timesheets, as well as an WebSocket interface to notify registered clients about status changes.
- a common library with interfaces, entities and exceptions.
- a AngularJS web client that is consuming the servers's REST API and registers for status changes.

# Time Tracker Server
## Main desicions
- Create a REST-ful application which provides an easy accessible interface, so accessing clients are not restricted to a technololgy or programming language.
- Provide WebSockets to notify a web client about status changes.
- The server technology is JEE7.
- JEE7 capabilities are used to enforce access permissions.
- The database has to be accessed with JPA.
- It must be possible to run the sever in a cluster.
- All REST methods must be tested by some test framework, with the intention to achieve a high coverage of the sever code.
- Tests and artifact generation must be automated for Continouus Integration.

## Used software and versions
- Java 1.8 Update 31 (http://www.oracle.com/technetwork/java/index.html)
- IDE : Oracle NetBeans 8.0.2 (https://netbeans.org/downloads/, choose the `Java EE` edition)
- JEE7 application server : Glassfish 4.1 (https://glassfish.java.net/download.html)
- Database : Derby (included in NetBeans)  
  For setup of runtime environment download `lib` version 10.11.1.1 (http://db.apache.org/derby/derby_downloads.html)
- Testing : JUnit 4.11 (is specified in Maven POM)
- Testing : Arquillian (is specified in Maven POM)
- Dependency management : Maven 3.2.5 (http://maven.apache.org/download.cgi, not the version included in NetBeans)
- CI : Jenkins 1.609 (http://mirrors.jenkins-ci.org/war/1.609/jenkins.war)
- GIT : via GIT SCM (http://git-scm.com/downloads) or GitHub (https://github.com/)
- _Optional : SQirreL SQL Client (http://sourceforge.net/projects/squirrel-sql/files/)_

For the following descriptions the base installation directory `\time-tracker` is assumed.

## Development
The directory structure described below is like this:
```
\time-tracker
   \glassfish4
   \netbeans-8.0.2
   \timetracker-server
```

### Requirements
1. Install NetBeans IDE  
  With the NetBeans installation, Derby and Maven are automatically installed. In case other versions are needed they may be installed separately and configured in NetBeans.
1. Unpack the Glassfish application server and configure it in NetBeans
1. Download the `timetracker-server` project from https://github.com/apatrikis/timetracker-server

### Initial configuration
#### Configure Glassfish
The JEE7 application will use a datebase and deals with access permissions. These components need to be configured once.

1. Configure `DB Connection Pool` and `JDBC Ressource`
1. Configure `BASIC Authentication`

The `DB Connection Pool` and `JDBC Ressource` can be created with an resource file that comes with the scources. Glassfish must be up an running.
```
<<glasfish_home>>\bin\asadmin start-domain
<<glasfish_home>>\bin\asadmin --user admin --host localhost --port 4848 add-resources <<pathTo>>/timetracker-server/src/main/setup/glassfish-resources.xml
```

For editing the `BASIC Authentication`, open http://localhost:4747/.
In the navigation, go to `Configurations -> server-config -> Security -> Realms -> JDBC Realm` and add a new entry.
```
Name: timeTrackerRealm
JAAS Context: jdbcRealm (must be exactly this value)
JNDI: jdbc/datasourceTimeTracker
User Table: APP.EMPLOYEE
User Name Column: EMAIL
Password Column: PASSWORD
Group Table: APP.EMPLOYEE2ROLE
Group Table User Name Column: EMPLOYEE_EMAIL
Group Name Column: ROLENAME
Password Encryption Algorithm: SHA-256
Digest Algorithm: SHA-256
Encoding: Hex
Charset: UTF-8
```

#### Create database object
For development, the database tables can be created in two ways:

1. executing SQL scripts
1. implicit creation of the tables by editting the `persistence.xml` file to `drop-and-create`, so the tables will be created automatically during startup of the application server.

Option (2) will **always** create new tables - and drop old ones which may contain test data. Therefore, option (1) is preferred.
The files come with the sources: `\timetracker-server\src\main\setup\database\sql`

## Test
The directory structure will be extended:
```
\time-tracker
   \...
   \glassfish4-arquillian
```

### Requirements
1. Unpack another Glassfish application server that will be used exclusively for Arquillian Tests.

### Initial configuration
#### Configure Glassfish
After unpacking Glassfish, the server will read it's configuration file which by default will use the same ports as Glasfish instance used for development. Therefore, these setting have to be adjusted.

1. Open the configuration file `<<glasfish_home>>\glassfish\domains\domain1\config\domain.xml`
1. Go to section `/domain/configs/config[name=server-config]/admin-service/jmx-connector`
1. Add "10000" to the port
1. Go to section `/domain/configs/config[name=server-config]/network-config/network-listeners/network-listener`
1. Add "10000" to the HTTP, HTTPS and Admin port

Check the configuration by starting the serever `glassfish4-arquillian\bin\asadmin start-domain` and access the server (http://localhost:18080/, https://localhost:18181/ and http://localhost:14848)

As described for development, the `Security Realm` has to be configured (the values will be written into the `<<glasfish_home>>\glassfish\domains\domain1\config\domain.xml` file).

Also, the `DB Connection Pool` settings need to be configured. Again, the `glassfish-resources.xml` can be used. **Beause for testing we do not want to start the database manually (as we do not want for the application server) we have to change a setting after loading the configuration.** Open the administration console on http://localhost:14848 and edit the connection pool `Datasource Classname` from `org.apache.derby.jdbc.ClientDataSource` to `org.apache.derby.jdbc.EmbeddedDataSource`.

### Execution
The `JNDI JDBC Datasource` and `JDBC Ressources` will be installed with the initial Arquillian deployment. Therefore, the first deployment will fail.

Running an Arquillian test will automaticaly start up the Glasfish application server. 

**The database server needs to be up and running.** This may be achived by using the Database in embedded mode, or have the database already up and runing.
- The same database may be used by referenceing the common database location, e. g.  
`<<glasfish_home>>\bin\asadmin start-database --dbhome ~\.netbeans-derby`
- An own database instance may be started by using a dedicated port, e. g.  
`<<glasfish_home>>\bin\asadmin start-database --dbhome ../../javadb --dbport 11527`

## Build (CI)
The directory structure will be extended:
```
\time-tracker
   \...
   \glassfish4-ci
```

### Requirements
1. Unpack another Glassfish application server that will be used exclusively for CI.
1. Start the server, open the servers web administration console and deploy `Jenkins`.

Usually, a real life project will have it's own CI server.
For case of this demonstartion, we will install the CI application server on the same machine.

### Initial configuration
#### Configure Glassfish
For simplicity, we assume no development is taking place when the CI application server is running. Otherwise, we would have to assign dedicated ports to the Glasfish server, the same way we did for the Arquillian server.

#### Jenkins
The configuration of jenkins is related to
- the general configuration of application paths like `JDK`, `GIT` and `Maven`
- the project specific configuration, including
  - where to find the sources
  - which branches to build
  - dependencies to other builds
  - preconditions and postconditions

A documentation off all this options is quiet excessive, therefore three configuration files are provided as a configuration reference.
See : \timetracker-server\main\setup\jenkins

### Execution
Jenkis is runnig on http://localhost:8080/jenkins within the application server and is checking the repository for changes automaticaly.
In case of changes and appropriate setup, a build is triggered without the need of manual intervention.

Oppon successful build and test execution the created artifact usually would be uploaded automatically into a artifact repository like `Sonatype Nexus`.

## Run
The directory structure will be extended:
```
\time-tracker
   \...
   \glassfish4-run
   \derby
```

### Requirements
1. Unpack the Derby archive
1. Unpack another Glassfish application server that will be used for execution of the generated depoyment unit.

### Initial configuration

#### Derby database
The database is created using the scripts provided within the `timetracker-server` project.
Before executing the scripts the database needs to be started. For production, es assume the database is always up and running, so the databas has to started in network server mode.

```
\db-derby-10.4.1.3-bin\lib> java -jar derbyrun.jar -Dderby.system.home=/path/to/derby/home/dir server start
```

More documentation on running the database in network server mode or as embedded database can be found here:
- http://db.apache.org/derby/docs/10.11/adminguide/index.html
- http://db.apache.org/derby/papers/DerbyTut/ns_intro.html#ns_easier_way

#### Glassfish

In an runtime (production) environment the application sever will run multiple instances in a cluster.
For the purpose of demonstration, the setup of cluster with two servers will be described.

An easy-to-read documentation how to setup `Glassfish` `Cluster` : http://blog.c2b2.co.uk/2013/03/creating-simple-cluster-with-glassfish.html
A complementary documentation : https://glassfish.java.net/docs/4.0/ha-administration-guide.pdf

Applied to our infrastructure the following steps are required:

```
<<glasfish_home>>\bin\asadmin --user admin --host localhost --port 4848 create-cluster tt-cluster-1
<<glasfish_home>>\bin\asadmin --user admin --host localhost --port 4848 create-node-config --nodehost localhost tt-node-1
<<glasfish_home>>\bin\asadmin --user admin --host localhost --port 4848 create-local-instance --node tt-node-1 --cluster tt-cluster-1 --lbenabled true --checkports --savemasterpassword tt-instance-1
<<glasfish_home>>\bin\asadmin --user admin --host localhost --port 4848 create-local-instance --node tt-node-1 --cluster tt-cluster-1 --lbenabled true --checkports --savemasterpassword tt-instance-2
<<glasfish_home>>\bin\asadmin --user admin --host localhost --port 4848 start-cluster tt-cluster-1
<<glasfish_home>>\bin\asadmin --user admin --host localhost --port 4848 list-instances
```

`create-local-instance` will display the ports used for communicating to the cluster, they can be found in the administartion console (and the `domain.xml` file).

Much like for development, the `JDBC` and `Security Reals` must be set up.
Again, we are running the application server on the same development machine, in a real world project this woud be a dedicated server. For simplicity, we assumme no development or CI server is up and running, so we do not have to change the port settings to avoid conflicts while running the application server and the database.

For the setup instructions refer to the documentaion in the `Development Initial configuration` chapter.

This time, the cluster setup requires the `--target` to be specified:
```
<<glasfish_home>>\bin\asadmin --user admin --host localhost --port 4848 add-resources --target tt-cluster-1 <<pathTo>>/timetracker-server/src/main/setup/glassfish-resources.xml
```

**Important : the manual realm setup is under node `tt-cluster-1-config` and not `server-config` as described above for develoment!**

The last configuration is to prepare the cluster to be used with `Apache` `mod_jk`.

Unfortunately the `High Availability Administration Guide` does not cover this topic.
An documentation to this configuration can be found here : http://alexandru-ersenie.com/2009/08/18/glassfish-load-balancing-using-apache-and-mod_jk/
and here : http://stackoverflow.com/questions/16293572/glassfish-instances-unable-to-startup-after-enabling-ajp

```
asadmin --user admin --host localhost --port 4848 create-jvm-options --target tt-cluster-1 -DjvmRoute=${AJP_INSTANCE_NAME}
asadmin --user admin --host localhost --port 4848 create-jvm-options --target tt-cluster-1 -Dcom.sun.enterprise.web.connector.enableJK=${AJP_PORT}

asadmin --user admin --host localhost --port 4848 create-system-properties --target tt-instance-1 AJP_INSTANCE_NAME=worker1
asadmin --user admin --host localhost --port 4848 create-system-properties --target tt-instance-1 AJP_PORT=8109

asadmin --user admin --host localhost --port 4848 create-system-properties --target tt-instance-2 AJP_INSTANCE_NAME=worker2
asadmin --user admin --host localhost --port 4848 create-system-properties --target tt-instance-2 AJP_PORT=8209
```

**Important : the configuration for `AJP_INSTANCE_NAME` and `AJP_PORT` must match exacly later in the `mod_jk` configuration on the client side.**

- restart the cluser to activate the configuration change

```
asadmin --user admin --host localhost --port 4848 stop-cluster tt-cluster-1
asadmin --user admin --host localhost --port 4848 start-cluster tt-cluster-1
asadmin --user admin --host localhost --port 4848 list-instances
```

More information about creating and maintaining a cluster can be found here:
- https://glassfish.java.net/docs/4.0/
- https://glassfish.java.net/docs/4.0/reference-manual.pdf

### Execution
Open the servers web administration console and deploy the generated deployment unit `timetracker-server.war`.

**Important** : the deployment target is the cluster!

