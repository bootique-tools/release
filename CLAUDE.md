# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A [Bootique](https://bootique.io)-based web app that automates releasing the multi-module Bootique project family to Maven Central. It clones the target repos locally, walks each repo through a multi-stage Maven release pipeline (pull → validate → prepare → perform → Central sync), and drives the whole thing from a Vue web UI at `http://127.0.0.1:9999/ui/`. GitHub metadata (repos, PRs, issues, milestones) is imported on a schedule and persisted to a local SQLite DB.

Single Maven module, Java 21. Backend = Bootique DI + Jersey REST + Jetty + Cayenne ORM. Frontend = Vue 2 / Bootstrap 4 built by webpack.

## Commands

```bash
# Full build — also installs node v14, runs npm install + webpack build via frontend-maven-plugin
mvn clean install

# CI build (build + test)
mvn clean verify

# Single test class / method
mvn test -Dtest=DefaultMavenServiceTest
mvn test -Dtest=ReleaseExecutorTest#releaseNotFinishWithFinishDescriptor

# Run the app (shaded jar). --add-opens is REQUIRED so the Jersey client can send PATCH
java --add-opens java.base/sun.net.www.protocol.https=ALL-UNNAMED \
     --add-opens java.base/java.net=ALL-UNNAMED \
     -jar target/release-1.0-SNAPSHOT.jar
```

Frontend-only iteration (from `src/main/assets/`): `npm run build` or `npm run build:watch`. The Maven build invokes this automatically; you rarely run it by hand.

## Configuration

Config layers, merged in order (later wins): `classpath:settings.yml` (committed defaults — ports, scheduler, jersey targets, org names) then `release-manager.yml` (user-specific, **gitignored**, must be created from `release-manager-sample.yml`).

`release-manager.yml` holds the GitHub token, Sonatype Central credentials, `basePath` (local clone location of the repos being released), and `javaHome` (a command like `/usr/libexec/java_home -v 21`). It may also hold an optional `gpgPassphrase` — when set, the app signs artifacts non-interactively (loopback pinentry) by passing it to Maven as the `MAVEN_GPG_PASSPHRASE` env var (`BaseDesktopService.mavenEnvironment()`), instead of relying on a pre-unlocked `gpg-agent`; requires `maven-gpg-plugin` >= 3.1.0 in the released modules. Treat the whole file as secret-bearing.

## Architecture

### Bootique module wiring
`Application` is both `main()` and the root `BQModule`. `configure(Binder)` is the single source of truth for what's bound — read it first. Notably it:
- binds every service interface → impl (Git, Maven, GitHub, GraphQL, batch jobs, loggers, descriptors, stage manager/updater),
- registers Jersey controllers by package scan (`addPackage(RepoController.class.getPackage())`),
- registers two WebSocket endpoints and the static-file servlet,
- registers two scheduled jobs and decorates `ServerCommand` with `ScheduleCommand` so the scheduler starts with the server,
- **maps the release state machine**: `bindMap(ReleaseStage.class, ReleaseTask.class)` and `bindMap(RollbackStage.class, ReleaseTask.class)`.

### Release pipeline (the core)
The release is a per-repository state machine. Each `ReleaseStage` (`RELEASE_PULL`, `RELEASE_VALIDATION`, `RELEASE_PREPARE`, `RELEASE_PERFORM`, `RELEASE_SYNC`) maps to a `ReleaseTask` (`Function<Repository, String>`) via the `bindMap` in `Application`. To add/change a stage, edit the `ReleaseStage` enum, add a `ReleaseTask` impl under `service/tasks/`, and wire it in the `bindMap`. Rollback is the mirror image (`RollbackStage` → `RollbackCentralPublishTask` / `RollbackMvnRelease`).

Execution flow: `ReleaseProcessController` → `ReleaseExecutor` (the `ReleaseExecutorService`) submits a `BatchJobDescriptor` to the `BatchJobService`. Stage tasks run across repos respecting Maven dependency order. `ReleaseExecutor` chains stages by re-invoking `executeRelease()` from the batch job's completion listener until the sync stage is reached; `canExecuteRelease()` / `releaseInProgress()` guard transitions. Status per repo×stage lives in `ReleaseStageStatus` (Not_Start, In_Progress, Success, Skip, Reload, failure…). `StageManagerService`/`StageUpdaterService` mutate stage state; `ReleasePersistentService` persists release descriptors so a release survives restart.

### Talking to the outside world
Actual `mvn` invocations do **not** run in-process. `DesktopService` (per-OS impls: `MacOSService`, `LinuxDesktopService`, `WindowsDesktopService`, chosen in `Application.createDesktopService`) shells out to the repo-root scripts **`maven.sh`** (`runMavenCommand`) and **`release.sh`** (`performReleasePlugin`, i.e. `mvn release:prepare|perform|rollback|clean`). Those scripts set `JAVA_HOME` from the `javaHome` preference and add GPG signing flags. `GitService` (`ExternalGitService`) likewise shells out to `git`. So debugging a release failure often means reading the script output / `logs.log`, not just Java stack traces.

### GitHub import & persistence
`GitHubDataImportJob` and `MavenProjectsImport` are Bootique scheduled jobs (cadence in `settings.yml` `scheduler.triggers`). GitHub data is fetched via GraphQL (`GraphQLGitHubApiImport` / `SimpleGraphQLService`) and REST v3 (`GitHubRestV3API`). Everything is persisted via **Apache Cayenne** to SQLite (`github.db`). Cayenne project: `src/main/resources/cayenne/cayenne-project.xml` with two maps — `github.map.xml` and `maven.map.xml`. Persistent classes live in `model/persistent/` and `model/maven/persistent/`; the `_*.java` classes under `auto/` are Cayenne-generated — edit the hand-written subclass, not the `_` superclass.

### Web layer
- REST controllers (`controller/`, JAX-RS) are served under `/ui/*` (see `settings.yml` `jersey.urlPattern`). `release/` subpackage holds the release-process endpoints.
- `view/` classes are Bootique MVC (Mustache) server-side views.
- WebSocket endpoints (`controller/websocket/`) push live stage/job status to the browser; `StageStatusListener` bridges stage updates to sockets.
- Frontend lives in `src/main/assets/` (Vue 2 components in `js/view/`, SCSS in `scss/`, webpack config `webpack.config.js`). Built assets are served as static content from `classpath:static`.

### Logging
Custom Logback setup (`service/logger/`): `MultiAppender` + per-release/per-rollback `ExecutionLogger`s capture stage output so it can be streamed to the UI and written to `logs.log`, in addition to console. Logger config is in `settings.yml` under `log:`.

## Testing notes

JUnit 5 (`bootique-junit`) + Mockito. Tests instantiate services directly and hand-build a Cayenne `ServerRuntime` from `cayenne/cayenne-project.xml` rather than booting the full app. Hand-written `Mock*` doubles (e.g. `MockPreferenceService`, `MockGitHubApi`, `MockReleasePersistentService`) live alongside the tests and are the preferred way to fake collaborators. Some tests are `@Disabled` (they depend on local git/maven state) — don't assume the whole suite runs clean offline. Test fixtures (sample POMs/repos) are under `src/test/resources/`.

## Coding standards

- prefer short comments that describe hidden ideas, not just what's in the code; do not mention the past iterations or other reasoning from the implementation cycle
- use Javadoc format comments for public API
- tests should cover public contracts, not internal implementation aspects
- pure utility methods that use single Cayenne object's data are good candidates for inclusion in the cayenne layer

## Claude Code Workflow

- prefer subagents where practical
- for complex tasks perform code review in a separate agent from the one implementing
