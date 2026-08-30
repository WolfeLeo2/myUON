# myUON — Domain Logic & Mock Data Review

Findings from a full read-through of the codebase, checked against how the
University of Nairobi (UoN) portal actually behaves. Ordered by how wrong
they are, not by file. Status column tracks fix progress.

| # | Issue | Status |
|---|-------|--------|
| 1 | `DegreeClass` stored per unit instead of computed cumulatively | Fixed |
| 2 | `currentGpaOrAverage` is a static mock number, never derived | Fixed |
| 3 | `isSenateThresholdMet` is a stored flag, not derived from attendance | Fixed |
| 4 | Attendance model is daily-hours based; real classes meet 1–2x/week | Fixed |
| 5 | Academic-year fee `invoiceBreakdown` double-counts line items | Fixed |
| 6 | Semester dates and exam dates are ~8 months apart | Fixed |
| 7 | `GradeRecordEntity` primary key (`unitCode` alone) will collide | Fixed |
| 8 | Mock data has no failure/edge cases | Fixed |
| 9 | Repo mutations have no error handling (all "submits" always succeed) | Fixed |
| 10 | Login accepts any non-blank password, no real failure state modeled | Fixed |
| 11 | Dates are hardcoded formatted strings, not real `Date`/`Instant` | Fixed |
| 12 | `UnitRegistration`/`FeeStatementDetail`/`HostelBookingScreen` `NavKey`s have no registered `entry` — crashes on navigate | Fixed |
| 13 | Tabs share one back stack; switching tabs discards in-tab navigation history | Fixed |
| 14 | Login isn't gated through the nav layer — back stack always starts at `Dashboard` regardless of real auth state | Fixed |
| 15 | Nav 3 back stack doesn't survive process death (no `rememberNavBackStack`/serialization) | Fixed |

---

## 1. `DegreeClass` attached to individual `GradeRecord`s

**Where:** `data/model/GradeRecord.kt`, `data/sample/MockDataProvider.kt`,
`data/db/Entities.kt`, `data/db/Converters.kt`

**Problem:** Degree classification (First Class, Second Upper, etc.) is a
*cumulative* result computed from the credit-weighted mean mark across an
entire degree program at graduation — not something a single course unit
"has." The model puts a `degreeClass` field on every `GradeRecord`, which
reads as "you got First Class Honours in CSC 301," which isn't how UoN (or
any university) classifies degrees.

**Fix:** Remove `degreeClass` from `GradeRecord`/`GradeRecordEntity`. Add a
credit-weighted cumulative calculation in `AcademicRepository`, derived from
all `GradeRecord`s, exposed as a `StateFlow`. Per-unit `gradeLetter` (A/B/C/D/E)
stays — that one's legitimate.

**Fixed:** `degreeClass` removed from `GradeRecord`/`GradeRecordEntity`.
`cumulativeAverage()` + `classifyDegree()` + `AcademicSummary` added to
`data/model/GradeRecord.kt`, exposed as `AcademicRepository.academicSummary`.

## 2. `currentGpaOrAverage` never recalculated

**Where:** `data/model/StudentProfile.kt:21`, `ui/dashboard/DashboardScreen.kt:443`

**Problem:** Hardcoded to `74.8` in `StudentProfile`, and nothing derives it
from `gradeRecords`. Approve a supplementary pass or fix a missing-marks
dispute and this number won't move.

**Fix:** Remove the static field from `StudentProfile`. Compute a
credit-weighted average from `AcademicRepository.gradeRecords` (same
calculation that feeds fix #1's classification) and have `DashboardViewModel`
source it from there instead of the profile.

**Fixed:** `currentGpaOrAverage` removed from `StudentProfile`.
`DashboardUiState.academicSummary` now carries the computed average +
degree class; `DashboardScreen` reads from there.

## 3. `isSenateThresholdMet` is a stored flag, not derived

**Where:** `data/model/AttendanceModels.kt:15`

**Problem:** `overallPercentage` right next to it is correctly computed from
the attendance counts. `isSenateThresholdMet` is a plain `Boolean` field that
has to be set correctly by hand every time attendance data is populated —
easy to silently drift out of sync with the real percentage, and that
percentage gates real exam eligibility.

**Fix:** Make it a computed property (`overallPercentage >= 75.0`), same
pattern as `overallPercentage`. Folded into the attendance model rework
below since both live in the same file.

**Fixed:** now a `get()` on `AttendanceSummary`, can no longer drift from
`overallPercentage`.

## 4. Attendance model doesn't match how UoN actually schedules classes

**Where:** `data/model/AttendanceModels.kt`, `ui/academics/AttendanceAnalyticsScreen.kt`,
`data/sample/MockDataProvider.kt`

**Problem:** The model tracked "hours attended per day" against an 8-hour/day
target, Monday through Sunday, styled like a fitness-app activity ring. Real
UoN units meet once or twice a week for a fixed 2-hour block (see
`TimetableItem` — e.g. "Mon 09:00–11:00"), not continuously every day. The
75% Senate rule is a percentage of *scheduled sessions attended*, not hours
logged per day, so the "8h/day" framing had no real relationship to the rule
it claimed to visualize.

**Fix (per clarification):** Rework the model around actual scheduled
lessons for the unit: a per-week record listing that week's session(s) for
the unit (1 or 2, matching its real timetable) and whether each was
attended, plus a 75%-threshold view. Screen shows one week at a time (with
prev/next navigation) with a cumulative (semester-to-date) toggle, replacing
the removed Day/Month/Year granularities that don't apply to a 1–2x/week
schedule.

**Fixed:** `AttendanceSummary.weeklyBreakdown: List<AttendanceWeekRecord>`
replaces the old daily-hours list; each week holds the unit's actual 1–2
scheduled sessions. `AttendanceAnalyticsScreen` now has a week selector
(prev/next), a per-week session-pill chart against the 75% line, and a
"Selected Week" / "Cumulative" toggle. The old Day/Week/Month/Year bottom
bar is gone.

## 5. Fee breakdown double-counts line items

**Where:** `data/sample/MockDataProvider.kt` (`sampleAcademicYearFeeStatement`)

**Problem:** `invoiceBreakdown` summed to 190,000 (78,500 × 2 semesters +
annual exam/library/medical/computer fees added again on top) but
`totalInvoiced` was 157,000. The semester breakdown already includes those
same fee categories inside each semester's 78,500; the annual view was
re-adding them as separate rows.

**Fixed:** Annual breakdown replaced with each category's own annual total
(2× its per-semester figure — e.g. "Tuition Fees (Annual)" = 124,000 =
62,000 × 2), which sums to exactly 157,000. No more phantom "Annual X" rows
stacked on top of the semester totals.

## 6. Semester dates vs. exam dates didn't line up

**Where:** `data/sample/MockDataProvider.kt` (`sampleFeeStatement`,
`sampleAcademicYearFeeStatement`, `sampleActiveBooking`)

**Problem:** Semester 2 (AY 2025/2026) started ~10–15 Jan 2026 per fee/hostel
records, but its final exams were dated 15 Sep–1 Oct 2026 — an 8-month gap
that doesn't match a real semester calendar. The exam dates themselves were
fine (deliberately near "today," and the dashboard frames them as "upcoming"
so they need to stay in the future) — the semester-2 start dates were what
didn't fit.

**Fixed:** Moved semester-2 fee/hostel dates forward from mid-Jan 2026 to
mid-May 2026 (tuition invoice, HELB/HEF disbursement, M-Pesa clearance,
hostel booking), so semester 2 now runs ~May–Sep 2026 with exams
mid-Sep–early-Oct — a normal semester length ending shortly before its own
exams, still consistent with "today" (29 Aug 2026) sitting near the end of
teaching. Semester 1 (Sep–Dec 2025, already graded) was untouched — it was
already internally consistent.

## 7. `GradeRecordEntity` primary key would collide

**Where:** `data/db/Entities.kt`

**Problem:** `@PrimaryKey val unitCode` — unit codes repeat across academic
years and on retake/supplementary, so two legitimate rows (e.g. the same
unit code taken in two different years) would collide on insert.

**Fixed:** Composite primary key `["unitCode", "academicYear", "semester"]`
via `@Entity(primaryKeys = [...])`. A supplementary retake for the same
unit/year/semester still `REPLACE`s the original row on insert — which is
correct, since a supplementary result supersedes the original mark on the
transcript.

## 8. No failure/edge cases in mock data

**Where:** `data/sample/MockDataProvider.kt`

**Problem:** Every grade passed, every request eventually resolved, every fee
statement reconciled cleanly. Worse, two existing records actively
contradicted their own resolved requests: `SMA 201`'s `GradeRecord` showed a
clean 73.0/A despite an *approved* supplementary request implying a 38.0
fail-then-resit, and `CSC 225`'s `isSpecial` flag was `false` despite an
*approved* special exam request for that exact unit.

**Fixed:** `SMA 201` now shows a policy-correct capped resit
(`totalScore = 40.0, gradeLetter = "D", isSupplementary = true`). `CSC 225`
now has `isSpecial = true`. Added a second, `REJECTED` special-exam request
(`STA 301`, insufficient documentation) and one genuinely failed, unresolved
unit (`CSC 219`, `isPass = false`) to actually exercise those UI/data paths.
Cumulative average moved from ~70.1% to ~64.85% (still First→Second Upper
band, plausible for a strong student with one capped resit and one
outstanding fail).

## 9. "Submit" flows had no error handling

**Where:** `data/repo/AcademicRepository.kt`, `FeeRepository.kt`,
`HostelRepository.kt`, and their ViewModels/screens

**Problem:** `submitSpecialExamRequest`, `submitSupplementaryRequest`,
`submitMissingMarksDispute`, `recordMpesaPayment`, and `payHostelRent`
mutated in-memory `StateFlow`s and always reported success — matching
`HostelRepository.bookRoom`'s already-correct pattern (fails when the hall
is full) nowhere else in the codebase.

**Fixed:** All five now return `Boolean` with a real validation rule —
duplicate in-flight/approved request rejection for the three academic
submissions, over-payment rejection for M-Pesa, and "no active/already-paid
booking" rejection for hostel rent. Every ViewModel now surfaces success vs.
failure via its existing message-state pattern, and every screen
(`SpecialExamScreen`, `SupplementaryScreen`, `MissingMarksScreen`,
`FeesScreen`) shows the failure and keeps the form open/editable instead of
blindly marking the request as submitted.

## 10. Auth accepted any non-blank password

**Where:** `data/repo/AuthRepository.kt`

**Problem:** `loginWithRegNo`/`loginWithActiveDirectory` accepted any
non-blank password (and, for AD login, any email merely containing the
right domain), fabricating a profile regardless. The failure-state UI in
`LoginViewModel`/`LoginScreen` already existed and worked — only the repo
never actually triggered it.

**Fixed:** Both methods now validate against one canonical demo credential
pair (`MockDataProvider.demoRegNo`/`demoStudentEmail`/`demoPassword`) and
return `false` otherwise. Demo login: reg no `P15/12345/2022` or email
`leo.wolfe@students.uonbi.ac.ke`, password `uon@2026`. No changes needed to
`LoginViewModel`/`LoginScreen`'s error path — it already worked correctly —
but its *pre-filled default* regNo/password state did need to change to
match, and now references `MockDataProvider`'s constants directly instead
of duplicating the literals a third time.

**Remaining loose end (not touched, pre-existing, out of scope):**
`sampleExamCard`, `sampleActiveBooking`, and the request lists still use a
different regNo (`"F16/28914/2022"`) than `sampleStudent`/the login
credential (`"P15/12345/2022"`) — this was true before any of these fixes
and wasn't part of what was asked.

## 11. Dates were formatted strings, nothing parsed them back

**Where:** new `data/util/DateFormats.kt`, `ui/dashboard/DashboardScreen.kt`

**Problem:** Dates across the app are hardcoded display strings (e.g.
`"15 Sep 2026"`); nothing parsed them back, so "how many days away" couldn't
be computed anywhere despite the Dashboard's "Upcoming Exam" framing
implying it.

**Fixed (intentionally scoped small):** Added one shared parsing utility
(`daysUntil`/`relativeDayLabel`, using `java.time` — `minSdk 31` supports it
natively, no desugaring/dependency needed) and used it in exactly one place,
the Dashboard's upcoming-exam banner, which now shows "in 17 days" etc.
alongside the existing date badge. Deliberately did **not** rewrite every
date field across the codebase to a real date type — that's a large
mechanical refactor with no other consumer today, out of scope until a
second feature actually needs date math.

---

The following were found during a Navigation 3 review (using the
`navigation-3` skill's reference recipes plus `android docs search/fetch`
against the live AndroidX docs — nav3 version in use: `1.1.6`).

## 12. Three declared `NavKey`s have no registered `entry` — crashes on navigate

**Where:** `ui/navigation/Destinations.kt`, `ui/navigation/MyUonNavDisplay.kt`

**Problem:** `UnitRegistration`, `FeeStatementDetail`, and `HostelBookingScreen`
are declared as `NavKey`s but none has a matching `entry<...> { }` in
`MyUonNavDisplay`'s `entryProvider`. Per the official Nav3 docs, the
`entryProvider` DSL "includes default fallback behavior (throwing an error)
if the key isn't found" — so `navigator.goTo(UnitRegistration)` (wired,
three layers deep, from `AcademicsScreen`'s `UnitRegistrationTabContent`,
via a callback (`onOpenFull`) that happens to never be invoked from any
visible button today) would crash the moment that dead wiring gets
connected to a button. `FeeStatementDetail`/`HostelBookingScreen` have zero
references anywhere outside `Destinations.kt` — pure orphaned scaffolding,
not reachable at all.

**Fix:** Remove the three dangling `NavKey`s and their dead callback
plumbing (`onOpenFull`/`onOpenUnitRegistration` chain) rather than stubbing
in placeholder screens for features that don't exist yet — no UI path
reaches any of them today, so there's nothing to preserve. Add them back
properly (declare the `NavKey` *and* wire a real `entry` *and* a real button
in the same change) if/when those screens actually get built.

**Fixed:** All three `NavKey`s and the dead `onOpenFull`/`onOpenUnitRegistration`
parameter chain (`MyUonNavDisplay` → `AcademicsScreen` →
`UnitRegistrationTabContent`) removed.

## 13. Tabs share one back stack

**Where:** `ui/navigation/Navigator.kt`, `ui/navigation/MyUonNavDisplay.kt`

**Problem:** `Navigator.switchTopLevel` clears the *entire* shared back
stack down to just the tapped destination. The official "Common UI"/
"Multiple back stacks" recipe keeps a separate `NavBackStack` per top-level
tab (a `Map<NavKey, NavBackStack<NavKey>>`, "exit through home" pattern —
`Dashboard`'s stack is always present, plus at most one other tab's), each
with its own `SaveableStateHolder` decorator, so drilling into e.g.
`UnitDetail` from Academics and then switching to Fees and back returns you
to `UnitDetail`, not the Academics tab root. Right now every tab switch
silently discards whatever was drilled into on the tab being left.

**Fix:** Rework `Navigator` to hold one back stack per top-level
destination instead of one shared list, and have `MyUonNavDisplay` combine
the decorated entries from the *current* tab's stack (plus `Dashboard`'s, if
not current) for `NavDisplay`, per the recipe's `toDecoratedEntries` pattern.

**Fixed (simplified from the plan above):** `Navigator` now holds one
`SnapshotStateList<NavKey>` per top-level tab internally, and keeps a single
flat `backStack` (still just a plain list, same shape `MyUonNavDisplay`
already consumed) in sync after every mutation — "exit through home" order:
`Dashboard`'s stack, then the current tab's stack appended if it isn't
`Dashboard`. Deliberately skipped the official recipe's
`rememberNavBackStack`/per-tab `SaveableStateHolder`/`entries=` machinery —
that's Composable-scoped persistence infrastructure that belongs to issue
#15 (still backlog), not needed to fix the actual bug (tab switches
discarding in-tab history). `MyUonNavDisplay.kt` needed no changes at all;
the fix is fully contained to `Navigator`.

## 14. Login isn't gated through the nav layer

**Where:** `ui/navigation/Navigator.kt`

**Problem:** `Navigator.backStack` is hardcoded to start at `Dashboard`
regardless of `AuthRepository.isLoggedIn`'s real value. `Login` is only ever
reached via an explicit `onLogout` UI callback (`navigator.replaceAll(Login)`),
not because the nav layer checked auth state. The official "Conditional
navigation" recipe centralizes this in the navigator (a `requiresLogin`
check inside `navigate()` that redirects to `Login`, remembering the
original target, and back on success). This app doesn't need the full
per-destination `requiresLogin` marker (the whole app is student-portal-only
— there's no anonymous section), but it does need its *startup* destination
to reflect real auth state instead of assuming logged-in.

**Fix:** Inject `AuthRepository` into `Navigator` and compute the initial
back stack entry from `authRepository.isLoggedIn.value` (`Login` if false,
`Dashboard` if true) instead of hardcoding `Dashboard`.

**Fixed:** `Navigator`'s `init` block now seeds `backStack` from
`authRepository.isLoggedIn.value` read once at construction (not
reactively — logout/login are already explicit calls into `Navigator`, not
something that flips underneath it). Logout (`replaceAll(Login)`) also now
resets every tab's stack back to its own root, so a fresh login never shows
leftover drilled-in state from before the previous logout.

## 15. Back stack won't survive process death

**Where:** `ui/navigation/Navigator.kt`

**Problem:** `Navigator.backStack` is a plain `mutableStateListOf(Dashboard)`
inside an `@ActivityRetainedScoped` Hilt class — survives rotation (via
Hilt's retained scope), but not process death, since it's never built via
`rememberNavBackStack` (Nav3's documented convenience method "designed to
create a back stack that persists across configuration changes **and
process death**") nor manually serialized. All the destinations are already
`@Serializable`, so the groundwork is there, just not wired to a persistence
path.

**Fixed:** Moved back-stack ownership from the Hilt-scoped singleton into
composition. `Navigator` is no longer `@Inject`/`@ActivityRetainedScoped` —
it's constructed by a new `@Composable fun rememberNavigator(authRepository)`
in the same file, called once in `MainActivity`'s `setContent { }` (which
still gets `AuthRepository` from Hilt as before). Each tab's back stack and
the current-tab state are created with `rememberSerializable` +
`SnapshotStateListSerializer`/`MutableStateSerializer`, using Nav3's own
`NavKeySerializer` as the element serializer — confirmed by reading its
actual source (`androidx.navigation3.runtime.serialization`, v1.1.6): it
resolves each concrete `NavKey`'s own generated serializer via
`Class.forName(...).kotlin.serializer()` at encode/decode time, so no
hand-written `SerializersModule` registering all ~13 concrete `NavKey`
types was needed. `backStack` itself stays a plain, non-persisted list —
it's fully re-derived (`syncBackStack()`) from the two persisted sources on
every (re)construction, including after process death.
