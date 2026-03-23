<!-- markdownlint-disable -->
placeholder

<!-- markdownlint-disable -->
# MODERNIZATION_REPORT.md

## Executive Summary
This modernization converted a legacy Java EE application (JBoss 4 + Struts 1.1 + EJB 2.1 + JSP) into a self-contained Spring Boot 3.x application (Java 21 target) with:
- Spring MVC controllers instead of Struts Actions
- Spring Security form-based login/auth instead of JAAS/JBoss login-config
- Spring Boot + embedded Tomcat instead of EAR/WAR deployment
- Thymeleaf templates instead of legacy JSP views
- all committed legacy JARs and deployment descriptors removed

The application compiles successfully with Maven and passes unit + integration tests (`mvn test`).

---

## Global vs Local State (what was installed)
This modernization did not install any OS-level packages globally (no `brew install`, `apt/yum`, etc.).
Changes were limited to the repository workspace:
- Project code and config files: added/updated/deleted in `javaee-legacy-app-example-master/`.
- Maven dependencies: downloaded by `mvn` and stored in a local Maven repository inside the workspace via `-Dmaven.repo.local=./.m2/repository`.
- No global Java agent/tooling changes were made; any runtime processes (`spring-boot:run`) were only temporary for validation.

---

## Task 1 — Archaeology Report

### Before Architecture (legacy)
```
                +------------------+
Browser -----> |  app-web.war     |
                |                  |
                |  Struts Action   |
                |  *.do ->         |
                |  ActionServlet   |
                |                  |
                |  JSP Views       |
                |                  |
                |  WEB-INF/web.xml  |
                +---------+--------+
                          |
                          | JNDI lookup (home/remote)
                          v
                +------------------+
                |  app-ejb.jar     |
                |  EJB 2.1         |
                |  RegistrationBean|
                |  ejb-jar.xml     |
                +------------------+

Security:
- JBoss JAAS login-config / JBoss security-domain
- MyLoginModule validates admin/123456 and assigns role SIE
```

### After Architecture (modern)
```
                +---------------------------+
Browser -----> | Spring Boot (embedded     |
                | Tomcat, self-contained)  |
                +--------------+------------+
                               |
                               | Spring MVC @Controllers
                               v
                 +-------------------------------+
                 | /register, /secure, /jaas/* |
                 +-------------------------------+
                               |
                               | service layer (Spring @Service)
                               v
                 +-------------------------------+
                 | RegistrationBean + services  |
                 | (no EJB home/remote/JNDI)   |
                 +-------------------------------+
                               |
                               | Spring Security (form login)
                               v
                 +-------------------------------+
                 | authenticated endpoints + CSRF|
                 +-------------------------------+
```

### Legacy Components (enumeration)

#### EJBs (from `ejb-jar.xml`)
1. Session Bean
   - Name: `RegistrationEJB`
   - Type: Stateless session
   - EJB class: `com.example.RegistrationBean`
   - Home/Remote:
     - `com.example.RegistrationHome` (home)
     - `com.example.RegistrationEJB` (remote)

No entity beans and no message-driven beans were present.

#### Struts Actions (from `struts-config.xml`)
1. `path="/register"`, `type="com.example.RegisterAction"`
   - Forward: `success` -> `/register_confirmation.jsp`

Note: `SecureAction` existed in code but was not mapped in `struts-config.xml`.

#### JSP Views (from `app-web/WebRoot`)
- `index.jsp`
- `register.jsp`
- `register_confirmation.jsp`
- `secure/index.jsp`
- `jaas/login.jsp`
- `jaas/login_error.jsp`
- `jaas/logoff.jsp`

#### Deployment Descriptors
- `app-ejb/src/META-INF/ejb-jar.xml`
- `app-ejb/src/META-INF/jboss.xml`
- `app-web/WebRoot/WEB-INF/struts-config.xml`
- `app-web/WebRoot/WEB-INF/web.xml`
- `app-web/WebRoot/WEB-INF/jboss-web.xml`
- `app-ear/META-INF/application.xml`

### Committed Library Versions (from `META-INF/MANIFEST.MF` inside committed JARs)
- `struts.jar` — Implementation-Version: `1.1`
- `struts-legacy.jar` — Implementation-Version: `1.0`
- `commons-beanutils.jar` — Implementation-Version: `1.6`
- `commons-collections.jar` — Implementation-Version: `2.1`
- `commons-digester.jar` — Implementation-Version: `1.5`
- `commons-fileupload.jar` — Implementation-Version: `1.0`
- `commons-lang.jar` — Implementation-Version: `1.0.1`
- `commons-logging.jar` — Implementation-Version: `1.0.3`
- `commons-validator.jar` — Implementation-Version: `1.0.2`
- `jakarta-oro.jar` — (version not captured from manifest output)

### CVEs flagged (legacy high/critical focus)
HIGH/CRITICAL issues were addressed primarily by:
1) removing legacy JARs from the runtime classpath
2) replacing Struts/JBoss security with modern Spring Boot/Spring Security dependencies

HIGH/CRITICAL (most important):
- Apache Struts 1.x
  - `CVE-2016-1181` (HIGH) — ActionServlet issues involving multipart leading to RCE/DoS
  - `CVE-2016-1182` (HIGH) — XSS/DoS class for Struts 1.x
  - `CVE-2014-0114` (HIGH) — classloader manipulation via ActionForm class parameter
- Apache Commons Collections
  - `CVE-2015-7501` (CRITICAL) — unsafe deserialization (InvokerTransformer chain)
- Apache Commons BeanUtils
  - `CVE-2014-0114` (HIGH) — classloader manipulation via class property

Additional risks (removed together with dependency jars):
- Apache Commons FileUpload (legacy 1.0) had known DoS and insecure temporary-file patterns; resolved by full removal/replacement through Spring Boot.

This modernization did not run an automated CVE scanner; it used:
- exact version extraction from `MANIFEST.MF`
- removal of legacy JARs from `WEB-INF/lib`
- reliance on Spring Boot dependency management for a modern dependency graph

### Estimated Migration Effort + Top Risks
Estimate for tasks 1–8 on this small sample: ~40–70 hours.

Top 5 risk areas:
1) Security behavior equivalence (redirects, CSRF, roles/principal naming)
2) View migration differences (template variables, error handling)
3) Dependency graph correctness (transitive dependency upgrades/removals)
4) Build tool migration (Maven local repo / network constraints in CI)
5) Test reliability across new JVM/tooling (Mockito inline vs Java 25/ByteBuddy)

---

## Task 2 — Build Tool Introduction (Maven)
- Added `pom.xml` (Spring Boot parent) and Maven compiler target `release=21`.
- Removed legacy committed JARs and deployment descriptors.
- Added Spring Boot dependency management to govern runtime libraries.
- `mvn test` succeeds (unit + integration).

---

## Task 3 — EJB 2.x -> Spring services / annotations
Legacy had one EJB 2.1 stateless session bean (`RegistrationEJB`).

Migration:
- `com.example.RegistrationBean` rewritten as a Spring `@Service` component.
- Removed legacy home/remote interfaces and JNDI lookup.
- Removed `ejb-jar.xml` and `jboss.xml`.

No entity beans / message-driven beans existed in this sample.

---

## Task 4 — Struts 1 -> Spring MVC
Migration:
- `RegisterAction` implemented as Spring MVC endpoints:
  - `GET /register` and `POST /register`
- `SecureAction` implemented as:
  - `GET /secure`
- JSP views migrated to Thymeleaf templates.

---

## Task 5 — Application Server -> Spring Boot
- EAR/WAR deployment artifacts removed; now built as a Spring Boot jar.
- Runs on embedded Tomcat.
- Spring Security form login configured:
  - login page: `/jaas/login`
  - login processing: `/login`
  - logout: `/jaas/logoff`

---

## Task 6 — Java Language Upgrade to Java 21
- Maven compiler target is `release 21` (in `pom.xml`).
- Modern idioms used where appropriate:
  - `record` DTO in the registration controller
  - `var` for local inference

---

## Task 7 — Security Remediation
- Removed Struts 1 and legacy JBoss/JAAS runtime components (no Struts jars, no ActionServlet, no legacy security descriptors).
- Added Spring Security:
  - `anyRequest().authenticated()`
  - form login + logout
  - CSRF enabled (templates include CSRF token)

---

## Task 8 — Testing
- Unit tests (JUnit 5 + Mockito):
  - `RegistrationValidatorTest`
  - `RegistrationBeanTest`
  - `AuthUserServiceTest`
- Integration test (@SpringBootTest):
  - `LegacyAppIntegrationTest` covers: login -> register -> secure page.

Tooling note:
- Mockito inline mocks did not work correctly on Java 25 in this environment.
- A Mockito `MockMaker` override was added to force `SubclassByteBuddyMockMaker` for stable tests.

---

## Full List of Files Changed
### Added
- `pom.xml`
- `src/main/java/com/example/LegacyAppApplication.java`
- `src/main/java/com/example/security/AuthUserService.java`
- `src/main/java/com/example/security/SecurityConfig.java`
- `src/main/java/com/example/web/AuthController.java`
- `src/main/java/com/example/web/IndexController.java`
- `src/main/java/com/example/registration/RegistrationValidator.java`
- `app-web/src/com/example/RegisterAction.java`
- `app-web/src/com/example/SecureAction.java`
- `src/main/resources/templates/index.html`
- `src/main/resources/templates/register.html`
- `src/main/resources/templates/register_confirmation.html`
- `src/main/resources/templates/secure/index.html`
- `src/main/resources/templates/jaas/login.html`
- `src/main/resources/templates/jaas/login_error.html`
- Tests:
  - `src/test/java/com/example/registration/RegistrationValidatorTest.java`
  - `src/test/java/com/example/RegistrationBeanTest.java`
  - `src/test/java/com/example/security/AuthUserServiceTest.java`
  - `src/test/java/com/example/LegacyAppIntegrationTest.java`
- Mockito config override:
  - `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`

### Updated
- `app-ejb/src/com/example/RegistrationBean.java` (legacy EJB -> Spring `@Service`)
- `src/main/resources/templates/secure/index.html` (template expression fix)

### Deleted
- Legacy committed JARs:
  - `app-web/WebRoot/WEB-INF/lib/*.jar`
- Legacy descriptors:
  - `app-web/WebRoot/WEB-INF/struts-config.xml`
  - `app-web/WebRoot/WEB-INF/web.xml`
  - `app-web/WebRoot/WEB-INF/jboss-web.xml`
  - `app-ejb/src/META-INF/ejb-jar.xml`
  - `app-ejb/src/META-INF/jboss.xml`
  - `app-ejb/src/META-INF/MANIFEST.MF`
  - `app-web/WebRoot/META-INF/MANIFEST.MF`
  - `app-ear/META-INF/application.xml`
- Legacy EJB/JAAS interfaces:
  - `app-ejb/src/com/example/RegistrationEJB.java`
  - `app-ejb/src/com/example/RegistrationHome.java`
  - `app-ejb/src/com/example/jaas/MyLoginModule.java`
  - `app-ejb/src/com/example/jaas/MyPrincipal.java`
- Legacy JSP views:
  - `app-web/WebRoot/**/*.jsp`

---

## Remaining Manual Tasks / Follow-ups
1) Clean up empty legacy directory structures (leftover `WebRoot/app-ear/app-ejb` folders without functional content).
2) Replace the in-memory admin user with a real user store (DB/LDAP/OAuth) per business requirements.
3) Add more end-to-end tests for logout, error cases, and invalid form submissions.
4) If formal CVE scanning is required, add OWASP dependency-check/OSV scanning in the CI pipeline.

---

## Lessons Learned
1) When modernizing legacy security, stabilize login/CSRF/roles first; otherwise view-layer and “happy path” tests can mislead.
2) “Replace dependencies” is not only a build-process change: runtime classpath must be clean of legacy JARs.
3) On very new JVM/tooling versions, test utilities (Mockito/ByteBuddy) may break; plan for mock-maker overrides or dependency pinning.
4) For small applications, rewriting the legacy core directly into Spring annotations can be faster than maintaining two build/runtime models at once.

<!-- markdownlint-disable -->
# MODERNIZATION_REPORT.md

## Executive Summary
This modernization converted a legacy Java EE application (JBoss 4 + Struts 1.1 + EJB 2.1 + JSP) into a self-contained Spring Boot 3.x application (Java 21 target) with:
- Spring MVC controllers instead of Struts Actions
- Spring Security form-based login/auth instead of JAAS/JBoss login-config
- Spring Boot + embedded Tomcat instead of EAR/WAR deployment
- Thymeleaf templates instead of legacy JSP views
- all committed legacy JARs and deployment descriptors removed

The application compiles successfully with Maven and passes unit + integration tests (`mvn test`).

---

## Global vs Local State (what was installed)
This modernization did not install any OS-level packages globally (no `brew install`, `apt/yum`, etc.).
What was changed/created during the process is limited to the repository workspace:
- Project code and config files: added/updated/deleted in `javaee-legacy-app-example-master/`.
- Maven dependencies: downloaded by `mvn` and stored in a local Maven repository inside the workspace via `-Dmaven.repo.local=./.m2/repository`.
  - Earlier attempts without `-Dmaven.repo.local` failed due to sandbox write restrictions on `~/.m2`, but the successful runs still used the workspace-local `.m2`.
- No global Java agent/tooling changes were made; any runtime processes (`spring-boot:run`) were only temporary for validation.

---

## Task 1 — Archaeology Report

### Before Architecture (legacy)
```
                +------------------+
Browser -----> |  app-web.war     |
                |                  |
                |  Struts Action   |
                |  *.do ->         |
                |  ActionServlet   |
                |                  |
                |  JSP Views       |
                |                  |
                |  WEB-INF/web.xml |
                +---------+--------+
                          |
                          | JNDI lookup (home/remote)
                          v
                +------------------+
                |  app-ejb.jar     |
                |  EJB 2.1         |
                |  RegistrationBean|
                |  ejb-jar.xml     |
                +------------------+

Security:
- JBoss JAAS login-config / JBoss security-domain
- MyLoginModule validates admin/123456 and assigns role SIE
```

### After Architecture (modern)
```
                +---------------------------+
Browser -----> | Spring Boot (embedded     |
                | Tomcat, self-contained)  |
                +--------------+------------+
                               |
                               | Spring MVC @Controllers
                               v
                 +-------------------------------+
                 | /register, /secure, /jaas/* |
                 +-------------------------------+
                               |
                               | service layer (Spring @Service)
                               v
                 +-------------------------------+
                 | RegistrationBean + services  |
                 | (no EJB home/remote/JNDI)   |
                 +-------------------------------+
                               |
                               | Spring Security (form login)
                               v
                 +-------------------------------+
                 | authenticated endpoints + CSRF|
                 +-------------------------------+
```

### Legacy Components (enumeration)

#### EJBs (from `ejb-jar.xml`)
1. Session Bean
   - Name: `RegistrationEJB`
   - Type: Stateless session
   - EJB class: `com.example.RegistrationBean`
   - Home/Remote:
     - `com.example.RegistrationHome` (home)
     - `com.example.RegistrationEJB` (remote)

No entity beans and no message-driven beans were present.

#### Struts Actions (from `struts-config.xml`)
1. `path="/register"`, `type="com.example.RegisterAction"`
   - Forward: `success` -> `/register_confirmation.jsp`

Note: `SecureAction` existed in code but was not mapped in `struts-config.xml`.

#### JSP Views (from `app-web/WebRoot`)
- `index.jsp`
- `register.jsp`
- `register_confirmation.jsp`
- `secure/index.jsp`
- `jaas/login.jsp`
- `jaas/login_error.jsp`
- `jaas/logoff.jsp`

#### Deployment Descriptors
- `app-ejb/src/META-INF/ejb-jar.xml`
- `app-ejb/src/META-INF/jboss.xml`
- `app-web/WebRoot/WEB-INF/struts-config.xml`
- `app-web/WebRoot/WEB-INF/web.xml`
- `app-web/WebRoot/WEB-INF/jboss-web.xml`
- `app-ear/META-INF/application.xml`

### Committed Library Versions (from `META-INF/MANIFEST.MF` inside committed JARs)
- `struts.jar` — Implementation-Version: `1.1`
- `struts-legacy.jar` — Implementation-Version: `1.0`
- `commons-beanutils.jar` — Implementation-Version: `1.6`
- `commons-collections.jar` — Implementation-Version: `2.1`
- `commons-digester.jar` — Implementation-Version: `1.5`
- `commons-fileupload.jar` — Implementation-Version: `1.0`
- `commons-lang.jar` — Implementation-Version: `1.0.1`
- `commons-logging.jar` — Implementation-Version: `1.0.3`
- `commons-validator.jar` — Implementation-Version: `1.0.2`
- `jakarta-oro.jar` — (version not captured from the manifest output)

### CVEs flagged (legacy high/critical focus)
These HIGH/CRITICAL issues were addressed primarily by:
1) removing legacy JARs from the runtime classpath
2) replacing Struts/JBoss security with modern Spring Boot/Spring Security dependencies

HIGH/CRITICAL (most important):
- Apache Struts 1.x
  - `CVE-2016-1181` (HIGH) — ActionServlet issues involving multipart leading to RCE/DoS
  - `CVE-2016-1182` (HIGH) — XSS/DoS class for Struts 1.x
  - `CVE-2014-0114` (HIGH) — classloader manipulation via ActionForm class parameter
- Apache Commons Collections
  - `CVE-2015-7501` (CRITICAL) — unsafe deserialization (InvokerTransformer chain)
- Apache Commons BeanUtils
  - `CVE-2014-0114` (HIGH) — classloader manipulation via class property

Additional risks (removed together with dependency jars):
- Apache Commons FileUpload (legacy 1.0) had known DoS and insecure temporary-file patterns; resolved by full removal/replacement through Spring Boot.

This report did not run a dedicated CVE scanner; instead it used:
- exact version extraction from `MANIFEST.MF`
- removal of legacy JARs from `WEB-INF/lib`
- reliance on Spring Boot dependency management for a modern dependency graph

### Estimated Migration Effort + Top Risks
Estimate for tasks 1–8 on this small sample: ~40–70 hours.

Top 5 risk areas:
1) Security behavior equivalence (redirects, CSRF, roles/principal naming)
2) View migration differences (template variables, error handling)
3) Dependency graph correctness (transitive dependency upgrades/removals)
4) Build tool migration (Maven local repo / network constraints in CI)
5) Test reliability across new JVM/tooling (Mockito inline vs Java 25/ByteBuddy)

---

## Task 2 — Build Tool Introduction (Maven)
- Added `pom.xml` (Spring Boot parent) and Maven compiler target `release=21`.
- Removed legacy committed JARs and deployment descriptors.
- Added Spring Boot dependency management to fully govern runtime libraries.
- `mvn test` succeeds (unit + integration).

---

## Task 3 — EJB 2.x -> Spring services / annotations
Legacy had one EJB 2.1 stateless session bean (`RegistrationEJB`).

Migration:
- `com.example.RegistrationBean` was rewritten as a Spring `@Service` component.
- Removed all legacy home/remote interfaces and JNDI lookup.
- `ejb-jar.xml` and `jboss.xml` were removed.

No entity beans / message-driven beans were present, so JPA `@Entity` migration was not applicable to this sample.

---

## Task 4 — Struts 1 -> Spring MVC
Migration:
- `RegisterAction` was implemented as Spring MVC endpoints:
  - `GET /register` and `POST /register`
- `SecureAction` was implemented as Spring MVC endpoint:
  - `GET /secure`
- JSP views were migrated to Thymeleaf templates.

---

## Task 5 — Application Server -> Spring Boot
- EAR/WAR deployment artifacts were removed; the application is now built as a Spring Boot jar.
- The application runs on embedded Tomcat.
- Spring Security form login configured:
  - login page: `/jaas/login`
  - login processing: `/login`
  - logout: `/jaas/logoff`

---

## Task 6 — Java Language Upgrade to Java 21
- Maven compiler target is `release 21` (in `pom.xml`).
- Modern idioms used where appropriate:
  - `record` DTO in the registration controller
  - `var` for local inference

---

## Task 7 — Security Remediation
What was done:
- Struts 1 and JBoss/JAAS vulnerable runtime components removed (no Struts jars, no ActionServlet, no legacy descriptors).
- Spring Security added:
  - protection for all endpoints (`anyRequest().authenticated()`)
  - form-based login + logout
  - CSRF remains enabled (templates include CSRF token)

Legacy Struts vulnerability audit:
- Struts runtime dependency removed -> prevents Struts classloader manipulation and legacy ActionForm-related issues post-migration.

---

## Task 8 — Testing
- Unit tests (JUnit 5 + Mockito):
  - `RegistrationValidatorTest`
  - `RegistrationBeanTest`
  - `AuthUserServiceTest`
- Integration test (@SpringBootTest):
  - `LegacyAppIntegrationTest` covers: login -> register -> secure page.

Tooling note:
- Mockito inline mocks did not work correctly on Java 25 in this environment.
- A Mockito `MockMaker` override was added to force `SubclassByteBuddyMockMaker` for stable tests.

---

## Full List of Files Changed
### Added
- `pom.xml`
- `src/main/java/com/example/LegacyAppApplication.java`
- `src/main/java/com/example/security/AuthUserService.java`
- `src/main/java/com/example/security/SecurityConfig.java`
- `src/main/java/com/example/web/AuthController.java`
- `src/main/java/com/example/web/IndexController.java`
- `src/main/java/com/example/registration/RegistrationValidator.java`
- `app-web/src/com/example/RegisterAction.java`
- `app-web/src/com/example/SecureAction.java`
- Thymeleaf templates:
  - `src/main/resources/templates/index.html`
  - `src/main/resources/templates/register.html`
  - `src/main/resources/templates/register_confirmation.html`
  - `src/main/resources/templates/secure/index.html`
  - `src/main/resources/templates/jaas/login.html`
  - `src/main/resources/templates/jaas/login_error.html`
- Tests:
  - `src/test/java/com/example/registration/RegistrationValidatorTest.java`
  - `src/test/java/com/example/RegistrationBeanTest.java`
  - `src/test/java/com/example/security/AuthUserServiceTest.java`
  - `src/test/java/com/example/LegacyAppIntegrationTest.java`
- Mockito config override:
  - `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`

### Updated
- `app-ejb/src/com/example/RegistrationBean.java` (legacy EJB -> Spring `@Service`)
- `src/main/resources/templates/secure/index.html` (template expression fix)

### Deleted
- Legacy committed JARs:
  - `app-web/WebRoot/WEB-INF/lib/*.jar`
- Legacy Struts/Servlet/Web/JBoss descriptors:
  - `app-web/WebRoot/WEB-INF/struts-config.xml`
  - `app-web/WebRoot/WEB-INF/web.xml`
  - `app-web/WebRoot/WEB-INF/jboss-web.xml`
  - `app-ejb/src/META-INF/ejb-jar.xml`
  - `app-ejb/src/META-INF/jboss.xml`
  - `app-ear/META-INF/application.xml`
- Legacy EJB/JAAS interfaces:
  - `app-ejb/src/com/example/RegistrationEJB.java`
  - `app-ejb/src/com/example/RegistrationHome.java`
  - `app-ejb/src/com/example/jaas/MyLoginModule.java`
  - `app-ejb/src/com/example/jaas/MyPrincipal.java`
- Legacy JSP views:
  - `app-web/WebRoot/**/*.jsp`

---

## Remaining Manual Tasks / Follow-ups
1) Clean up empty legacy directory structures (leftover `WebRoot/app-ear/app-ejb` folders without functional content).
2) Replace the in-memory admin user with a real user store (DB/LDAP/OAuth) per business requirements.
3) Add more end-to-end tests for logout, error cases, and invalid form submissions.
4) If formal CVE scanning is required, add OWASP dependency-check/OSV scanning in the CI pipeline.

---

## Lessons Learned
1) When modernizing legacy security, stabilize login/CSRF/roles first; otherwise view-layer and “happy path” tests can mislead.
2) “Replace dependencies” is not only a build-process change: runtime classpath must be clean of legacy JARs.
3) On very new JVM/tooling versions, test utilities (Mockito/ByteBuddy) may break; plan for mock-maker overrides or dependency pinning.
4) For small applications, rewriting the legacy core directly into Spring annotations can be faster than maintaining two build/runtime models at once.

<!-- markdownlint-disable -->
# MODERNIZATION_REPORT.md

## Executive Summary
This modernization converted a legacy Java EE application (JBoss 4 + Struts 1.1 + EJB 2.1 + JSP) into a self-contained Spring Boot 3.x application (Java 21 target) with:
- Spring MVC controllers instead of Struts Actions
- Spring Security form-based login/authorization instead of JAAS/JBoss login-config
- Spring Boot + embedded Tomcat instead of EAR/WAR deployment
- Thymeleaf templates instead of legacy JSP views
- all committed legacy JARs and deployment descriptors removed

The application compiles successfully with Maven and passes unit + integration tests (`mvn test`).

---

## Global vs Local State (what was installed)
This modernization did not install any OS-level packages globally (no `brew install`, `apt/yum`, etc.).
What was changed/created during the process is limited to the repository workspace:
- Project code and configuration files: added/updated/deleted in `javaee-legacy-app-example-master/`.
- Maven dependencies: downloaded by `mvn` and stored in a *local* Maven repository inside the workspace via `-Dmaven.repo.local=./.m2/repository`.
  - Earlier attempts without `-Dmaven.repo.local` failed due to sandbox write restrictions on `~/.m2`, but the successful runs still used the workspace-local `.m2`.
- No global Java agent/tooling changes were made; any runtime processes (`spring-boot:run`) were only temporary for validation.

---

## Task 1 — Archaeology Report

### Before Architecture (legacy)
```
                +------------------+
Browser -----> |  app-web.war     |
                |                  |
                |  Struts Action   |
                |  *.do ->         |
                |  ActionServlet   |
                |                  |
                |  JSP Views       |
                |                  |
                |  WEB-INF/web.xml  |
                +---------+--------+
                          |
                          | JNDI lookup (home/remote)
                          v
                +------------------+
                |  app-ejb.jar     |
                |  EJB 2.1         |
                |  RegistrationBean|
                |  ejb-jar.xml     |
                +------------------+

Security:
- JBoss JAAS login-config / JBoss security-domain
- MyLoginModule validates admin/123456 and assigns role SIE
```

### After Architecture (modern)
```
                +---------------------------+
Browser -----> | Spring Boot (embedded     |
                | Tomcat, self-contained)  |
                +--------------+------------+
                               |
                               | Spring MVC @Controllers
                               v
                 +-------------------------------+
                 | /register, /secure, /jaas/* |
                 +-------------------------------+
                               |
                               | service layer (Spring @Service)
                               v
                 +-------------------------------+
                 | RegistrationBean + services  |
                 | (no EJB home/remote/JNDI)   |
                 +-------------------------------+
                               |
                               | Spring Security (form login)
                               v
                 +-------------------------------+
                 | authenticated endpoints + CSRF|
                 +-------------------------------+
```

### Legacy Components (enumeration)

#### EJBs (from `ejb-jar.xml`)
1. Session Bean
   - Name: `RegistrationEJB`
   - Type: Stateless session
   - EJB class: `com.example.RegistrationBean`
   - Home/Remote:
     - `com.example.RegistrationHome` (home)
     - `com.example.RegistrationEJB` (remote)

No entity beans and no message-driven beans were present.

#### Struts Actions (from `struts-config.xml`)
1. `path="/register"`, `type="com.example.RegisterAction"`
   - Forward: `success` -> `/register_confirmation.jsp`

Note: `SecureAction` existed in code, but it was not mapped in `struts-config.xml`.

#### JSP Views
- `index.jsp`
- `register.jsp`
- `register_confirmation.jsp`
- `secure/index.jsp`
- `jaas/login.jsp`
- `jaas/login_error.jsp`
- `jaas/logoff.jsp`

#### Deployment Descriptors
- `app-ejb/src/META-INF/ejb-jar.xml`
- `app-ejb/src/META-INF/jboss.xml`
- `app-web/WebRoot/WEB-INF/struts-config.xml`
- `app-web/WebRoot/WEB-INF/web.xml`
- `app-web/WebRoot/WEB-INF/jboss-web.xml`
- `app-ear/META-INF/application.xml`

### Committed Library Versions (from `META-INF/MANIFEST.MF` inside committed JARs)
- `struts.jar` — Implementation-Version: `1.1` (Struts Framework)
- `struts-legacy.jar` — Implementation-Version: `1.0`
- `commons-beanutils.jar` — Implementation-Version: `1.6`
- `commons-collections.jar` — Implementation-Version: `2.1`
- `commons-digester.jar` — Implementation-Version: `1.5`
- `commons-fileupload.jar` — Implementation-Version: `1.0`
- `commons-lang.jar` — Implementation-Version: `1.0.1`
- `commons-logging.jar` — Implementation-Version: `1.0.3`
- `commons-validator.jar` — Implementation-Version: `1.0.2`
- `jakarta-oro.jar` — (version not captured in manifest output)

### CVEs flagged (legacy high/critical focus)
The following HIGH/CRITICAL CVEs (and well-documented critical classes) were addressed primarily by:
1) removing legacy JARs from the runtime classpath, and
2) replacing Struts/JBoss security with modern Spring Boot/Spring Security dependencies.

HIGH/CRITICAL (most important):
- Apache Struts 1.x
  - `CVE-2016-1181` (HIGH) — issues in ActionServlet handling / multipart leading to RCE/DoS class
  - `CVE-2016-1182` (HIGH) — XSS/DoS class for Struts 1.x
  - `CVE-2014-0114` (HIGH) — classloader manipulation via ActionForm class parameter
- Apache Commons Collections
  - `CVE-2015-7501` (CRITICAL) — unsafe deserialization (InvokerTransformer chain)
- Apache Commons BeanUtils
  - `CVE-2014-0114` (HIGH) — classloader manipulation via class property

Additional risks (removed together with dependency jars):
- Apache Commons FileUpload (legacy 1.0) had known DoS and insecure temporary-file patterns in early 1.x line; resolved by full removal/replacement through Spring Boot.

In this context, a complete automated CVE scan was not executed; instead the approach was:
- extract exact versions from `MANIFEST.MF`
- remove legacy JARs from `WEB-INF/lib`
- rely on Spring Boot dependency management for a safe modern dependency graph

### Estimated Migration Effort + Top Risks
Estimate for tasks 1-8 on this small sample: ~40–70 hours.
This includes refactoring to Spring Boot, migrating security, the view layer, and tests.

Top 5 risk areas:
1) Security behavior equivalence (redirects, CSRF, roles/principal naming)
2) View migration differences (template variables, error handling)
3) Dependency graph correctness (transitive dependency upgrades/removals)
4) Build tool migration (Maven local repo / network constraints in CI)
5) Test reliability across new JVM/tooling (Mockito inline vs Java 25/ByteBuddy)

---

## Task 2 — Build Tool Introduction (Maven)
- Added `pom.xml` (Spring Boot parent) and Maven compiler target `release=21`.
- Removed legacy committed JARs and deployment descriptors.
- Added Spring Boot dependency management to fully govern runtime libraries.
- `mvn test` is successful (unit + integration).

---

## Task 3 — EJB 2.x -> Spring Services / CDI (annotations)
Legacy had one EJB 2.1 stateless session bean (`RegistrationEJB`).

Migration:
- `com.example.RegistrationBean` was rewritten as a Spring `@Service`.
- Removed all legacy home/remote interfaces and JNDI lookup:
  - deleted: `RegistrationEJB.java`, `RegistrationHome.java`
  - replaced Struts `RegisterAction` with a Spring MVC controller
- `ejb-jar.xml` and `jboss.xml` were removed (annotation-based config).

There were no entity beans / message-driven beans, so a JPA `@Entity` replacement was not applicable for this specific sample.

---

## Task 4 — Struts 1 -> Spring MVC
Legacy Struts routing:
- `struts-config.xml`: `path="/register"` -> `RegisterAction`

Migration:
- `app-web/src/com/example/RegisterAction.java` was rewritten as a Spring `@Controller`.
- `app-web/src/com/example/SecureAction.java` was implemented as a Spring MVC controller mapped to `/secure`.
- Routing is via Spring annotations:
  - `@GetMapping("/register")`, `@PostMapping("/register")`
  - `@RequestMapping("/secure")`
- View layer migration:
  - legacy JSPs were replaced by Thymeleaf templates.

---

## Task 5 — Application Server -> Spring Boot (embedded Tomcat)
- EAR/WAR deployment artifacts were removed; the application is built as a Spring Boot jar.
- The application runs on embedded Tomcat.
- Spring Security form login was configured:
  - loginPage: `/jaas/login`
  - processingUrl: `/login`
  - logoutUrl: `/jaas/logoff`

---

## Task 6 — Java Language Upgrade to Java 21
- Maven compiler target is `release 21` (in `pom.xml`).
- Modern idioms were used where appropriate:
  - `record` DTO (in `RegisterAction`)
  - `var` (in the register handler)

---

## Task 7 — Security Remediation
What was done:
- Removed Struts 1 and JBoss/JAAS vulnerable components.
- Added Spring Security:
  - protection for all endpoints (`anyRequest().authenticated()`)
  - form-based login and logout
  - CSRF remains enabled (Thymeleaf templates include the CSRF hidden field)
- Roles:
  - an in-memory admin user with role `SIE` for the secure area

Audit against legacy Struts vulnerabilities:
- Struts runtime dependency was removed.
- Legacy `ActionServlet`, `struts-config.xml`, and Struts JARs do not exist in the project -> prevents classloader manipulation and ActionForm injection class issues post-migration.

---

## Task 8 — Testing
- Unit tests (JUnit 5 + Mockito):
  - `RegistrationValidatorTest`
  - `RegistrationBeanTest`
  - `AuthUserServiceTest`
- Integration test (@SpringBootTest):
  - `LegacyAppIntegrationTest` covers: login -> register -> secure page.

Important tooling note:
- Mockito inline mocks did not work correctly on Java 25 in this environment.
- A Mockito `MockMaker` override was added to force `SubclassByteBuddyMockMaker` for stable tests.

---

## Files Changed (added/updated/deleted)
### Added
- `pom.xml`
- `src/main/java/com/example/LegacyAppApplication.java`
- `src/main/java/com/example/security/AuthUserService.java`
- `src/main/java/com/example/security/SecurityConfig.java`
- `src/main/java/com/example/web/AuthController.java`
- `src/main/java/com/example/web/IndexController.java`
- `src/main/java/com/example/registration/RegistrationValidator.java`
- `app-web/src/com/example/RegisterAction.java` (Spring controller version)
- `app-web/src/com/example/SecureAction.java` (Spring controller version)
- `src/main/resources/templates/index.html`
- `src/main/resources/templates/register.html`
- `src/main/resources/templates/register_confirmation.html`
- `src/main/resources/templates/secure/index.html`
- `src/main/resources/templates/jaas/login.html`
- `src/main/resources/templates/jaas/login_error.html`
- Unit tests:
  - `src/test/java/com/example/registration/RegistrationValidatorTest.java`
  - `src/test/java/com/example/RegistrationBeanTest.java`
  - `src/test/java/com/example/security/AuthUserServiceTest.java`
- Integration test:
  - `src/test/java/com/example/LegacyAppIntegrationTest.java`
- Mockito config override:
  - `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`

### Updated
- `app-ejb/src/com/example/RegistrationBean.java` (EJB -> Spring `@Service`)
- `src/main/resources/templates/secure/index.html` (template expression fix)

### Deleted
- Legacy JARs:
  - `app-web/WebRoot/WEB-INF/lib/*.jar` (Struts 1.1 + commons-* line)
- Legacy descriptors:
  - `app-web/WebRoot/WEB-INF/struts-config.xml`
  - `app-web/WebRoot/WEB-INF/web.xml`
  - `app-web/WebRoot/WEB-INF/jboss-web.xml`
  - `app-ejb/src/META-INF/ejb-jar.xml`
  - `app-ejb/src/META-INF/jboss.xml`
  - `app-ear/META-INF/application.xml`
- Legacy EJB/JAAS interfaces:
  - `app-ejb/src/com/example/RegistrationEJB.java`
  - `app-ejb/src/com/example/RegistrationHome.java`
  - `app-ejb/src/com/example/jaas/MyLoginModule.java`
  - `app-ejb/src/com/example/jaas/MyPrincipal.java`
- Legacy JSP views:
  - `app-web/WebRoot/**/*.jsp`
- Legacy manifests:
  - `app-ejb/src/META-INF/MANIFEST.MF`
  - `app-web/WebRoot/META-INF/MANIFEST.MF`

---

## Remaining Manual Tasks / Follow-ups
1) Clean up empty legacy directory structures (leftover `WebRoot/app-ear/app-ejb` folders without functional content).
2) Replace the in-memory admin user with a real user store (DB/LDAP/OAuth) per business requirements.
3) Add more end-to-end tests for logout, error cases, and invalid form submissions.
4) If formal CVE scanning is required, add OWASP dependency-check/OSV scanning in the CI pipeline.

---

## Lessons Learned
1) When modernizing legacy security, stabilize login/CSRF/roles first; otherwise view-layer and “happy path” tests can mislead.
2) “Replace dependencies” is not only a build-process change: runtime classpath must be clean of legacy JARs.
3) On very new JVM/tooling versions, test utilities (Mockito/ByteBuddy) may break; plan for mock-maker overrides or dependency pinning.
4) For small applications, rewriting the legacy core directly into Spring annotations can be faster than maintaining two build/runtime models at once.

<!-- markdownlint-disable -->
# MODERNIZATION_REPORT.md

## Executive Summary
Тази модернизация превърна legacy Java EE приложение (JBoss 4 + Struts 1.1 + EJB 2.1 + JSP) в самостоятелна Spring Boot 3.x апликация (Java 21 target) със:
- Spring MVC controllers вместо Struts Actions
- Spring Security форм базирана login/защита вместо JAAS/JBoss login-config
- Spring Boot + embedded Tomcat вместо EAR/WAR deployment
- Thymeleaf templates вместо legacy JSP views
- премахнати всички committed legacy JAR-и и deployment descriptors

Приложението компилира успешно с Maven и минава unit + integration тестове (`mvn test`).

## Global vs Local State (what was installed)
This modernization did not install OS-level packages globally (no `brew install`, `apt/yum`, etc.).
What was changed/created during the process is limited to the repository workspace:
- Project code and config files: added/updated/deleted in `javaee-legacy-app-example-master/`.
- Maven dependencies: downloaded by `mvn` and stored in a *local* Maven repository inside the workspace via `-Dmaven.repo.local=./.m2/repository`.
  - Earlier attempts without `-Dmaven.repo.local` failed due to sandbox write restrictions on `~/.m2`, but the successful runs still used the workspace-local `.m2`.
- No global Java agent/tooling changes were made; any runtime processes (`spring-boot:run`) were only temporary to validate the server.

---

## Task 1 — Archaeology Report

### Before Architecture (legacy)
```
                +------------------+
Browser -----> |  app-web.war     |
                |                  |
                |  Struts Action   |
                |  *.do ->         |
                |  ActionServlet   |
                |                  |
                |  JSP Views       |
                |                  |
                |  WEB-INF/web.xml  |
                +---------+--------+
                          |
                          | JNDI lookup (home/remote)
                          v
                +------------------+
                |  app-ejb.jar     |
                |  EJB 2.1         |
                |  RegistrationBean|
                |  ejb-jar.xml     |
                +------------------+

Security:
- JBoss JAAS login-config / JBoss security-domain
- MyLoginModule validates admin/123456 and assigns role SIE
```

### Legacy Components (enumeration)

#### EJBs (from `ejb-jar.xml`)
1. Session Bean
   - Name: `RegistrationEJB`
   - Type: Stateless session
   - EJB class: `com.example.RegistrationBean`
   - Home/Remote:
     - `com.example.RegistrationHome` (home)
     - `com.example.RegistrationEJB` (remote)

No entity beans and no message-driven beans were present.

#### Struts Actions (from `struts-config.xml`)
1. `path="/register"`, `type="com.example.RegisterAction"`
   - Forward: `success` -> `/register_confirmation.jsp`

Note: `SecureAction` existed in code, but it was not mapped in `struts-config.xml`.

#### JSP Views
- `index.jsp`
- `register.jsp`
- `register_confirmation.jsp`
- `secure/index.jsp`
- `jaas/login.jsp`
- `jaas/login_error.jsp`
- `jaas/logoff.jsp`

#### Deployment Descriptors
- `app-ejb/src/META-INF/ejb-jar.xml`
- `app-ejb/src/META-INF/jboss.xml`
- `app-web/WebRoot/WEB-INF/struts-config.xml`
- `app-web/WebRoot/WEB-INF/web.xml`
- `app-web/WebRoot/WEB-INF/jboss-web.xml`
- `app-ear/META-INF/application.xml`

### Committed Library Versions (from `META-INF/MANIFEST.MF` inside committed JARs)
- `struts.jar` — Implementation-Version: `1.1` (Struts Framework)
- `struts-legacy.jar` — Implementation-Version: `1.0`
- `commons-beanutils.jar` — Implementation-Version: `1.6`
- `commons-collections.jar` — Implementation-Version: `2.1`
- `commons-digester.jar` — Implementation-Version: `1.5`
- `commons-fileupload.jar` — Implementation-Version: `1.0`
- `commons-lang.jar` — Implementation-Version: `1.0.1`
- `commons-logging.jar` — Implementation-Version: `1.0.3`
- `commons-validator.jar` — Implementation-Version: `1.0.2`
- `jakarta-oro.jar` — (version not captured in manifest output)

### CVEs flagged (legacy high/critical focus)
Следните HIGH/CRITICAL CVE-и (или добре документирани критични класове уязвимости) бяха елиминирани поради:
1) премахване на legacy JAR-ите от runtime classpath
2) замяна на Struts/JBoss security с модерни Spring Boot/Spring Security зависимости

HIGH/CRITICAL (най-важни):
- Apache Struts 1.x
  - `CVE-2016-1181` (HIGH) — issues in ActionServlet handling / multipart leading to RCE/DoS class
  - `CVE-2016-1182` (HIGH) — XSS/DoS class for Struts 1.x
  - `CVE-2014-0114` (HIGH) — classloader manipulation via ActionForm class parameter
- Apache Commons Collections
  - `CVE-2015-7501` (CRITICAL) — unsafe deserialization (InvokerTransformer chain)
- Apache Commons BeanUtils
  - `CVE-2014-0114` (HIGH) — classloader manipulation via class property

Additional (removed together with dependency jars):
- Apache Commons FileUpload (legacy 1.0) had known DoS and insecure temporary-file patterns in early 1.x line; resolved by full removal/replacement through Spring Boot.

За да “твърдим” пълно покритие, в този контекст не използвах автоматичен CVE scanner, а комбинирах:
- точни версии от MANIFEST.MF
- премахване на legacy JAR-и от `WEB-INF/lib`
- и замяна с модерната dependency graph чрез Spring Boot dependency management

### Estimated Migration Effort + Top Risks
Оценка (общо за tasks 1-8 за това конкретно малко sample): ~40-70 часа.
Това включва рефактор към Spring Boot, migration на security, view layer и тестове.

Top 5 Risk Areas:
1) Security behavior equivalence (redirects, CSRF, roles/principal name)
2) View migration differences (template model variables, error handling)
3) Dependency graph correctness (transitive dependency upgrades/removals)
4) Build tool migration (Maven local repo / network constraints in CI)
5) Test reliability across new JVM/tooling (Mockito inline vs Java 25/ByteBuddy)

---

## Task 2 — Build Tool Introduction (Maven)
- Добавен е `pom.xml` (Spring Boot parent) и Maven compiler target `release=21`.
- Legacy committed JAR-и и deployment descriptors бяха премахнати.
- Добавена е Spring Boot dependency graph, която изцяло управлява runtime библиотеки.
- `mvn test` е успешен (unit + integration).

---

## Task 3 — EJB 2.x -> Spring Services / annotations
Legacy имаше един EJB 2.1 stateless session bean (`RegistrationEJB`).
Миграцията:
- `com.example.RegistrationBean` беше пренаписан като Spring `@Service`
- премахнати са всички legacy home/remote интерфейси и JNDI lookup:
  - deleted: `RegistrationEJB.java`, `RegistrationHome.java`
  - пренаписан: Struts `RegisterAction` -> Spring MVC controller
- `ejb-jar.xml` и `jboss.xml` са изтрити (annotation-based config).

Нямаше entity beans / message-driven beans, така че JPA `@Entity` замяна не е приложима за този конкретен sample.

---

## Task 4 — Struts 1 -> Spring MVC
Legacy Struts routing:
- `struts-config.xml`: `path="/register"` -> `RegisterAction`

Миграцията:
- `app-web/src/com/example/RegisterAction.java` е пренаписан като Spring `@Controller`
- `app-web/src/com/example/SecureAction.java` е пренаписан като Spring MVC controller
- Routing е чрез Spring annotations:
  - `@GetMapping("/register")`, `@PostMapping("/register")`
  - `@RequestMapping("/secure")`
- View layer е мигриран:
  - legacy JSP са заменени от Thymeleaf templates

---

## Task 5 — Application Server -> Spring Boot (embedded Tomcat)
- EAR/WAR дескрипторите са премахнати, а build-ът е Spring Boot jar.
- Приложението работи с embedded Tomcat.
- Използвана е Spring Security form login:
  - loginPage: `/jaas/login`
  - processingUrl: `/login`
  - logoutUrl: `/jaas/logoff`

---

## Task 6 — Java Language Upgrade to Java 21
- Maven compiler target е `release 21` (в `pom.xml`).
- Използвани са модерни идиоми в migration кода:
  - `record` DTO (в `RegisterAction`)
  - `var` (в register handler)

---

## Task 7 — Security Remediation
Какво е направено:
- Премахнати са Struts 1 и JBoss/JAAS уязвимите компоненти.
- Добавен е Spring Security:
  - защита за всички endpoints (`anyRequest().authenticated()`)
  - form-based login и logout
  - CSRF остава включена (template-ите включват CSRF hidden поле)
- Roles:
  - in-memory admin user с role `SIE` за secure page.

Аудит за legacy Struts vulnerabilities:
- Struts runtime dependency е премахната.
- Legacy `ActionServlet`, `struts-config.xml` и Struts JAR-ове не съществуват в проекта -> classloader manipulation и ActionForm injection класове не са налични post-migration.

---

## Task 8 — Testing
- Unit tests (JUnit 5 + Mockito):
  - `RegistrationValidatorTest`
  - `RegistrationBeanTest`
  - `AuthUserServiceTest`
- Integration test (@SpringBootTest):
  - `LegacyAppIntegrationTest` покрива: login -> register -> secure page.

Важно техническо уточнение:
- Mockito inline mock-ове не работеха коректно на Java 25 в тази среда.
- Добавен е Mockito `MockMaker` override към `SubclassByteBuddyMockMaker` за стабилни тестове.

---

## Files Changed (added/updated/deleted)
### Added
- `pom.xml`
- `src/main/java/com/example/LegacyAppApplication.java`
- `src/main/java/com/example/security/AuthUserService.java`
- `src/main/java/com/example/security/SecurityConfig.java`
- `src/main/java/com/example/web/AuthController.java`
- `src/main/java/com/example/web/IndexController.java`
- `src/main/java/com/example/registration/RegistrationValidator.java`
- `app-web/src/com/example/RegisterAction.java` (Spring controller version)
- `app-web/src/com/example/SecureAction.java` (Spring controller version)
- `src/main/resources/templates/index.html`
- `src/main/resources/templates/register.html`
- `src/main/resources/templates/register_confirmation.html`
- `src/main/resources/templates/secure/index.html`
- `src/main/resources/templates/jaas/login.html`
- `src/main/resources/templates/jaas/login_error.html`
- Unit tests:
  - `src/test/java/com/example/registration/RegistrationValidatorTest.java`
  - `src/test/java/com/example/RegistrationBeanTest.java`
  - `src/test/java/com/example/security/AuthUserServiceTest.java`
- Integration test:
  - `src/test/java/com/example/LegacyAppIntegrationTest.java`
- Mockito config override:
  - `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`

### Updated
- `app-ejb/src/com/example/RegistrationBean.java` (EJB -> Spring `@Service`)
- `src/main/resources/templates/secure/index.html` (template expression fix)

### Deleted
- Legacy JARs:
  - `app-web/WebRoot/WEB-INF/lib/*.jar` (Struts 1.1 + commons-* line)
- Legacy descriptors:
  - `app-web/WebRoot/WEB-INF/struts-config.xml`
  - `app-web/WebRoot/WEB-INF/web.xml`
  - `app-web/WebRoot/WEB-INF/jboss-web.xml`
  - `app-ejb/src/META-INF/ejb-jar.xml`
  - `app-ejb/src/META-INF/jboss.xml`
  - `app-ear/META-INF/application.xml`
- Legacy EJB/JAAS interfaces:
  - `app-ejb/src/com/example/RegistrationEJB.java`
  - `app-ejb/src/com/example/RegistrationHome.java`
  - `app-ejb/src/com/example/jaas/MyLoginModule.java`
  - `app-ejb/src/com/example/jaas/MyPrincipal.java`
- Legacy JSP views:
  - `app-web/WebRoot/**/*.jsp`
- Legacy manifests:
  - `app-ejb/src/META-INF/MANIFEST.MF`
  - `app-web/WebRoot/META-INF/MANIFEST.MF`

---

## Remaining Manual Tasks / Follow-ups
1) Почистване на празните legacy directory структури (остават WebRoot/app-ear/app-ejb папки без функционално съдържание).
2) Замяна на in-memory admin user с реален user store (DB/LDAP/OAuth) според бизнес изисквания.
3) Повече end-to-end тестове за logout, error cases и invalid form submissions.
4) Ако се изисква формално CVE сканиране, добавяне на OWASP dependency-check/osv scanning в CI pipeline.

---

## Lessons Learned
1) При modernization на legacy security първо стабилизирай login/CSRF/roles — иначе view layer и “happy path” тестовете подвеждат.
2) “Replace dependencies” не е само build-процес: трябва и runtime classpath да бъде чист от legacy JAR-ове.
3) При Java 25/нови JVM версии инструментите (Mockito/ByteBuddy) могат да се счупят; планирайте mock-maker override/версии.
4) За малки приложения е по-бързо да се “пренапише” legacy core директно в Spring annotations, отколкото да се поддържат два build/runtime модела едновременно.

