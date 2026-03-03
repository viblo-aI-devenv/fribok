# Fribok Modernization Plan

This document outlines a phased plan to modernize the Fribok codebase from its
current Java 5/6-era coding style to modern Java idioms and practices, while
keeping the application functional throughout.

## Current State

| Metric | Value |
|--------|-------|
| Codebase age | ~17 years (first commit 2009) |
| Lines of Java | ~149,400 |
| Java files | 639 production, 25 test |
| GUI files (Swing) | 310 Java + 111 IntelliJ `.form` files |
| Test coverage | 430 test methods across 25 test classes |
| Java target | 21 (code written in Java 5/6 style) |
| Logging | SLF4J + Logback (done; 4 stray `System.out.printf` remain in 3 files) |
| Persistence | Java serialization into HSQLDB `OBJECT` columns |
| Date/time | `java.util.Date` / `Calendar` (migration started; `SimpleDateFormat` fully eliminated) |

### Key Issues by Count

| Category | Occurrences | Notes |
|----------|:-----------:|-------|
| Old date/time API (`import java.util.Date`) | 140 files | 66 `new Date()`, 325 `Calendar.`, 0 `SimpleDateFormat` |
| Java serialization (`implements Serializable`) | 45 classes | |
| `return null` (no `Optional`) | ~419 | |
| Mutable public fields | ~49 | Concentrated in BgMax data classes |
| `synchronized` blocks | 11 | SSDB and threading code |
| Raw types / `Vector` | 6 | `Vector` still used in a few places |
| Stray `System.out.printf` | 4 | In 3 files: `Bookkeeping.java`, `SSBookkeeping.java`, `SSReportCache.java` |
| Dead server/locking code | 0 | Removed in Phase 3.5 |
| Broad `catch (Exception)` | 0 | Fixed in Phase 1 |
| Old-style try/finally (no try-with-resources) | 0 | Fixed in Phase 1 |
| Deprecated boxing constructors | 0 | Fixed in Phase 1 |

---

## Progress Overview

| Phase | Description | Status |
|:-----:|-------------|--------|
| 0 | Test Foundation | **Done** |
| 1 | Syntax Modernization | **Done** |
| 2 | Logging | **Done** (4 stray prints remain) |
| 3 | Date/Time Migration | In progress (Steps 15-17 done; Step 18 remains) |
| 3.5 | Remove Multi-User/Server Mode | **Done** |
| 4 | Code Quality & Encapsulation | Not started |
| 5 | Persistence Architecture | Not started |
| 6 | Dependency Updates | Not started |
| 7 | Build & Tooling | Not started |

---

## Phase 0: Test Foundation ✓ DONE

**Goal:** Establish the safety net that makes all subsequent changes safe.

**Risk:** None | **Effort:** Medium

1. **Upgrade to JUnit 5** -- Replace `junit:junit:4.13.2` with JUnit Jupiter 5.x
   in `pom.xml`. Migrate the 4 existing tests. Add `maven-surefire-plugin`
   configuration. ✓

2. **Add test infrastructure** -- Add Mockito and AssertJ as test dependencies.
   Fix existing test issues (resource leak in `BgMaxFileTest`, reversed assertion
   args in `SSAutoIncrementTest`, mixed JUnit 3/4 imports). ✓

3. **Write tests for core business logic** -- Before changing any production
   code, cover:
   - Calculator classes (`SSBalanceCalculator`, `SSResultCalculator`,
     `SSSalesTaxCalculator`, `SSOCRNumber`) ✓
   - Data model classes (`SSInvoice`, `SSVoucher`, `SSAccount`, `SSCustomer`,
     `SSSupplier`) ✓
   - Import/export (`SIEReader`, `SIEWriter`, `BgMaxFile`, Excel importers) ✓
   - Utility classes (`SSUtil`, `SSAutoIncrement`, `SSConfig`) ✓
   - **DB integration tests** (`SSCustomer`, `SSInvoice`, `SSSupplier`,
     `SSVoucher` CRUD operations against an in-memory HSQLDB instance). ✓
     These are tagged `@Tag("integration")` and run in a separate surefire
     execution with a forked JVM to prevent SSDB singleton state from leaking.

   > **Note:** The HSQLDB account-plan seed step loads three XLS files
   > whose names originally contained the Swedish character `ä`.  These
   > were renamed to ASCII (`Enskild-naringsidkare`) in PR #12 to avoid
   > locale-dependent build failures.  The `LANG=C.UTF-8` workaround is
   > no longer required.

4. **Add `.editorconfig`** -- Enforce 4-space indentation, UTF-8, LF line
   endings, 120-character line width. ✓

---

## Phase 1: Low-Risk Syntax Modernization ✓ DONE

**Goal:** Bring code to modern Java idioms without changing behavior.

**Risk:** Low | **Effort:** Medium-High

5. **Replace deprecated boxing constructors** -- `new Integer(x)` to
   `Integer.valueOf(x)` (2 occurrences). ✓

6. **Fix raw types** -- Add generics to raw-type usages. Replace `Vector`
   with `ArrayList`. ✓ (Note: 6 `Vector` references remain — verify if intentional.)

7. **Add try-with-resources** -- Convert all old-style try/finally blocks.
   Fix resources that leak on exceptions. ✓

8. **Use diamond operator** -- Replace explicit type arguments with `<>` across
   the codebase. ✓

9. **Replace anonymous inner classes with lambdas** -- `Runnable`,
   `ActionListener`, `Comparator`, etc. Large but mechanical transformation
   across GUI code. ✓

10. **Introduce streams** -- Replace simple filter/map/collect `for` loops with
    `Stream` pipelines where it improves readability. ✓

11. **Fix broad exception catching** -- Replace `catch (Exception e)` with
    specific exception types (33 occurrences). Remove silently swallowed
    exceptions. ✓

---

## Phase 2: Logging ✓ DONE

**Goal:** Replace ad-hoc console output with structured logging.

**Risk:** Low | **Effort:** Low

12. **Add SLF4J + Logback** -- Add `slf4j-api` and `logback-classic` to
    `pom.xml`. Create `logback.xml` with sensible defaults (console + file
    appender, log rotation). ✓

13. **Replace `System.out.println` / `System.err.println`** -- Convert all 109
    occurrences to SLF4J log calls at appropriate levels. ✓ (4 stray
    `System.out.printf` remain in `Bookkeeping.java:155`,
    `SSBookkeeping.java:114`, and `SSReportCache.java:104,117`.)

14. **Replace `e.printStackTrace()`** -- Convert to `log.error("message", e)`
    with contextual messages. ✓

---

## Phase 3: Date/Time API Migration

**Goal:** Migrate from `java.util.Date`/`Calendar`/`SimpleDateFormat` to
`java.time.*`.

**Risk:** Medium | **Effort:** Medium

15. **Create adapter utilities** -- Bridge methods between `java.util.Date` and
    `java.time.LocalDate`/`LocalDateTime` for gradual migration (Swing date
    pickers and JasperReports may still need `Date`). ✓ (`SSDateUtil.java`
    created with full adapter API.)

16. **Migrate data model classes** -- Change date fields in domain objects from
    `Date` to `LocalDate`/`LocalDateTime`. Use `SSDateUtil` for boundary
    conversion. (143 files still import `java.util.Date`; 89 `new Date()`
    calls remain.) ✓ (16 domain model classes migrated: internal fields changed
    to `LocalDate`, deprecated `Date`-typed bridge getters/setters added for
    backward compatibility with ~50 GUI/print callers, new `LocalDate`-typed
    API added, `new Date()` replaced with `SSDateUtil.today()`, tests updated.)

17. **Migrate import/export** -- Replace `SimpleDateFormat` with
    `DateTimeFormatter` (thread-safe) in SIE, BgMax, and Excel code. (33
    `SimpleDateFormat` usages remain; note `SIEWriter` has a static
    `SimpleDateFormat` field — a potential concurrency bug.) ✓ (All 13 files
    migrated: SIEWriter, SIEIterator, SSBgMaxImporter, LBinLine,
    SSWritableExcelRow, SSExcelCell, SSOrderImporter, SSOrderExporter,
    SSDBConfig, SSReportCache, SSInvoicePrinter, SSCreditinvoicePrinter,
    SSMainMenu. Zero `SimpleDateFormat` imports remain in codebase.
    All 430 tests pass.)

18. **Migrate GUI date components** -- Update date chooser panels and table
    renderers. (325 `Calendar.` calls remain, mostly in GUI.)

**Human verification:** Verify date-related screens (invoice dates, accounting
periods) display correctly.

---

## Phase 3.5: Remove Multi-User/Server Mode ✓ DONE

**Goal:** Remove the dead client-server/multi-user networking and locking code.

**Risk:** Low | **Effort:** Medium

The application originally supported a multi-user mode where multiple clients
connected to a shared HSQLDB server via JDBC, with two custom TCP socket
channels: port 2222 for a text-based pessimistic locking protocol, and port
2223 for broadcasting database trigger notifications to all clients.  The
server component (`se.swedsoft.SSServer`) was **not in this repository** and
is presumed lost.  The multi-user mode could not function and was dead code
that complicated every modification to SSDB, triggers, and GUI code.

### What was removed

**Deleted entirely:**
- `SSPostLock.java` -- Record-level locking over port 2222 socket (was imported
  by ~54 GUI Frame/Dialog files) ✓
- `SSCompanyLock.java` -- Company-level locking over port 2222 socket ✓
- `SSYearLock.java` -- Accounting-year-level locking over port 2222 socket ✓

**Simplified `SSTriggerHandler.java`:** ✓
- Removed `extends Thread`, the `run()` socket-listener method, and the
  constructor socket code (port 2223).  Kept only the `Trigger.fire()` method
  for local/embedded HSQLDB triggers.

**Removed from `SSDB.java`:** ✓
- Fields: `iSocket`, `iIn`, `iOut`, `iLocking`
- Methods: `getLocking()`, `setLocking()`, `openSocket()`,
  `startupRemote()`, `loadSelectedDatabase()`, `removeClient()`,
  `getReader()`, `getWriter()`, `getSocket()`, `LockDatabase()`,
  `UnlockDatabase()`, `createServerTriggers()`
- Simplified methods that branched on `iLocking`: `shutdown()`,
  `shutdownCompact()`, `loadLocalDatabase()`, `addVoucher()`,
  `readOldDatabase()`, `createTriggers()`, `toString()`

**Removed from `SSDBConfig.java`:** ✓
- Fields and methods: `iServerAddress`, `iClientKey`,
  `getServerAddress()`, `setServerAddress()`, `getClientkey()`,
  `setClientKey()`
- Removed server/clientkey parsing from `load()`

**Removed from GUI/import files (~54 files):** ✓
- All `SSPostLock.applyLock()` / `SSPostLock.removeLock()` /
  `SSPostLock.isLocked()` calls and surrounding lock-check `if` blocks
- `SSCompanyLock` / `SSYearLock` usage in `SSMainMenu.java`,
  `SSBookkeeping.java`, `Bookkeeping.java`,
  `SSNewAccountingYearDialog.java`, `SSAccountingYearFrame.java`
- `getLocking()` branches in `SSDBUtils.java`, `SSBackupUtils.java`,
  `SSMainMenu.java`, `SSBookkeeping.java`, `Bookkeeping.java`

**Result:** 69 files changed, ~2,500 lines deleted.  SSDB.java reduced from
~8,200 lines to ~7,850 lines.  All 430 tests pass.

---

## Phase 4: Code Quality & Encapsulation

**Goal:** Improve code structure and safety.

**Risk:** Low | **Effort:** Medium

19. **Encapsulate public mutable fields** -- Add getters/setters for the ~51
    public fields (concentrated in BgMax data classes: `BgMaxBetalning`,
    `BgMaxAvsnitt`, `BgMaxFile`, `BgMaxReferens`). Consider converting simple
    data carriers to Java records.

20. **Introduce `Optional<T>`** -- Prioritize entity lookup methods, search/find
    methods, and parser methods. Focus on public API methods first rather than
    all ~418 `return null` sites.

21. **Clean up orphaned code** -- Remove the duplicate legacy entry point
    (`SSBookkeeping.java`), orphaned test data files, and resolve the 7 TODOs.
    Also fix the 4 remaining `System.out.printf` calls left from Phase 2.

22. **Resolve remaining `Vector` usages** -- Verify the 6 remaining `Vector`
    references from Phase 1 are intentional or replace with `ArrayList`.

**Human verification:** None.

---

## Phase 5: Persistence Architecture

**Goal:** Replace Java serialization with a proper persistence strategy.

**Risk:** High | **Effort:** Very High

This is the most complex and risky phase. Java serialization as the ORM strategy
means that every domain class change can break existing databases. 45 domain
classes currently implement `Serializable`.

23. **Choose migration strategy:**
    - **Option A:** Normalized SQL schema with JDBC or jOOQ
    - **Option B:** JSON serialization (Jackson) into HSQLDB text columns (less
      disruptive)
    - **Option C:** Upgrade HSQLDB to 2.x with proper SQL and a lightweight ORM

24. **Upgrade HSQLDB** -- From 1.8.0.10 (2008-era) to 2.7.x for SQL standard
    compliance, better performance, and modern JDBC support.

25. **Write a database migration tool** -- Must read the old serialized-object
    format and write data in the new format. Critical for users with existing
    data.

26. **Implement new persistence layer** -- Replace `OBJECT` columns with
    normalized SQL or JSON. Remove `Serializable` from domain classes
    incrementally.

27. **Keep backup/restore working** -- Ensure the backup system works with the
    new format.

**Human verification:** Test with a real database backup to verify data
migration.

---

## Phase 6: Dependency Updates

**Goal:** Bring all dependencies to current versions and replace obsolete ones.

**Risk:** Medium | **Effort:** Medium

28. **Replace `javax.mail`** with `jakarta.mail` (Jakarta EE migration).

29. **Replace `jxl`** (abandoned 2009) with Apache POI for Excel support.

30. **Evaluate `itext` 4.2.2** -- Consider upgrading to OpenPDF (LGPL fork) or
    iText 7+ (AGPL).

31. **Evaluate `forms_rt` and `ideauidesigner-maven-plugin`** -- The IntelliJ
    GUI Designer dependency ties the build to a specific IDE. Consider migrating
    111 `.form` files to plain Java (substantial work).

32. **Evaluate `javax.help:javahelp`** -- Abandoned. Consider replacing with a
    simple HTML-based help browser or removing if unused.

33. **Evaluate Spring usage** -- Only `spring-core` and `spring-beans` are used.
    Determine if Spring is needed at all, or if it can be replaced with a
    lighter solution.

**Human verification:** None.

---

## Phase 7: Build & Tooling

**Goal:** Modern build practices and quality enforcement.

**Risk:** None | **Effort:** Low

34. **Add Checkstyle configuration** -- Create a project-specific
    `checkstyle.xml` enforcing the style from `AGENTS.md`. (Plugin is declared
    in `pom.xml` but no `checkstyle.xml` exists yet.)

35. **Replace FindBugs with SpotBugs** -- FindBugs is abandoned; SpotBugs is
    the active successor.

36. **Add code coverage** -- Add JaCoCo plugin to `pom.xml` with coverage
    reporting.

37. **Add CI quality gates** -- Fail CI on checkstyle violations, SpotBugs
    issues, or coverage drops.

**Human verification:** None.

---

## Phase Summary

| Phase | Description | Risk | Effort | Status |
|:-----:|-------------|------|--------|--------|
| 0 | Test Foundation | None | Medium | **Done** |
| 1 | Syntax Modernization | Low | Medium-High | **Done** |
| 2 | Logging | Low | Low | **Done** |
| 3 | Date/Time Migration | Medium | Medium | In Progress (Steps 15-17 done) |
| 3.5 | Remove Multi-User/Server Mode | Low | Medium | **Done** |
| 4 | Code Quality | Low | Medium | Not started |
| 5 | Persistence Architecture | High | Very High | Not started |
| 6 | Dependency Updates | Medium | Medium | Not started |
| 7 | Build & Tooling | None | Low | Not started |

**Recommended next steps:**
1. Finish Phase 3 (Step 18: migrate GUI date components). 140 files still
   import `java.util.Date`, with 325 `Calendar.` references mostly in GUI code.
2. Phase 4: clean up orphaned `SSBookkeeping.java`, encapsulate BgMax public
   fields, add `Optional<T>` to lookup methods.
3. Phase 7 (tooling) can be done in parallel — low risk, no coordination
   required.
4. Phase 6 (dependencies) before Phase 5, as HSQLDB upgrade is part of both.
5. Phase 5 last — it is the most disruptive change.

---

## Out of Scope (Future Considerations)

These are larger architectural changes beyond "modernization":

- **SSDB God Object refactor** -- `SSDB.java` is ~7,850 lines and acts as DB
  engine, GUI orchestrator, and data container. Phase 3.5 removed the lock
  manager responsibility. Breaking up the remainder requires extensive test
  coverage first.
- **GUI framework migration** -- Swing to JavaFX or a web frontend (Vaadin,
  Spring Boot + HTMX). The 310 GUI files + 111 `.form` files make this a major
  undertaking.
- **Dependency injection** -- Replace singleton/static-access patterns with
  proper DI.
- **Maven to Gradle** -- Optional, better incremental builds.
- **Java module system (JPMS)** -- Enforce architectural boundaries.
