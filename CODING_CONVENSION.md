# Coding Convention

## Purpose

This document answers exactly one question:

> **If two people code the same feature, will their code look the same?**

If yes, the convention works. This is a checklist, not a textbook. You can read it in 5 minutes and start using it.

---

## 1. General Principles

- Write readable code before writing clever code.
- Keep classes focused on a single responsibility.
- Prefer composition over duplication.
- Follow the existing project structure before creating new packages (see `PROJECT_STRUCTURE.md`).
- Keep implementations consistent across the team. If you are not sure how something should be written, find similar code already in the project and follow that style.

---

## 2. Naming Convention

**Package** — lowercase, no uppercase letters, no underscores:

```kotlin
// ✅
ui.document

// ❌
ui.DocumentUI
ui.Document_UI
```

**Class** — PascalCase:

```kotlin
DocumentScreen
DocumentRepository
ReminderDao
```

**Function** — camelCase:

```kotlin
saveDocument()
deleteDocument()
```

**Variable** — camelCase:

```kotlin
documentTitle
selectedCategory
```

**Constant** — UPPER_SNAKE_CASE:

```kotlin
MAX_ATTACHMENT_SIZE
```

**Boolean** — start with `is`, `has`, `can`, or `should`:

```kotlin
isExpired
hasPermission
canScan
```

**Test function** — describe using the format `<action>_<condition>_<expected_result>`:

```kotlin
saveDocument_whenTitleEmpty_throwsException()
loadReminder_whenExpired_returnsEmptyList()
```

---

## 3. Kotlin Convention

- Use `val` whenever possible.
- Use `var` only when mutation is required.
- Avoid nullable types unless necessary.
- Prefer expression body for simple functions.
- Keep functions under ~30 lines whenever practical.

---

## 4. Jetpack Compose Convention

**Screens only build UI and do not contain business logic:**

```kotlin
// ✅
@Composable
fun DocumentScreen(viewModel: DocumentViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    // only render UI based on uiState
}

// ❌
@Composable
fun DocumentScreen() {
    database.insert()
    repository.save()
}
```

- State must always come from the ViewModel using `collectAsState()`. Do not create business state inside a Composable.
- Composable names must use PascalCase: `ReminderCard()`, `DocumentItem()` - do not write `reminderCard()`.

---

## 5. Layer Access Rule

The detailed architecture is already covered in the proposal. This document only states the two mandatory rules when coding:

```
UI never accesses DAO directly.
Repository is the only data source for ViewModel.
```

Violating either of these rules is a required fix during code review.

---

## 6. Room Database Convention

```
Entity      → DocumentEntity
DAO         → DocumentDao
Repository  → DocumentRepository
Database    → AppDatabase
```

```kotlin
// ❌ Do not name it like this
DBHelper
DatabaseManager
```

---

## 7. Code Quality Checklist

Checklist before creating a Pull Request (see `GIT_WORKFLOW.md` for the detailed Git/PR process; this section only covers code quality):

- ✓ Build succeeds.
- ✓ Remove unused imports.
- ✓ Remove commented-out code.
- ✓ Follow the naming convention in section 2.
- ✓ Do not include files unrelated to the task.

---

## Code Examples

```kotlin
// ✅ Good
class DocumentRepository

// ❌ Bad
class document_repository
```

```kotlin
// ✅ Good
fun saveDocument()

// ❌ Bad
fun SaveDocument()
```

```kotlin
// ✅ Good
val isExpired = true

// ❌ Bad
val expired = true
```

---

## Not Included in This Document

- Detailed formatting rules (spacing, characters per line) - Android Studio handles that automatically.
- The full Google Kotlin Style Guide - too long for the scope of this project.
- SOLID - not needed for this project scope.
- Clean Architecture - the proposal does not use it; this project follows a simple MVVM approach.