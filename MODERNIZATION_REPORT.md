# Modernization Report

## 1. Executive Summary

This repository started as an Eclipse-managed Java EE sample built around JBoss 4.0.5, Struts 1.1, JSP, JAAS, and EJB 2.1. It did not contain Spring, Maven, Gradle, or any real persistence implementation.

I modernized the repo by adding a new root-level Spring Boot 3.5.9 application compiled for Java 21, migrating the UI from JSP to Thymeleaf, replacing container-managed JAAS and remote EJB calls with Spring Security and service classes, introducing Spring Data JPA with H2, and adding unit and integration tests.

Decision: I implemented the modernization as a replacement runtime in the same repository rather than trying to adapt Struts/EJB 2.1 directly.
Justification: the legacy codebase is extremely small, but its runtime model is deeply obsolete and not meaningfully compatible with Java 21/Spring Boot 3 without a near-total rewrite anyway.

## 2. Codebase Assessment

### Legacy baseline

- Java version in use: Java 1.6, from Eclipse compiler settings in `app-web/.settings/org.eclipse.jdt.core.prefs` and `app-ejb/.settings/org.eclipse.jdt.core.prefs`
- Spring version in use: none
- Persistence layer: EJB 2.1 remote session bean only; no JPA, JDBC, ORM, or database-backed repository
- Frontend technology: JSP
- Build tool: none; the repo was an Eclipse WTP / JBoss deployment structure, not a Maven or Gradle build
- Application packaging: EAR + WAR + EJB JAR

### Legacy project structure

- `app-ear/`: EAR assembly metadata
- `app-ejb/`: EJB 2.1 remote bean and JAAS login module
- `app-web/`: Struts 1.1 web tier, JSP views, checked-in servlet libraries, deployment descriptors

### Deprecated and obsolete APIs found

- `javax.ejb.EJBHome` in `app-ejb/src/com/example/RegistrationHome.java`
- `javax.ejb.EJBObject` in `app-ejb/src/com/example/RegistrationEJB.java`
- `javax.ejb.SessionBean` lifecycle model in `app-ejb/src/com/example/RegistrationBean.java`
- `javax.rmi.PortableRemoteObject` in `app-web/src/com/example/RegisterAction.java`
- Struts 1 `Action`, `ActionForm`, `ActionMapping`, `ActionForward` in `app-web/src/com/example/RegisterAction.java` and `app-web/src/com/example/SecureAction.java`
- JSP scriptlets in `app-web/WebRoot/jaas/logoff.jsp` and `app-web/WebRoot/secure/index.jsp`
- `java.security.acl.Group` usage in `app-ejb/src/com/example/jaas/MyLoginModule.java`
- Container-specific JBoss JAAS classes `org.jboss.security.SimpleGroup` and `org.jboss.security.SimplePrincipal` in `app-ejb/src/com/example/jaas/MyLoginModule.java`

### Outdated dependencies and legacy libraries

Versions were identified from checked-in JAR manifests under `app-web/WebRoot/WEB-INF/lib/`.

- Struts Framework 1.1
- Commons BeanUtils 1.6
- Commons Collections 2.1
- Commons Digester 1.5
- Commons FileUpload 1.0
- Commons Lang 1.0.1
- Commons Logging 1.0.3
- Commons Validator 1.0.2
- Struts Legacy Distribution 1.0
- Jakarta ORO legacy jar without modern support status

### Known CVE-vulnerable legacy libraries

The legacy runtime bundles multiple known-vulnerable components.

- Commons BeanUtils 1.6 is affected by [CVE-2014-0114](https://nvd.nist.gov/vuln/detail/CVE-2014-0114)
- Commons FileUpload 1.0 is affected by [CVE-2023-24998](https://nvd.nist.gov/vuln/detail/CVE-2023-24998)
- Commons FileUpload 1.0 is also within the affected range for [CVE-2025-48976](https://nvd.nist.gov/vuln/detail/CVE-2025-48976)
- Struts 1.1 is affected by [CVE-2015-0899](https://nvd.nist.gov/vuln/detail/CVE-2015-0899)

Additional risk context:

- Apache Struts 1 is end-of-life and unsupported by the Apache Struts project, so even unenumerated issues are operationally high risk. See the project security page: [Apache Struts Security](https://struts.apache.org/security.html).

## 3. Modernization Work Completed

### Build and platform upgrade

- Added Maven build at `pom.xml`
- Set Java release level to 21
- Migrated runtime to Spring Boot 3.5.9
- Adopted Spring Framework 6.2.15 through the Boot BOM

Decision: I used the Spring Boot parent BOM instead of pinning every Spring dependency manually.
Justification: it is the idiomatic way to keep the Spring stack internally compatible and patched.

### Java 21 adoption

The new code compiles against Java 21 and uses modern language/runtime features:

- Records:
  - `src/main/java/com/example/web/RegistrationRequest.java`
  - `src/main/java/com/example/web/RegistrationView.java`
- Text blocks:
  - `src/main/java/com/example/service/WelcomeMessageService.java`
- Pattern matching in switch:
  - `src/main/java/com/example/controller/PageController.java`
- Virtual threads:
  - `src/main/java/com/example/config/ApplicationConfig.java`
  - `src/main/resources/application.properties`

Decision: I used records for request/response models rather than mutable form beans.
Justification: they fit small immutable MVC payloads better than legacy setter-heavy objects.

### Spring modernization

There was no existing Spring XML to convert because the legacy application did not use Spring at all.

Instead, I replaced the legacy XML/container model with Java-configured Spring Boot components:

- `src/main/java/com/example/ModernizedApplication.java`
- `src/main/java/com/example/config/ApplicationConfig.java`
- `src/main/java/com/example/config/SecurityConfig.java`
- `src/main/java/com/example/config/DataInitializer.java`
- `src/main/java/com/example/controller/PageController.java`

Legacy XML/descriptors now superseded by Java config:

- `app-web/WebRoot/WEB-INF/web.xml`
- `app-web/WebRoot/WEB-INF/struts-config.xml`
- `app-ejb/src/META-INF/ejb-jar.xml`
- `app-ejb/src/META-INF/jboss.xml`
- `app-ear/META-INF/application.xml`

Decision: I mapped the old deployment-descriptor behavior into Boot configuration rather than attempting XML compatibility layers.
Justification: it removes container coupling and is the most idiomatic modern Spring approach.

### Persistence modernization

Legacy state:

- No real persistence implementation; `RegistrationBean.register()` returned `"Hello"` and ignored submitted data

Modern state:

- Added JPA entity: `src/main/java/com/example/domain/UserAccount.java`
- Added repository: `src/main/java/com/example/repository/UserAccountRepository.java`
- Added in-memory H2 datasource via `src/main/resources/application.properties`

Decision: I used Spring Data JPA + H2.
Justification: it is a minimal, production-shaped persistence stack that keeps the sample easy to run locally while removing all EJB-specific complexity.

### Frontend migration

All JSP pages were identified and functionally migrated to Thymeleaf templates.

Legacy JSP inventory:

- `app-web/WebRoot/index.jsp`
- `app-web/WebRoot/register.jsp`
- `app-web/WebRoot/register_confirmation.jsp`
- `app-web/WebRoot/jaas/login.jsp`
- `app-web/WebRoot/jaas/login_error.jsp`
- `app-web/WebRoot/jaas/logoff.jsp`
- `app-web/WebRoot/secure/index.jsp`

Migration mapping:

- `app-web/WebRoot/index.jsp` -> `src/main/resources/templates/index.html`
- `app-web/WebRoot/register.jsp` -> `src/main/resources/templates/register.html`
- `app-web/WebRoot/register_confirmation.jsp` -> `src/main/resources/templates/register_confirmation.html`
- `app-web/WebRoot/jaas/login.jsp` and `app-web/WebRoot/jaas/login_error.jsp` -> `src/main/resources/templates/jaas/login.html`
- `app-web/WebRoot/jaas/logoff.jsp` -> Spring Security logout flow configured in `src/main/java/com/example/config/SecurityConfig.java`
- `app-web/WebRoot/secure/index.jsp` -> `src/main/resources/templates/secure/index.html`

Decision: I fully migrated the JSP surface instead of producing a deferred plan.
Justification: the UI footprint was small enough to finish safely in one pass, which is better than leaving a mixed rendering stack behind.

## 4. SOLID / Code Quality Refactoring

The biggest design issue in the legacy code was that `RegisterAction` mixed HTTP handling, remote lookup, business logic invocation, and lifecycle cleanup in one Struts action.

Refactoring performed:

- Request handling moved to `PageController`
- registration business logic moved to `RegistrationService`
- password rules isolated in `PasswordPolicyService`
- message formatting isolated in `WelcomeMessageService`
- authentication lookup isolated in `CustomUserDetailsService`
- audit behavior isolated in `AuditService`

Why this improves SOLID:

- Single Responsibility: each class now has one reason to change
- Open/Closed: policy and message logic can evolve without rewriting controllers
- Dependency Inversion: high-level flows depend on injected abstractions instead of JNDI/EJB container lookups
- Testability: services can be unit-tested without servlet or JBoss runtime dependencies

## 5. Security Review and Remediation

### Insecure patterns removed

- Removed hard-coded JAAS credential check from active runtime path
- Removed JBoss-specific security implementation from active runtime path
- Removed JSP scriptlet-based logout behavior from active runtime path
- Removed remote EJB invocation from active runtime path
- Replaced plain value passthrough with validated request objects
- Added BCrypt password hashing
- Added duplicate-email checks and basic password policy enforcement

### SQL injection review

Legacy application:

- No SQL layer existed, so there was no direct SQL injection sink to remediate

Modern application:

- Persistence is handled through Spring Data JPA repository methods
- No string-built SQL or manual JDBC concatenation is present

Decision: I kept data access at repository level and avoided hand-written SQL.
Justification: it is the simplest way to avoid introducing new injection risk during the migration.

### CVE remediation status

Resolved in the active runtime path by removing the legacy stack from execution:

- Struts 1.1
- Commons BeanUtils 1.6
- Commons FileUpload 1.0
- Other checked-in Struts 1 era libraries

Current runtime dependency posture:

- The new executable path uses Spring Boot 3.5.9 and its managed dependency versions instead of the vulnerable checked-in legacy jars

Audit status:

- `mvn test` completed successfully
- `mvn org.owasp:dependency-check-maven:check` was started successfully, but the first-run NVD database population was still in progress during this session because it needed to ingest a very large feed

## 6. Tests Added

### Unit tests

- `src/test/java/com/example/service/PasswordPolicyServiceTest.java`
- `src/test/java/com/example/service/WelcomeMessageServiceTest.java`
- `src/test/java/com/example/service/RegistrationServiceTest.java`
- `src/test/java/com/example/service/CustomUserDetailsServiceTest.java`

### Integration test

- `src/test/java/com/example/integration/ModernizedApplicationIntegrationTest.java`

What the integration test covers:

- user registration through MVC
- authenticated login through Spring Security
- secure page rendering through the Boot application context

## 7. Files Modified and Added

### New or updated build / root files

- `.gitignore`
- `pom.xml`
- `MODERNIZATION_REPORT.md`

### New application source files

- `src/main/java/com/example/ModernizedApplication.java`
- `src/main/java/com/example/config/ApplicationConfig.java`
- `src/main/java/com/example/config/DataInitializer.java`
- `src/main/java/com/example/config/SecurityConfig.java`
- `src/main/java/com/example/controller/PageController.java`
- `src/main/java/com/example/domain/UserAccount.java`
- `src/main/java/com/example/repository/UserAccountRepository.java`
- `src/main/java/com/example/security/CustomUserDetailsService.java`
- `src/main/java/com/example/service/AuditService.java`
- `src/main/java/com/example/service/DuplicateEmailException.java`
- `src/main/java/com/example/service/PasswordPolicyService.java`
- `src/main/java/com/example/service/RegistrationService.java`
- `src/main/java/com/example/service/WelcomeMessageService.java`
- `src/main/java/com/example/web/RegistrationRequest.java`
- `src/main/java/com/example/web/RegistrationView.java`

### New resources and templates

- `src/main/resources/application.properties`
- `src/main/resources/templates/index.html`
- `src/main/resources/templates/register.html`
- `src/main/resources/templates/register_confirmation.html`
- `src/main/resources/templates/jaas/login.html`
- `src/main/resources/templates/secure/index.html`

### New test files

- `src/test/java/com/example/integration/ModernizedApplicationIntegrationTest.java`
- `src/test/java/com/example/service/CustomUserDetailsServiceTest.java`
- `src/test/java/com/example/service/PasswordPolicyServiceTest.java`
- `src/test/java/com/example/service/RegistrationServiceTest.java`
- `src/test/java/com/example/service/WelcomeMessageServiceTest.java`

## 8. Verification

### Command run

```bash
mvn test
```

### Result

- Build status: success
- Tests run: 8
- Failures: 0
- Errors: 0

## 9. Remaining Manual Tasks

- Decide whether to delete the legacy `app-ear`, `app-ejb`, and `app-web` directories after stakeholder sign-off; I left them in place as migration reference material
- Replace H2 with the production database of choice if this sample should become a deployable application
- Externalize the seeded admin credentials before any real deployment
- Let the first-run OWASP dependency-check database sync finish, then archive the generated report for compliance evidence
- If desired, add a Mockito agent configuration to silence the JDK 25 dynamic-agent warning during test execution

## 10. Source References

- [Spring Boot 3.5.9 available now](https://spring.io/blog/page-6)
- [Apache Struts Security](https://struts.apache.org/security.html)
- [CVE-2014-0114](https://nvd.nist.gov/vuln/detail/CVE-2014-0114)
- [CVE-2023-24998](https://nvd.nist.gov/vuln/detail/CVE-2023-24998)
- [CVE-2025-48976](https://nvd.nist.gov/vuln/detail/CVE-2025-48976)
- [CVE-2015-0899](https://nvd.nist.gov/vuln/detail/CVE-2015-0899)
