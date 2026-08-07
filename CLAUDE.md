# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A [Bootique](https://bootique.io)-based web app that automates releasing the multi-module Bootique project family to Maven Central. It clones the target repos locally, walks each repo through a multi-stage Maven release pipeline (pull → validate → prepare → perform → Central sync), and drives the whole thing from a Vue web UI at `http://127.0.0.1:9999/ui/`. GitHub metadata (repos, PRs, issues, milestones) is imported on a schedule and persisted to a local SQLite DB.

Single Maven module, Java 21. Backend = Bootique DI + Jersey REST + Jetty + Cayenne ORM. Frontend = a Vue 3 + TypeScript single-page app on Bootstrap 5, built by Vite.

## Commands

```bash
# Full build — also installs node v22, runs npm ci + vite build via frontend-maven-plugin
mvn clean install

# Backend only, skipping the node round-trip
mvn clean install -Dskip.frontend=true

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

Frontend-only iteration (from `src/main/assets/`): `npm run build:watch` rebuilds into `target/classes/static/ui/` on save; reload `http://127.0.0.1:9999/ui/` by hand (hard-reload, since watch mode uses stable filenames). Also `npm run typecheck`, `npm test`, `npm run lint` (Prettier `--check` + ESLint) and `npm run format`. There is deliberately **no Vite dev server** — the app must be served by Jetty so `/api` and the WebSockets resolve.

`npm run lint` is bound to the Maven **`verify`** phase, not to the build, so a style warning never blocks producing an artifact. One-time setup so `git blame` skips the Prettier reformat: `git config blame.ignoreRevsFile .git-blame-ignore-revs`.

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

`clone` uses the repository's `sshUrl` (falling back to the HTTPS `url` only if it is null), so the working copy's `origin` matches the `scm:git:ssh://git@github.com/...` `developerConnection` that the released POMs declare — `maven-release-plugin` pushes commits and tags to that SCM URL, not to the working copy's remote. There is therefore no HTTPS path through a release; an SSH key is required.

### GitHub import & persistence
`GitHubDataImportJob` and `MavenProjectsImport` are Bootique scheduled jobs (cadence in `settings.yml` `scheduler.triggers`). GitHub data is fetched via GraphQL (`GraphQLGitHubApiImport` / `SimpleGraphQLService`) and REST v3 (`GitHubRestV3API`). Everything is persisted via **Apache Cayenne** to SQLite (`github.db`). Cayenne project: `src/main/resources/cayenne/cayenne-project.xml` with two maps — `github.map.xml` and `maven.map.xml`. Persistent classes live in `model/persistent/` and `model/maven/persistent/`; the `_*.java` classes under `auto/` are Cayenne-generated — edit the hand-written subclass, not the `_` superclass.

### Web layer

**The browser owns `/ui/*`, the REST API owns `/api/*`.** There is no server-side HTML rendering.

- REST controllers (`controller/`, JAX-RS) are served under `/api/*` (see `settings.yml` `jersey.urlPattern`). The `release/` subpackage holds the release-process endpoints; note five classes share `@Path("/release")`.
- `web/SpaFilter` redirects `/` to `/ui/` and forwards every extension-less `/ui/**` path to `/ui/index.html`, so client-side routes survive a reload. Real files (the hashed assets) and `/api/*` chain straight through to the static servlet.
- WebSocket endpoints (`controller/websocket/`) push live stage/job status: `/api/release/socket` sends the literal `"LOAD"` on each stage transition, `/job/status` sends `""` on each batch task completion. Both are content-free pings — the client re-fetches. Their paths are absolute against the Jetty context and unaffected by `jersey.urlPattern`.
- `GET /api/session` supplies the nav's organization, user and badge counts. `organization`/`user` are null until the first GitHub import succeeds, and the UI renders a hint rather than an error.

Frontend lives in `src/main/assets/` — Vite root, sources under `src/`:
- `api/` — one module per endpoint group over a shared `fetch` wrapper (`http.ts`, base `/api`). `agrest.ts` builds the Agrest `include`/`sort`/`cayenneExp` parameters; `types.ts` mirrors the Java enums and entity shapes.
- `domain/` — pure, unit-tested logic with no Vue imports: `releaseBoard.ts` holds the release/rollback state machine, `version.ts` the version arithmetic, `nav.ts` the nav derivation, `labelColor.ts` the GitHub label contrast maths. **Changes to board button visibility belong here, with a test** — that means `stageActions()`, which composes the predicates into the ordered list of buttons a cell offers, not just the predicates themselves.
- `composables/` — shared reactive state. `useAsyncResource` is the one way to load data (fetch + `loading` + `error`); `useAgrestList` adds URL-synced sort/filter on top of it. Also `useJobStatus`, `useReleaseBoard`, `useSession`, `useCacheWarmup`, `useReleaseSummary`.
- `components/`, `views/`, `styles/`, `router/` (`routes.ts` is the route table; `index.ts` builds the router).

**The route table is the single source of truth for paths, page headings and the nav.** A route's `meta.title` is its `<h1>` (via `PageShell`) and its document title; `meta.nav` puts it in `AppNav`. Views never pass a title, and cross-page links use route `name`, never a literal path.

`src/styles/` — `_bs.scss` is the only file that may name `bootstrap/scss/bootstrap`; it configures Bootstrap through `@forward ... with`. Overrides go there as Sass variables, because Bootstrap regenerates its whole `--bs-*` set per `data-bs-theme` and so gets dark mode for free. The `--app-*` tokens in `_tokens.scss` deliberately hold **no colours**, since an app-defined colour would need a hand-written dark twin.

Vite emits to `target/classes/static/ui/` with `base: '/ui/'`; Jetty serves it from `classpath:static`. Nothing else may be written into `target/classes/static/` — the `ui/` subdirectory is what makes `emptyOutDir` safe.

### Logging
Custom Logback setup (`service/logger/`): `MultiAppender` + per-release/per-rollback `ExecutionLogger`s capture stage output so it can be streamed to the UI and written to `logs.log`, in addition to console. Logger config is in `settings.yml` under `log:`.

## Testing notes

JUnit 5 (`bootique-junit`) + Mockito. Tests instantiate services directly and hand-build a Cayenne `ServerRuntime` from `cayenne/cayenne-project.xml` rather than booting the full app. Hand-written `Mock*` doubles (e.g. `MockPreferenceService`, `MockGitHubApi`, `MockReleasePersistentService`) live alongside the tests and are the preferred way to fake collaborators. Some tests are `@Disabled` (they depend on local git/maven state) — don't assume the whole suite runs clean offline. Test fixtures (sample POMs/repos) are under `src/test/resources/`.

Frontend tests are Vitest over `src/main/assets/src/**/*.spec.ts`, wired into the Maven `test` phase (skipped by `-DskipTests`). They cover the release board, version arithmetic, nav derivation, label contrast and `useAsyncResource` — the logic worth protecting. **Components are deliberately not mounted**: the environment is `node`, and component tests here would assert Bootstrap class strings rather than behaviour. Push logic into `domain/` instead. `vue-tsc --noEmit` runs as part of `npm run build` with `strictTemplates`, so a type error — including a component used but not imported — fails the Maven build.

## Coding standards

- prefer short comments that describe hidden ideas, not just what's in the code; do not mention the past iterations or other reasoning from the implementation cycle
- use Javadoc format comments for public API
- tests should cover public contracts, not internal implementation aspects
- pure utility methods that use single Cayenne object's data are good candidates for inclusion in the cayenne layer

### Frontend conventions

These exist because each was violated in more than one way before, not as style preference:

- **Destructure composables at the top of `<script setup>`, renaming on the way out where names collide. A template never contains `.value`.**
- **One loading idiom per situation**: `LoadingBar` (page-level, a listing is being fetched), `JobProgress` (a server batch job with a real percentage), `InlineSpinner` (a region with nothing to show yet), `BusyButton` (a single action in flight). Nothing else hand-rolls a spinner.
- **One error surface**: `AppAlert`, inside modals as much as on pages. Guidance is not an error — it belongs in an `EmptyState`, not in a red banner.
- **Tables go through `TableFrame`**, which owns the card frame and the canonical classes. `ModuleTable` covers every module listing; omit `selected` for a read-only one.
- **Modals that ask for a decision use `ConfirmModal`**; `AppModal` is the primitive underneath it.
- Badges and pills use `chip-*` (`_chips.scss`), never Bootstrap's `text-bg-*`, whose `light` variant is not theme-scoped and glares in dark mode.

## Claude Code Workflow

- prefer subagents where practical
- for complex tasks perform code review in a separate agent from the one implementing
