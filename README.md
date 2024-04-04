# Bootique Release Automation 

[Bootique](https://bootique.io) release automation tools prototype.

### Prerequisites

1. Create file `release-manager.yml` with config. Template of this file is release-manager-sample.yml.
2. [Generate](https://blog.github.com/2013-05-16-personal-api-tokens/) your personal access token on GitHub. Set **gitHubToken** variable in config file.
3. Install [GPG Suite](https://gpgtools.org) 
   - Generate your GPG key.
   - Add key to keychain to prevent password request for every module.
4. Create folder to have local copy of releasing project. Set **basePath** variable in config file.
5. Setup [RSA key](https://docs.github.com/en/authentication/connecting-to-github-with-ssh/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent) to access repositories on the GitHub
6. Create `.m2/settings.xml` or add a Bootique release repo (`oss-sonatype-releases`) credentials to the existing one. You could get your Sonatype API key at the [OSS website](https://oss.sonatype.org/#profile;User%20Token):
   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>oss-sonatype-releases</id>
            <username>API_key_name</username>
            <password>API_key_password</password>
        </server>
    </servers>
   </settings>
   ```
7. On the newer MacOS versions you may need `ssh-askpass`: 
   ```bash
   brew tap theseal/ssh-askpass
   brew install ssh-askpass
   sudo ln -s /usr/local/bin/ssh-askpass /usr/X11R6/bin/ssh-askpass
   ```
8. You may need to tweak `release.sh` and `maven.sh` scripts on MacOS to set proper JDK version

### Usage
```
$ mvn clean install
$ java --add-opens java.base/sun.net.www.protocol.https=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED -jar target/release-1.0-SNAPSHOT.jar
```

**NOTE**: `--add-opens` required for the Jersey Client to be able to send `PATCH` request

Then just go to http://127.0.0.1:9999/ui/

