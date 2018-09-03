# Bootique Release Automation 

[Bootique](https://bootique.io) release automation tools prototype.

### Prerequisites

1. Create file with config. Template of this file is release-manager-sample.yml.
2. Generate your personal access token on GitHub.
3. Install [GPG Suite](https://gpgtools.org) 
   - Generate your GPG key.
   - Add key to keychain to prevent password request for every module.
4. Create folder to have local copy of releasing project. Set __basePath__ variable in config file.

### Usage
```
$ mvn clean install
$ java -jar target/release-1.0-SNAPSHOT.jar
```

Then just go to http://127.0.0.1:9999/ui/

### TODO
1. Add gpg key check before starting release.
