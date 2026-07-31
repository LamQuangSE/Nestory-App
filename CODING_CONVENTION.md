# Coding Convention

## Purpose

This document answers one question:

> If two people code the same feature, will their code look the same?

This is a checklist, not a textbook. Keep it practical and follow the existing code before inventing a new style.

---

## 1. General Principles

- Write readable code before clever code.
- Keep classes focused on one responsibility.
- Prefer composition over duplication.
- Follow the current project structure before creating new packages.
- Keep behavior changes separate from formatting-only changes when practical.
- Do not include files unrelated to the task.

---

## 2. Naming Convention

Packages use lowercase names, with no uppercase letters and no underscores.

```kotlin
// Good
ui.document

// Avoid
ui.DocumentUI
ui.Document_UI
```

Classes use PascalCase.

```kotlin
DocumentScreen
DocumentRepository
ReminderDao
```

Functions and variables use camelCase.

```kotlin
saveDocument()
documentTitle
selectedCategory
```

Constants use UPPER_SNAKE_CASE.

```kotlin
MAX_ATTACHMENT_SIZE
```

Booleans should start with `is`, `has`, `can`, or `should`.

```kotlin
isExpired
hasPermission
canScan
shouldShowError
```

Test function names should follow:

```text
<action>_<condition>_<expected_result>
```

```kotlin
saveDocument_whenTitleEmpty_throwsException()
loadReminder_whenExpired_returnsEmptyList()
```

---

## 3. Kotlin Style

- Use `val` whenever possible.
- Use `var` only when mutation is required.
- Avoid nullable types unless the data can really be absent.
- Prefer expression bodies for simple functions.
- Keep functions short enough to scan without scrolling heavily.
- Use trailing commas in multi-line argument lists, annotation parameters, arrays, and constructors.
- Keep imports ordered: AndroidX imports first, project imports next, Kotlin imports last when present.
- Do not use fully qualified class names in function bodies when a normal import is clearer.
- Avoid inline comments on obvious properties. Use comments for non-obvious rules or tradeoffs.

```kotlin
// Good
data class DocumentEntity(
    val id: Long = 0,
    val title: String,
)

// Avoid
data class DocumentEntity(
    val id: Long = 0, // auto generated id
    val title: String // document title
)
```

---

## 4. Jetpack Compose Style

Screens build UI and forward events. They should not contain database or repository logic.

```kotlin
// Good
@Composable
fun DocumentScreen(viewModel: DocumentViewModel) {
    val uiState by viewModel.uiState.collectAsState()
}

// Avoid
@Composable
fun DocumentScreen() {
    database.insert()
    repository.save()
}
```

- Composable names use PascalCase: `ReminderCard()`, `DocumentItem()`.
- Screen composables expose user actions as lambdas: `onBack`, `onCreateVault`, `onUnlocked`.
- Keep navigation decisions in navigation-level composables, not reusable UI components.
- State should come from ViewModel using `collectAsState()` when the state is business state.
- Local UI-only state is acceptable for short-lived UI behavior such as pressed state or temporary PIN input.
- Use imported Compose classes instead of fully qualified names inside UI code.
- Use trailing commas in multi-line composable calls.

```kotlin
// Good
Text(
    text = "Create vault",
    modifier = Modifier.fillMaxWidth(),
    textAlign = TextAlign.Center,
)

// Avoid
Text(
    text = "Create vault",
    modifier = Modifier.fillMaxWidth(),
    textAlign = androidx.compose.ui.text.style.TextAlign.Center
)
```

---

## 5. Layer Access Rule

The application uses a simple MVVM-style structure.

```text
UI -> ViewModel -> Repository -> RepositoryImpl -> DAO -> Room Database
```

Mandatory rules:

- UI must not access DAO directly.
- ViewModel should depend on repository interfaces, not DAO.
- Repository is the data access boundary for ViewModel.
- DAO should contain SQL only. Business validation belongs in repository or higher-level use cases.

---

## 6. Room and Data Layer Convention

Use these suffixes consistently:

```text
Entity      -> DocumentEntity
DAO         -> DocumentDao
Repository  -> DocumentRepository
Impl        -> DocumentRepositoryImpl
Database    -> AppDatabase
```

Do not use vague database names.

```kotlin
// Avoid
DBHelper
DatabaseManager
```

Folder rules:

- `data/entity`: Room entities. One file should represent one database table.
- `data/dao`: Room DAO interfaces and SQL queries.
- `data/database`: Room database configuration.
- `data/database/converter`: Room type converters.
- `data/model`: domain values that are not tables, such as enums.
- `data/repository`: repository interfaces and implementations.
- `data/filesystem`: local file and vault setup logic, not SQL.
- `relation`: Room query result classes across multiple tables. These are not entities.

---

## 7. DAO Query Naming

Use `observe...` only for reactive Room queries returning `Flow`.

```kotlin
@Query("SELECT * FROM documents WHERE id = :documentId LIMIT 1")
fun observeById(documentId: Long): Flow<DocumentEntity?>
```

Use `get...` only for one-shot reads. These functions should usually be `suspend`.

```kotlin
@Query("SELECT * FROM documents WHERE id = :documentId LIMIT 1")
suspend fun getById(documentId: Long): DocumentEntity?
```

Do not write `get...` methods that return `Flow`.

```kotlin
// Avoid
fun getById(documentId: Long): Flow<DocumentEntity?>
```

Keep `observe` and `get` pairs close together when both exist.

---

## 8. Repository Convention

Repository interfaces expose app-facing operations and hide DAO details.

```kotlin
interface DocumentRepository {
    fun observeDocumentById(documentId: Long): Flow<DocumentEntity?>
    suspend fun getDocumentById(documentId: Long): Result<DocumentEntity?>
}
```

Repository implementations delegate to DAO.

```kotlin
class DocumentRepositoryImpl(
    private val documentDao: DocumentDao,
) : DocumentRepository
```

Use `Result` for write operations and one-shot reads where callers may need failure information.

```kotlin
override suspend fun createDocument(document: DocumentEntity): Result<Long> =
    runCatching { documentDao.insert(document) }
```

---

## 9. SQL Formatting

Short queries can stay on one line.

```kotlin
@Query("SELECT * FROM documents WHERE id = :documentId LIMIT 1")
```

Long queries must use multi-line SQL.

```kotlin
@Query(
    """
    SELECT * FROM documents
    WHERE (:category IS NULL OR category = :category)
        AND (:isFavorite IS NULL OR is_favorite = :isFavorite)
    ORDER BY title COLLATE NOCASE
    """
)
fun filterDocuments(
    category: DocumentCategory?,
    isFavorite: Boolean?,
): Flow<List<DocumentEntity>>
```

---

## 10. Git and Generated Files

- Do not commit generated caches such as `__pycache__/` or `*.pyc`.
- Do not commit local machine files such as `local.properties`.
- Do not commit build outputs such as APKs, AABs, or Gradle build folders.
- Before merging feature branches, make sure `git status` is clean.
- If a branch mixes UI and data work, merge intentionally by layer instead of resolving conflicts blindly.

---

## 11. Verification Commands

Run these before opening or merging a pull request:

```bash
cd src
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew lintDebug
```

The current Room schema export warning is not a blocker, but it should be fixed before adding database migrations.

---

## 12. Code Quality Checklist

Before creating a pull request:

- Build succeeds.
- Unit tests pass.
- Lint passes.
- Unused imports are removed.
- Commented-out code is removed.
- Naming follows this document.
- Unrelated files are not included.
- `observe...` methods return `Flow`.
- `get...` methods are one-shot reads and do not return `Flow`.

---

## Not Included

- The full Google Kotlin Style Guide.
- SOLID theory.
- Clean Architecture. This project follows a simple MVVM approach.
