# International Data Spaces

The [International Data Spaces Association](https://internationaldataspaces.org) (IDS) aims at open,
federated data ecosystems and marketplaces ensuring data sovereignty for the creator of the data.
The [IDS Connector](https://international-data-spaces-association.github.io/DataspaceConnector/Introduction)
is the core of the data space. Is the gateway to connect existing systems and their data
to an IDS ecosystem.

We would like to thank the colleagues of the
[Data Economy](https://www.isst.fraunhofer.de/en/business-units/data-economy/technologies/Dataspace-Connector.html)
unit of Fraunhofer ISST for the open-source
[Dataspace Connector](https://international-data-spaces-association.github.io/DataspaceConnector/) implementation.


# EGI DataHub Adapter

This project builds an adapter component (aka Provider App) that can be used to publish
a space from [EGI DataHub](https://docs.egi.eu/users/datahub/) to an IDS Connector.

A space in EGI DataHub is just a hierarchical structure of containers (folders) and objects (files).
Publishing a space to an IDS connector means creating a catalog for the space, then in that
catalog create a resource for each file. The adapter will not create resources for folders.

The resources created by the adapter will contain the following custom properties:

- The unique identifier of the file in the OneData backend
- The path of the file in the space, which allows consumers to reconstruct the
  hierarchical structure if they wish to do so

The adapter exposes a REST API through which users can publish data to a configured provider connector.
When the application is running, the API is available at `http://localhost:8080/api`. There is also a
Swagger UI available at `http://localhost:8080/api/doc`. 

The adapter endpoint `http://localhost:8080/api/action/publish` will take the passed in space ID
and will create a catalog for it in the connector. For each file in that space it will also create 
a new resource in the connector, together with all required entities, so that (a representation of) the
resource can be discovered and consumed (downloaded) by another (consumer) IDS connector.

The [entities](https://international-data-spaces-association.github.io/spaceConnector/Documentation/v6/DataModel)
created for each file from the space are:

- A resource to represent the file, which has
  - A contract with
      - A rule (usage policy)
  - A representation that includes
      - An artifact that has an URL to download the file, together with an access token whose 
        claims restrict access to just this file in read-only mode, and is valid for a (configurable)
        limited time

The type of usage policy that will be created by default allows free use, but this can be overridden
in the parameters of the call.

> As consumers will start discovering these resources, and request access to the artifact of a resource,
> the connector will create agreement entities tied to each requested artifact.

> This adapter application has to be [configured](#configure-the-adapter) with 2 access tokens that have access to
> all spaces that will be published.

> Because the URL in the artifacts is using a time-limited access token, the space will have to
> be published again (refreshed), before the access tokens expire. This can be achieved by calling the
> broker's publish endpoint from a cron job (or similar). 

This project uses [Quarkus](https://quarkus.io/), the Supersonic Subatomic Java Framework.


## Configuration

The application configuration file is in `src/main/resources/application.yml`.

> The settings that are supposed to be overridable with environment variables should also
> be added to `src/main/resources/application.properties`. 

> Do not forget to change the setting `quarkus/tls/trust-all` to _false_ when deploying in
> production, which causes connections to the IDS connector to fail, if the connector's 
> certificate does not match the URL the adapter uses to connect to it from the
> setting `idsa/connector`.


## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw compile quarkus:dev
```

Then open the Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.


## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into
the `target/quarkus-app/lib/` directory.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.


## Run the application with Docker Compose

You can use Docker Compose to easily deploy and run this adapter application alongside a
custom (provider-side) IDS connector and its associated UI. The steps to do this:

- [Obtain a certificate](#obtain-certificate-for-the-ids-connector) for your custom IDS connector
- [Configure the components](#configure-the-components)
- [Build and run the containers](#build-provider-components-and-run-them-in-containers)


### Obtain certificate for the IDS connector

The certificate included in the [IDS connector source code repository](https://github.com/International-Data-Spaces-Association/DataspaceConnector)
is a self-signed certificate not suitable for deployment in production. This is why the connector
by default runs with the setting `DEPLOY_MODE` set to `TEST_DEPLOYMENT`, instructing the connector
to skip some certificate validations.

This project includes a provider-side IDS connector but changes its configuration to use `PRODUCTIVE_DEPLOYMENT`
deploy mode, which requires you to supply a new component certificate (replace the file
`src/main/docker/provider/connector/conf/my-keystore.p12` as the password
for this EGI certificate is not included):

> The steps below will guide you to obtain a certificate suitable to connect to the IDS broker
> (and possibly other infrastructure components) operated by [TNO](https://ids.tno.nl) for the EUHubs4Data project.
> In case you want to use a certificate issued by a different certification authority (CA), follow
> the procedure that is appropriate for requesting a certificate from your preferred CA.

> The prerequisite for performing the steps below is to have [OpenSSL](https://wiki.openssl.org/index.php/Binaries) installed.

1. Choose the hostname where your IDS connector will be available from the Internet
   (e.g. the certificate included in this project for the provider-side IDS connector uses
   the hostname _euh4d-datahub.vm.fedcloud.eu_). 
2. Create key pairs for your organization (participant in the EUHubs4Data project) and connector:
    ```shell
    $ openssl req -new -newkey rsa:4096 -nodes -keyout participant.key -out participant.csr
    $ openssl req -new -newkey rsa:4096 -nodes -keyout component.key -out component.csr
    ```

   > Use the hostname from step 1 in the subject of the component certificate, as this certificate.
   > will also be used for HTTPS.

3. Get your participant certificate signed, by going to https://daps.euh4d.dataspac.es/#participants
   and filling in the form **Request participant certificate**.

   > The field _Participant ID_ must be a valid URI that uniquely identifies your organization.
   
   > The field _Certificate Signing Request_ must be filled with the content of the `participant.csr`
   > file generated in step 2.

4. Download the signed participant certificate (file `participant.cer`).
5. Get your component certificate signed, by going to https://daps.euh4d.dataspac.es/#connectors
   and filling in the form **Request component certificate**.

   > The field _Participant ID_ must match the participant ID you used in step 3.

   > The field _Component ID_ must be a valid URI that uniquely identifies your IDS connector.

   > The field _Certificate Signing Request_ must be filled with the content of the `component.csr`
   > file generated in step 2.

6. Download the signed component certificate (file `component.cer`)
7. Create a PKCS12 keystore with the component key and certificate:
    ```shell
    $ openssl pkcs12 -export -out my-keystore.p12 -inkey component.key -in component.crt
    ```

   > The password you use in this step is the keystore password needed in the steps below.

8. Copy the keystore to `src/main/docker/provider/connector/conf/my-keystore.p12`.  
9. Send an email to [Maarten Kollenstart](mailto:maarten.kollenstart@tno.nl) to activate the 
   participant and component identities.
10. Obtain the truststore for the CA that issued the certificates and save it to the
   file `src/main/docker/provider/connector/conf/my-truststore.p12` 

   > The TNO truststore (file `truststore.p12`) can be downloaded from [here](https://daps.euh4d.dataspac.es/truststore.p12)

This project also includes a consumer IDS connector, which uses the default `TEST_DEPLOYMENT`
deploy mode. If you want to use a custom certificate for this connector too, repeat the above
steps and copy the resulting keystore to `src/main/docker/consumer/connector/conf/my-keystore.p12`.


### Configure the components

Two of the containers rely on the provider-side IDS connector, thus some connector settings
must be specified so that they can be passed on to each container. The easiest way to do this
is to create the following environment variables before you build and run the containers:

- `IDSA_TITLE` is the custom name of the IDS connector
- `IDSA_USERNAME` is the username to access the REST API of the IDS connector
- `IDSA_PASSWORD` is the password to access the REST API of the IDS connector


#### Configure the IDS connector

The IDS connector settings are in the file `src/main/docker/provider/connector/connector.env`.
Make the following changes:

- Change the setting `COMPONENT_ENDPOINT` to a URL of the form `https://HOSTNAME:PORT/api` where
  HOSTNAME is the hostname chosen in step 1 above (and which resolves to the machine or container
  running the IDS connector), whereas PORT is the port on which the IDS connector is published at
  that hostname.
- Change the setting `DEPLOY_MODE` to `PRODUCTIVE_DEPLOYMENT`
- Change the setting `COMPONENT_ID` to be the component ID you used in step 5 above.
- Change the setting `COMPONENT_CERTIFICATE` to contain the component certificate (content of the
  file `component.cer`) downloaded in step 6 above (the part between "-----BEGIN CERTIFICATE-----"
  and "-----END CERTIFICATE-----", without newlines)
- Change the setting `SERVER_SSL_KEY-STORE-PASSWORD`to be the keystore password you set in step 7 above.
- Change the setting `CONFIGURATION_KEYSTOREPASSWORD` to be the keystore password you set in step 7 above.
- Change the setting `CONFIGURATION_TRUSTSTOREPASSWORD` to be the truststore password for the file
  from step 10 above (or "euhubs4data" for the included TNO truststore).


#### Sizing considerations

When consumers connect to the provider-side IDS connector and access a resource's data,
the connector does a GET request on the URL from the artifact's `accessUrl` field,
then passes the response body of this request to the consumer. Keep in mind that the connector
keeps the entire artifact content/body in memory during the above process.

In case of large artifacts and/or multiple consumers requesting resources from the connector,
you must make sure the machine/container that runs the IDS connector has enough RAM to keep all
these artifacts loaded in memory.


#### Configure the adapter

The adapter (or backend) settings are in the file `src/main/docker/provider/adapter/adapter.env`.

For the adapter application to be able to create entities in the provider-side IDS connector,
it needs access to the space in EGI DataHub that is being published, as it has to enumerate
files and read/write file metadata. To be able to do these operations, it needs 2 access tokens:

- An access token for OneZone (this allows access to 
  the [administrative REST API](https://onedata.org/#/home/api/stable/onezone?anchor=section/Overview) of the backend)
- An access token for OneProvider (this allows access to 
  the [data management REST API](https://onedata.org/#/home/api/stable/oneprovider?anchor=section/Overview)
  for reading and writing file metadata)

These two tokens should go in settings `DATAHUB_ONEZONE-TOKEN` and `DATAHUB_ONEPROVIDER-TOKEN`, respectively.

> The source files of this project already contain the necessary OneData access tokens for the sample space
> created for this project, in file `src/main/resources/application.yml`.
> You only need to supply new tokens if you want to publish files from other spaces.


### Build provider components and run them in containers

Navigate to `src/main/docker/provider` and run `build`.

This will result in 3 containers running:

- The adapter's REST API and its Swagger UI will be available at 
http://localhost:8080/api and
http://localhost:8080/api/docs, respectively.
- The provider-side IDS connector's REST API and its Swagger UI will be available at
https://localhost:8081/api and
https://localhost:8081/api/docs, respectively.
- The UI that offers an overview of the connector's configuration and the
resources in the connector is reachable at https://localhost:8082.

After all containers are up and running:

- You can use the supplied Postman collections to create, browse and consume resources manually.
- The adapter container will automatically publish the entire content of the configured
  EGI DataHub space to the IDS connector, then will repeat the operation every 3 days to
  make sure the credentials of each artifact are valid.


> The IDS connector should also be available through the URL specified in the variable `COMPONENT_ENDPOINT` 
> from the connector's configuration file. The component certificate you added in the
> [previous step](#configure-the-ids-connector) should be for the URL in this variable.


### Build consumer and run it in a container

Navigate to `src/main/docker/consumer` and run `build`.

This will result in 1 container running:

- The consumer-side IDS connector's REST API and its Swagger UI will be available at
  https://localhost:8083/api and
  https://localhost:8083/api/docs, respectively.


## Related Guides

- [REST client implementation](https://quarkus.io/guides/rest-client): REST client to easily call REST APIs
- [REST server implementation](https://quarkus.io/guides/rest-json): REST endpoint framework to implement REST APIs
- [REST Easy Reactive](https://quarkus.io/guides/resteasy-reactive) Writing reactive REST services
- [YAML Configuration](https://quarkus.io/guides/config#yaml): Use YAML to configure your application
- [Swagger UI](https://quarkus.io/guides/openapi-swaggerui): Add user-friendly UI to view and test your REST API
- [Mutiny Guides](https://smallrye.io/smallrye-mutiny/guides): Reactive programming with Mutiny
- [Optionals](https://dzone.com/articles/optional-in-java): How to use Optional in Java
- [CompletableFuture](https://www.callicoder.com/java-8-completablefuture-tutorial/): Java CompletableFuture Tutorial
