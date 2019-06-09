# Listing Reporter

Listing Reporter is a command line Java program that

- fetches sample data from the REST API about online marketplace listings and information relevant to them
  - possible listing statuses
  - marketplaces
  - location info
- real world currency information
- validate listing information
- log malformed listing information into a CSV file
- synchronize the data with a database
- generate listing reports in JSON format based on the gathered data
- upload the reports to an FTP server

### Getting started

Database and FTP connection parameters should be set correctly in *./config/application.properties*.  
Before first run, execute *createDbTables.sql* on the database you specified in the config.  
Use Maven to synchronize dependencies of the project, or download the .jar files (based on pom.xml)  
and include them in your classpath on building the app.

With all preparations done, run main from ListingReporter.java.

### Used technology

- Sample data provided by *Mockaroo*
- Currency information by *exchangeratesapi.io*
- PostgreSQL 10 database and jdbc driver
- Gson API for JSON serialization and de-serialization
- Apache Commons Net FTP tools
- Date4J comprehensive DateTime library

## Author

- **Albert Kelemen** - [AlbertPrograms](https://github.com/albertprograms)