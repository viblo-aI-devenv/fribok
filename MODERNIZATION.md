# Fribok Modernization Plan

This document outlines a phased plan to modernize the Fribok codebase from its
current Java 5/6-era coding style to modern Java idioms and practices, while
keeping the application functional throughout.

## Current State

| Metric | Value |
|--------|-------|
| Codebase age | ~17 years (first commit 2009) |
| Lines of Java | ~153,000 |
| Java files | 641 production, 4 test |
| GUI files (Swing) | 310 Java + 111 IntelliJ `.form` files |
| Test coverage | ~0.6% (13 test methods) |
| Java target | 21 (code written in Java 5/6 style) |
| Logging | None (`System.out.println`) |
| Persistence | Java serialization into HSQLDB `OBJECT` columns |
| Date/time | `java.util.Date` / `Calendar` / `SimpleDateFormat` |

### Key Issues by Count

| Category | Occurrences |
|----------|:-----------:|
| `return null` (no `Optional`) | 380 |
| Old date/time API | 191 |
| Java serialization references | 151 |
| `System.out/err.println` | 109 |
| Mutable public fields | 54 |
| Broad `catch (Exception)` | 33 |
| Old-style try/finally (no try-with-resources) | 16+ |
| `synchronized` blocks | 11 |
| Raw types / `Vector` | 4 |
| Deprecated boxing constructors | 2 |

---

## Phase 0: Test Foundation

**Goal:** Establish the safety net that makes all subsequent changes safe.

**Risk:** None | **Effort:** Medium

1. **Upgrade to JUnit 5** -- Replace `junit:junit:4.13.2` with JUnit Jupiter 5.x
   in `pom.xml`. Migrate the 4 existing tests. Add `maven-surefire-plugin`
   configuration.

2. **Add test infrastructure** -- Add Mockito and AssertJ as test dependencies.
   Fix existing test issues (resource leak in `BgMaxFileTest`, reversed assertion
   args in `SSAutoIncrementTest`, mixed JUnit 3/4 imports).

3. **Write tests for core business logic** -- Before changing any production
   code, cover:
   - Calculator classes (`SSBalanceCalculator`, `SSResultCalculator`,
     `SSSalesTaxCalculator`, `SSOCRNumber`)
   - Data model classes (`SSInvoice`, `SSVoucher`, `SSAccount`, `SSCustomer`,
     `SSSupplier`)
   - Import/export (`SIEReader`, `SIEWriter`, `BgMaxFile`, Excel importers)
   - Utility classes (`SSUtil`, `SSAutoIncrement`, `SSConfig`)

4. **Add `.editorconfig`** -- Enforce 4-space indentation, UTF-8, LF line
   endings, 120-character line width.

**Human verification:** Run the app once to confirm it still works.

---

## Phase 1: Low-Risk Syntax Modernization

**Goal:** Bring code to modern Java idioms without changing behavior.

**Risk:** Low | **Effort:** Medium-High

5. **Replace deprecated boxing constructors** -- `new Integer(x)` to
   `Integer.valueOf(x)` (2 occurrences).

6. **Fix raw types** -- Add generics to the 4 raw-type usages. Replace `Vector`
   with `ArrayList`.

7. **Add try-with-resources** -- Convert all 16+ old-style try/finally blocks.
   Fix resources that leak on exceptions.

8. **Use diamond operator** -- Replace explicit type arguments with `<>` across
   the codebase.

9. **Replace anonymous inner classes with lambdas** -- `Runnable`,
   `ActionListener`, `Comparator`, etc. Large but mechanical transformation
   across GUI code.

10. **Introduce streams** -- Replace simple filter/map/collect `for` loops with
    `Stream` pipelines where it improves readability.

11. **Fix broad exception catching** -- Replace `catch (Exception e)` with
    specific exception types (33 occurrences). Remove silently swallowed
    exceptions.

**Human verification:** None. Tests from Phase 0 verify correctness.

---

## Phase 2: Logging

**Goal:** Replace ad-hoc console output with structured logging.

**Risk:** Low | **Effort:** Low

12. **Add SLF4J + Logback** -- Add `slf4j-api` and `logback-classic` to
    `pom.xml`. Create `logback.xml` with sensible defaults (console + file
    appender, log rotation).

13. **Replace `System.out.println` / `System.err.println`** -- Convert all 109
    occurrences to SLF4J log calls at appropriate levels.

14. **Replace `e.printStackTrace()`** -- Convert to `log.error("message", e)`
    with contextual messages.

**Human verification:** None.

---

## Phase 3: Date/Time API Migration

**Goal:** Migrate from `java.util.Date`/`Calendar`/`SimpleDateFormat` to
`java.time.*`.

**Risk:** Medium | **Effort:** Medium

15. **Create adapter utilities** -- Bridge methods between `java.util.Date` and
    `java.time.LocalDate`/`LocalDateTime` for gradual migration (Swing date
    pickers and JasperReports may still need `Date`).

16. **Migrate data model classes** -- Change date fields in domain objects from
    `Date` to `LocalDate`/`LocalDateTime`.

17. **Migrate import/export** -- Replace `SimpleDateFormat` with
    `DateTimeFormatter` (thread-safe) in SIE, BgMax, and Excel code.

18. **Migrate GUI date components** -- Update date chooser panels and table
    renderers.

**Human verification:** Verify date-related screens (invoice dates, accounting
periods) display correctly.

---

## Phase 4: Code Quality & Encapsulation

**Goal:** Improve code structure and safety.

**Risk:** Low | **Effort:** Medium

19. **Encapsulate public mutable fields** -- Add getters/setters for the 54
    public fields (concentrated in BgMax data classes). Consider converting
    simple data carriers to Java records.

20. **Introduce `Optional<T>`** -- Prioritize entity lookup methods, search/find
    methods, and parser methods. Focus on public API methods first rather than
    all 380 `return null` sites.

21. **Clean up orphaned code** -- Remove the duplicate legacy entry point
    (`SSBookkeeping.java`), orphaned test data files, and resolve the 7 TODOs.

**Human verification:** None.

---

## Phase 5: Persistence Architecture

**Goal:** Replace Java serialization with a proper persistence strategy.

**Risk:** High | **Effort:** Very High

This is the most complex and risky phase. Java serialization as the ORM strategy
means that every domain class change can break existing databases.

22. **Choose migration strategy:**
    - **Option A:** Normalized SQL schema with JDBC or jOOQ
    - **Option B:** JSON serialization (Jackson) into HSQLDB text columns (less
      disruptive)
    - **Option C:** Upgrade HSQLDB to 2.x with proper SQL and a lightweight ORM

23. **Upgrade HSQLDB** -- From 1.8.0.10 (2008-era) to 2.7.x for SQL standard
    compliance, better performance, and modern JDBC support.

24. **Write a database migration tool** -- Must read the old serialized-object
    format and write data in the new format. Critical for users with existing
    data.

25. **Implement new persistence layer** -- Replace `OBJECT` columns with
    normalized SQL or JSON. Remove `Serializable` from domain classes
    incrementally.

26. **Keep backup/restore working** -- Ensure the backup system works with the
    new format.

**Human verification:** Test with a real database backup to verify data
migration.

---

## Phase 6: Dependency Updates

**Goal:** Bring all dependencies to current versions and replace obsolete ones.

**Risk:** Medium | **Effort:** Medium

27. **Replace `javax.mail`** with `jakarta.mail` (Jakarta EE migration).

28. **Replace `jxl`** (abandoned 2009) with Apache POI for Excel support.

29. **Evaluate `itext` 4.2.2** -- Consider upgrading to OpenPDF (LGPL fork) or
    iText 7+ (AGPL).

30. **Evaluate `forms_rt` and `ideauidesigner-maven-plugin`** -- The IntelliJ
    GUI Designer dependency ties the build to a specific IDE. Consider migrating
    111 `.form` files to plain Java (substantial work).

31. **Evaluate `javax.help:javahelp`** -- Abandoned. Consider replacing with a
    simple HTML-based help browser or removing if unused.

32. **Evaluate Spring usage** -- Only `spring-core` and `spring-beans` are used.
    Determine if Spring is needed at all, or if it can be replaced with a
    lighter solution.

**Human verification:** None.

---

## Phase 7: Build & Tooling

**Goal:** Modern build practices and quality enforcement.

**Risk:** None | **Effort:** Low

33. **Add Checkstyle configuration** -- Create a project-specific
    `checkstyle.xml` enforcing the style from `AGENTS.md`.

34. **Replace FindBugs with SpotBugs** -- FindBugs is abandoned; SpotBugs is
    the active successor.

35. **Add code coverage** -- Add JaCoCo plugin to `pom.xml` with coverage
    reporting.

36. **Add CI quality gates** -- Fail CI on checkstyle violations, SpotBugs
    issues, or coverage drops.

**Human verification:** None.

---

## Phase Summary

| Phase | Description | Risk | Effort | AI-Automatable |
|:-----:|-------------|------|--------|:--------------:|
| 0 | Test Foundation | None | Medium | Yes |
| 1 | Syntax Modernization | Low | Medium-High | Yes |
| 2 | Logging | Low | Low | Yes |
| 3 | Date/Time Migration | Medium | Medium | Mostly |
| 4 | Code Quality | Low | Medium | Yes |
| 5 | Persistence Architecture | High | Very High | Partially |
| 6 | Dependency Updates | Medium | Medium | Yes |
| 7 | Build & Tooling | None | Low | Yes |

**Recommended execution order:** Phases 0-2 first (safest, immediate value),
then 3-4, then 6-7, and finally Phase 5 once everything else is solid.

---

## Out of Scope (Future Considerations)

These are larger architectural changes beyond "modernization":

- **GUI framework migration** -- Swing to JavaFX or a web frontend (Vaadin,
  Spring Boot + HTMX). The 310 GUI files + 111 `.form` files make this a major
  undertaking.
- **Dependency injection** -- Replace singleton/static-access patterns with
  proper DI.
- **Maven to Gradle** -- Optional, better incremental builds.
- **Java module system (JPMS)** -- Enforce architectural boundaries.
