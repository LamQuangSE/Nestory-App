
---

# Project Structure

app/
│
├── ui/
│   ├── home/
│   ├── document/
│   ├── reminder/
│   ├── scanner/
│   └── ...
│
├── data/
│   ├── database/
│   ├── entity/
│   ├── dao/
│   └── repository/
│
├── navigation/
│
├── common/
│
├── utils/
│
└── MainActivity.kt

## Purpose

This document defines the project structure used in Nestory.

A consistent folder structure helps every team member:

* understand the architecture of the application,
* know where new code should be placed,
* quickly locate existing code,
* reduce merge conflicts during team development,
* infer where implementation belongs directly from the Jira task without asking other team members.

Rather than organizing files by technical type only, Nestory combines **Feature-based organization** for the UI layer and **Layer-based organization** for the data layer. This keeps related files close together while maintaining clear architectural boundaries.

---

# Overall Architecture

The application follows a layered architecture.

```text
User

↓

Screen

↓

ViewModel

↓

Repository

↓

DAO

↓

Room

↑

Flow<List<DocumentEntity>>

↑

Repository

↑

ViewModel

↑

StateFlow<DocumentUiState>

↑

Compose recomposition
```

Each layer has a single responsibility.

| Layer         | Responsibility                               |
| ------------- | -------------------------------------------- |
| Screen        | Display UI and receive user interactions     |
| ViewModel     | Manage UI state, user actions, and lightweight business logic |
| Repository    | Coordinate data access and expose clean APIs |
| DAO           | Execute database queries                     |
| Room Database | Manage database configuration                |
| SQLite        | Store local application data                 |

For example, when a user creates a document:

```text
User taps "Save"

↓

DocumentScreen

↓

DocumentViewModel

↓

DocumentRepository

↓

DocumentDao

↓

Room Database

↓

SQLite
```

The UI never communicates directly with the database.

---

# Why Feature-based UI?

Nestory is organized around user features rather than technical file types.

Instead of:

```text
screens/
viewmodels/
states/
```

the project groups everything related to one feature together.

Example:

```text
ui/
└── document/
    ├── DocumentScreen.kt
    ├── DocumentViewModel.kt
    └── DocumentUiState.kt
```

All files required for the Document feature stay in one package.

### Why?

Suppose Jira assigns:

```text
UI - Improve Document Screen
```

The developer immediately knows to work inside

```text
ui/document/
```

instead of searching across multiple folders.

This also allows multiple developers to work on different features with fewer merge conflicts.

---

# UI Layer

```text
ui/
    <feature_name>/
```

Each feature package contains:

```text
<feature_name>/

FeatureScreen.kt

FeatureViewModel.kt

FeatureUiState.kt
```

Additional files such as dialogs, feature-specific components, or navigation helpers should remain inside the same feature package whenever possible.

### Example

```text
ui/
└── reminder/
    ├── ReminderScreen.kt
    ├── ReminderViewModel.kt
    ├── ReminderUiState.kt
    └── ReminderDialog.kt
```

Everything related to Reminder belongs together because it represents one user feature.

---

# Data Layer

Unlike the UI, the data layer is organized by responsibility rather than feature.

```text
data/

    database/

    entity/

    dao/

    repository/
```

Why?

The same database objects are often shared by multiple features.

For example,

DocumentEntity is used by:

* Document Library
* Reminder
* Scanner
* Search
* Document Kit

Duplicating database code for every feature would make maintenance difficult.

Instead, all persistence-related code is centralized inside `data/`.

---

## database/

```text
data/database/
```

### Responsibility

Contains the Room database configuration.

Typical contents:

* AppDatabase
* Database version
* Registered entities
* Registered DAOs

This folder should never contain business logic.

### Example

```text
AppDatabase.kt
```

Registers

```text
DocumentEntity

ContainerEntity

ReminderEntity
```

and exposes

```text
DocumentDao()

ReminderDao()
```

---

## entity/

```text
data/entity/
```

### Responsibility

Represents database tables.

One Entity equals one database table.

Entities only describe the database schema.

They should not contain business logic.

### Example

```text
DocumentEntity.kt
```

represents

```text
documents
```

table.

```text
id

title

category

expiration_date
```

Each property becomes a database column.

---

## dao/

```text
data/dao/
```

### Responsibility

DAO (Data Access Object) is the only layer allowed to communicate directly with Room.

DAO performs:

* INSERT
* UPDATE
* DELETE
* SELECT

DAO should never contain application logic.

### Example

```text
DocumentDao
```

contains

```text
insert()

update()

delete()

getDocumentById()
```

It does not decide **when** a document should be inserted.

It only knows **how**.

---

## repository/

```text
data/repository/
```
For this project, repositories primarily act as a data access layer between ViewModel and DAO. Most repository functions simply delegate CRUD operations to DAO while exposing a clean API to the UI layer. Repositories may coordinate multiple DAO calls when necessary, but business rules should remain lightweight.

### Responsibility

Repositories in Nestory follow a lightweight data repository pattern.

Repositories primarily forward CRUD operations to DAO and expose clean APIs to ViewModel.

Business logic should remain minimal.

Repositories are not intended to become service classes.

Repository sits between ViewModel and DAO.

Its job is to expose clean APIs to the application and coordinate data access.

Repositories may combine multiple DAO calls when necessary.

The UI should never call DAO directly.

Repository coordinates data access and should avoid business rules.
### Example

```text
DocumentRepository
```

may internally use

```text
DocumentDao

AttachmentDao

ReminderDao
```

to build a complete Document Detail.

The ViewModel does not need to know how many DAOs are involved.

---

# Common

```text
common/
```

### Responsibility

Contains reusable UI components shared by multiple features.

Only components used by several features belong here.

Feature-specific UI components should remain inside their own feature package.

### Example

```text
common/

Loading.kt

PrimaryButton.kt

ConfirmationDialog.kt
```

These components can be reused by

* Document
* Reminder
* Backup
* Scanner

without duplication.

---

# Utils

```text
utils/
```

### Responsibility

Contains reusable helper classes that do not belong to any specific business feature.

Utilities should remain independent from application logic.

If a helper is only used by one feature, keep it inside that feature instead.

### Example

```text
DateUtils.kt
```

Used by

* Reminder
* Backup
* Document

to format timestamps.

Another example

```text
FileUtils.kt
```

provides helper methods for

* file size formatting
* file extension extraction

without depending on any feature.

---

# Navigation

```text
navigation/
```

### Responsibility

Defines how users move between screens.

Navigation contains routes and navigation graphs only.

It should not contain UI or business logic.

### Example

```text
NavGraph.kt
```

defines

```text
Home

↓

Library

↓

Document Detail

↓

Reminder
```

When the user taps a document,

Navigation decides which screen opens next.

---

# Mapping Jira Tasks to Project Structure

Jira tasks follow the format

```text
<Role> - <Description>
```

The role indicates **which layer** should be modified.

The Epic identifies **which feature** is affected.

A developer should be able to locate the implementation without asking another team member.

---

## Step 1 — Determine the Layer

| Jira Role | Expected Location                                      |
| --------- | ------------------------------------------------------ |
| UI        | `ui/<feature>/`                                        |
| User      | `ui/<feature>/` (ViewModel and user interaction logic) |
| App       | `data/repository/` or application-level integration    |
| DB        | `data/database/`, `data/entity/`, `data/dao/`          |
| Security  | `data/` or `utils/` depending on responsibility        |
| QA        | Test packages corresponding to the feature             |
| Docs      | `docs/`                                                |
| PM        | `pa/`                                                  |

---

## Step 2 — Determine the Feature

| Jira Epic                      | Feature Package |
| ------------------------------ | --------------- |
| Document Records               | `ui/document/`  |
| Original Location Tracking     | `ui/location/`  |
| Scanner & Attachment           | `ui/scanner/`   |
| OCR Text Extraction            | `ui/ocr/`       |
| Document Library               | `ui/library/`   |
| Document Kit Management        | `ui/kit/`       |
| Expiry Reminder & Notification | `ui/reminder/`  |
| Backup & Restore               | `ui/backup/`    |
| Biometric App Lock             | `ui/security/`  |

Infrastructure Epics:

| Jira Epic               | Expected Location                                                 |
| ----------------------- | ----------------------------------------------------------------- |
| App Foundation & Layout | `navigation/`, `ui/home/`                                         |
| Local Database & Core   | `data/database/`, `data/entity/`, `data/dao/`, `data/repository/` |

---

# Rules

* Every feature owns its own package under `ui/`.
* Every database table has exactly one Entity.
* DAO is the only layer allowed to access Room directly.
* ViewModel must communicate through Repository, never directly with DAO.
* Shared UI components belong in `common/`.
* Shared helper utilities belong in `utils/`.
* Navigation should only manage screen transitions.
* Keep business logic out of Entity and DAO.
* Prefer feature isolation to reduce merge conflicts and simplify maintenance.

---

How to Decide Where New Code Belongs

Whenever implementing a new feature, ask the following questions.

1. Am I creating or modifying the user interface?

Examples

Screen layout
Button
Dialog
TextField
Compose UI

→ Place the code inside

ui/<feature>/

Example

ui/document/
    DocumentScreen.kt
2. Am I handling user interaction or UI state?

Examples

Button click
Screen state
Validation before calling Repository
Loading indicator
Snackbar state

→ Place the code inside

ui/<feature>/

specifically

<Feature>ViewModel.kt
3. Am I reading or writing data?

Examples

Save document
Delete reminder
Query containers
Search documents

→ Place the code inside

data/repository/

Repositories coordinate data access and expose APIs to ViewModels.

4. Am I writing SQL or Room queries?

Examples

SELECT

INSERT

UPDATE

DELETE

→ Place the code inside

data/dao/

Only DAO communicates with Room.

5. Am I changing the database structure?

Examples

New table
New column
Foreign key
Entity fields

→ Modify

data/entity/

and

data/database/AppDatabase.kt
6. Am I creating reusable UI components?

Examples

PrimaryButton

LoadingIndicator

ConfirmationDialog

used by multiple features.

→ Place inside

common/
7. Am I creating helper functions?

Examples

Date formatting

File utilities

Encryption helper

→ Place inside

utils/
8. Am I changing navigation?

Examples

Add a new screen
Navigate to Reminder
Navigate back

→ Modify

navigation/



| If you are implementing...	| Put the code in... |
| ----------------------- | ----------------------------------------------------------------- |
| A new screen	| ui/<feature>/ |
| Button click logic	| <Feature>ViewModel.kt|
|Screen state	| <Feature>UiState.kt|
|CRUD operations	| data/repository/|
|SQL query	| data/dao/|
|New database table	| data/entity/|
|Register Room database	| data/database/|
|Shared UI component	| common/|
|Helper function	| utils/|
|Navigation	| navigation/|