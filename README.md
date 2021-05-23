member-data
---
A program to fetch member-data from helpberkeley.org, and post information
about it to back to topics on the site.

Currently, the site postings are designed to assist controlBlockRestaurant order
data reconciliation, driver dispatcher, and administrative member
data tasks.

### Building

*mvn install* builds the standard jar file and additional an uber jar file
containing all of the required dependencies.

*mvn test* runs local tests which use simulated data and do not require access
to the website.

The uber jar contains org.helpberkeley.memberdata.Main.main as the entry
point and requires authentication to be configured.


### Authentication Requirements:

A Java properties file named memberdata.properties must be created
and added somewhere within the classpath.

The properties file must contain the following properties:
* Api-Username - a username from the helpberkeley.org site.
* Api-Key - a valid API key for that user.

API keys are generated in the Admin->API page.

Example:

```
Api-Username   aloyisus
Api-Key        123456789abcdef987654321
GMaps-Api-Key  xyzzy12345
```

###[License](LICENSE)
