# Bootique Release Automation 

[Bootique](https://bootique.io) release automation tools prototype.

### Prerequisites

1. Create file `release-manager.yml` with config. Template of this file is release-manager-sample.yml.
2. [Generate](https://blog.github.com/2013-05-16-personal-api-tokens/) your personal access token on GitHub. Set **gitHubToken** variable in config file.
3. Install [GPG Suite](https://gpgtools.org) 
   - Generate your GPG key.
   - Add key to keychain to prevent password request for every module.
4. Create folder to have local copy of releasing project. Set **basePath** variable in config file.

### Usage
```
$ mvn clean install
$ java --add-opens java.base/sun.net.www.protocol.https=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED -jar target/release-1.0-SNAPSHOT.jar
```

**NOTE**: `--add-opens` required for the Jersey Client to be able to send `PATCH` request

Then just go to http://127.0.0.1:9999/ui/

