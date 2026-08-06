# Project Structure

## Purpose

This document defines the folder structure of the Nestory project.

Its purpose is to help every team member:

- know where to place new files,
- know where to find existing code,
- know where to place new files,
- know where to find existing code,
- infer where implementation belongs from the Jira task without asking other team members.

---

## Feature-based Structure

Each feature has its own package under `ui/`.

```
ui/
  <feature_name>/
```

Each feature package contains

```
<feature_name>/
  <FeatureName>Screen.kt
  <FeatureName>ViewModel.kt
  <FeatureName>UiState.kt
```

New feature -> add one new package in the same format. There is no need to update this document when a new feature appears in a later sprint.
Additional files (e.g., dialogs, components, or navigation) should remain inside the same feature package whenever possible.

---

## Data Layer

```
data/
  database/
  entity/
  dao/
  repository/
```

| Folder | Purpose |
|---------|---------|
| database | Room database configuration |
| entity | Database entities (1 file = 1 table) |
| dao | Room DAO interfaces |
| repository | Data access layer, the bridge between ViewModel and DAO |

---

## Common

```
common/
```

Shared UI components used across multiple features.

Examples: `Button`, `Dialog`, `Card`, `Loading`

---

## Utils

```
utils/
```

Shared helper classes.

Examples: `Constants`, `Extensions`, `DateUtils`

---

## Navigation

```
navigation/
```

Contains the navigation graph for the entire app.

Examples: `NavGraph`, `Destination`

---

# Mapping Jira Tasks to Project Structure

Jira tasks in Nestory follow the naming convention:

```
<Role> - <Description>
```

For example:

- `UI - Design Document Record Screens`
- `DB - Implement Core Room Database`
- `App - Implement Core Repository Layer`

A developer should be able to infer where the implementation belongs directly from the Jira task.

---

## Step 1 — Determine the Layer

The task prefix (`Role`) identifies the primary implementation layer.

| Jira Role | Expected Location |
|------------|-------------------|
| UI | `ui/<feature>/` |
| User | `ui/<feature>/` (ViewModel and user interaction logic) |
| App | `data/repository/` or application-level integration |
| DB | `data/database/`, `data/entity/`, `data/dao/` |
| Security | Feature-related security implementation (`data/` or `utils/`) |
| QA | Test packages corresponding to the feature |
| Docs | `docs/` |
| PM | `pa/` |

---

## Step 2 — Determine the Feature Package

The Jira Epic identifies the feature package.

Current Sprint 3 mapping:

| Jira Epic | Feature Package |
|------------|-----------------|
| Document Records | `ui/document/` |
| Category Management | `ui/category/` |
| Original Location Tracking | `ui/location/` |
| Biometric App Lock | `ui/security/` |
| Scanner & Attachment | `ui/scanner/` |
| OCR Text Extraction | `ui/ocr/` |
| Document Library | `ui/library/` |
| Document Kit Management | `ui/kit/` |
| Expiry Reminder & Notification | `ui/reminder/` |
| Backup & Restore | `ui/backup/` |

The following Epics represent shared infrastructure instead of feature-specific UI:

| Jira Epic | Expected Location |
|------------|-------------------|
| App Foundation & Layout | `navigation/`, `ui/home/`, application setup |
| Local Database & Core | `data/database/`, `data/entity/`, `data/dao/`, `data/repository/` |

---

## Example

Task:

```
UI - Design Document Kit Screens
```

Epic:

```
Document Kit Management
```

Expected implementation:

```
ui/
└── kit/
    ├── DocumentKitScreen.kt
    ├── DocumentKitViewModel.kt
    └── DocumentKitUiState.kt
```

---

Task:

```
DB - Implement Core Room Database
```

Epic:

```
Local Database & Core
```

Expected implementation:

```
data/
├── database/
├── entity/
├── dao/
└── repository/
```

---

## Rules

- Every new feature must have its own package under `ui/`.
- Do not create duplicate folders with similar purposes.
- UI code must stay inside `ui/`.
- Database code must stay inside `data/database/`.
- Shared components belong in `common/`.
- Feature package names should closely match the Jira epic name so they are easy to trace back.
- Prefer splitting features into separate packages instead of grouping them together, even when two features are closely related (e.g. Scanner and OCR, Home and Library) - this helps each feature be developed/tested independently and reduces conflicts when multiple people are working in parallel.