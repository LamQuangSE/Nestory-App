
---

# Git Workflow

## 1. Purpose

This document defines the Git workflow used by the Nestory project to ensure a consistent development process across the team.

The objectives are to:

* Keep the `main` branch stable.
* Integrate completed features safely through `dev`.
* Ensure every change is reviewed before merging.
* Maintain traceability between Jira, GitHub, and Slack-up.

---

# 2. Naming Convention

| Item | Format | Notes |
| --- | --- | --- |
| Branch | `<type>/<jira-key>-<short-description>` | Description must be lowercase. |
| Commit | `<type>(scope): <jira-key> <description>` | Description must be lowercase. |
| Pull Request | `<jira-key> <type>(scope): <description>` | Description must be lowercase. |
| Jira Issue Name | `<area> - <action> <object>` | Keep it concise and action-oriented. |

Allowed types:

| Type | Meaning |
| --- | --- |
| feature | New feature |
| bugfix | Bug fix |
| refactor | Code refactor |
| chore | Setup, config, CI/CD |
| docs | Documentation |
| test | Unit test or integration test |

Examples:

```text
feature/SCRUM-96-implement-local-vault
bugfix/SCRUM-92-fix-room-foreign-key
docs/SCRUM-20-update-git-workflow
```

```text
feature(data): SCRUM-94 add document repository
bugfix(database): SCRUM-92 fix room foreign key
docs(workflow): SCRUM-20 update git convention
```

```text
SCRUM-94 feature(data): add document repository
SCRUM-96 feature(vault): implement local vault
```

Do not use `feat`. Use `feature` so branch, commit, and PR types match.

---

# 3. Pull Request Description Template

Use this structure as the default. Add extra notes when useful; the template does not need to be followed rigidly if the PR needs more context.

```markdown
## Jira
SCRUM-94

## What changed
- Added document repository interface and implementation.
- Added one-shot DAO reads for document and attachment flows.
- Standardized `observe...` and `get...` naming.

## Testing
- Ran `./gradlew testDebugUnitTest`
- Ran `./gradlew assembleDebug`
- Ran `./gradlew lintDebug`

## Screenshot
(if any)
```

`What changed` must be specific enough for reviewers to understand and test the change carefully.

---

# 4. Branch Strategy

```
main
│
└── dev
     │
     ├── feature/SCRUM-20-project-architecture
     ├── feature/SCRUM-21-home-dashboard
     ├── feature/SCRUM-22-local-vault
     ├── feature/SCRUM-92-document-core
     ├── feature/SCRUM-93-kit-backup
     ├── feature/SCRUM-94-document-core-repositories
     ├── feature/SCRUM-95-kit-backup-repositories
     └── bugfix/SCRUM-92-room-fk
```

---

# 5. Branch Responsibilities

## main

### Purpose

Production / Release branch.

Contains only stable, reviewed and fully tested code.

### Rules

* Never commit directly.
* Never push directly.
* Only merge from `dev`.
* Used for final demo and submission.

---

## dev

### Purpose

Integration branch.

All completed features are merged here before release.

### Rules

* Every feature branch creates a Pull Request into `dev`.
* Used for integration testing.
* Used for QA testing.
* Used for Sprint demonstrations.

---

## feature/*

### Purpose

Developer working branch.

Rules

* One Jira Issue = One Feature Branch.
* One Feature Branch = One Pull Request.
* Created from the latest `dev`.

Example

```
feature/SCRUM-92-document-core
```

---

## bugfix/*

### Purpose

Bug fixing branch.

Example

```
bugfix/SCRUM-92-room-foreign-key
```

---

# 6. Initial Repository Setup

Performed once by the Project Manager.

```bash
git checkout main
git pull origin main

git checkout -b dev
git push -u origin dev
```

Repository structure becomes

```
main
dev
```

---

# 7. Starting a New Task

Always create the feature branch from the latest `dev`.

```bash
git checkout dev

git pull origin dev

git checkout -b feature/SCRUM-92-document-core
```

Never create feature branches from `main`.

---

# 8. Development Workflow

Developer implements the assigned Jira task.

Commit frequently.

Example

```bash
git add .

git commit -m "feature(database): SCRUM-92 add container entity"
```

Push changes.

```bash
git push
```

---

# 9. Update Feature Branch Before Pull Request

Before opening a Pull Request, synchronize the feature branch with the latest `dev`.

```bash
git checkout dev

git pull origin dev

git checkout feature/SCRUM-92-document-core

git merge dev
```

Resolve conflicts if necessary.

Build the project again.

Push updated branch.

---

# 10. Create Pull Request

After development is completed:

```
feature/SCRUM-92-document-core
            │
            ▼
           dev
```

Create a Pull Request.

Use the PR title format:

```text
SCRUM-92 feature(database): add container entity
```

Never merge directly.

---

# 11. Code Review

The reviewer checks:

### Requirement

* Matches Jira description.
* Meets Acceptance Criteria.

### Code Quality

* Naming convention.
* Package structure.
* MVVM architecture.
* Repository pattern.
* Room implementation.
* No unnecessary files.

### Build Validation

For project-wide tasks such as:

* SCRUM-20
* Architecture
* Dependency updates

The reviewer should checkout the feature branch.

```bash
git fetch origin

git checkout feature/SCRUM-20-project-architecture
```

Verify:

* Gradle Sync
* Build
* Application runs successfully

If successful

Approve.

Otherwise

Request Changes.

---

# 12. Merge into dev

After approval

```
feature
     │
     ▼
    dev
```

Merge using **Create a merge commit**.

Do not squash unless agreed by the team.

---

# 13. QA Testing on dev

QA always tests the latest `dev`.

```bash
git checkout dev

git pull origin dev
```

Verify:

* Build
* Integration
* Navigation
* Database
* Repository
* UI
* Feature interaction

Example

```
SCRUM-20
SCRUM-21
SCRUM-22
```

QA ensures these tasks work together correctly.

---

# 14. Bug Fix Workflow

If a bug is found

Never modify code directly on `dev`.

Create a bugfix branch.

Example

```
bugfix/SCRUM-20-build-error
```

Workflow

```
bugfix

↓

Commit

↓

Push

↓

Pull Request

↓

Review

↓

Merge → dev
```

---

# 15. Release Workflow

At the end of the Sprint

```
dev
 │
 ▼
main
```

Create Pull Request.

Review.

Merge.

Now `main` contains the stable Sprint release.

---

# 16. Workflow Summary

```
Jira Issue

↓

Create Feature Branch

↓

Development

↓

Commit

↓

Push

↓

Update Feature Branch
(with latest dev)

↓

Build

↓

Pull Request

↓

Code Review

↓

Approve

↓

Merge → dev

↓

QA Testing

↓

Bug?

├── Yes → Bugfix Branch → PR → dev
│
└── No

↓

Sprint Complete

↓

Merge dev → main
```

---

# 17. Workflow for SCRUM-20 (Project Foundation)

```
Developer
(feature/SCRUM-20)

↓

Develop

↓

Commit

↓

Push

↓

Pull Request

↓

Reviewer

↓

Checkout branch

↓

Build

↓

Run application

↓

Approve

↓

Merge → dev

↓

Team pulls latest dev

↓

SCRUM-21

SCRUM-22

SCRUM-92

...

start from dev
```

SCRUM-20 must be completed and merged into `dev` before other implementation tasks begin.

---

# 18. Pull Request Checklist

Before requesting review:

* [ ] Jira issue is updated.
* [ ] Latest `dev` has been merged into the feature branch.
* [ ] Project builds successfully.
* [ ] No unnecessary files are included.
* [ ] Commit messages follow the convention.
* [ ] Pull Request title follows the convention.
* [ ] Pull Request description is completed.
* [ ] Slack-up is updated (if applicable).

---

# 19. Definition of Done

A Jira task is considered **Done** only when:

* ✅ Implementation is complete.
* ✅ Code has been committed and pushed.
* ✅ Latest `dev` has been merged into the feature branch.
* ✅ Pull Request has been approved.
* ✅ Changes have been merged into `dev`.
* ✅ Project builds successfully on `dev`.
* ✅ Acceptance Criteria are satisfied.
* ✅ Jira issue status is updated.
* ✅ No blocking issues remain.

---

# 20. Workflow Principles

Every team member should follow these principles:

1. One Jira Issue = One Feature Branch.
2. One Feature Branch = One Pull Request.
3. Never commit directly to `main`.
4. Never commit directly to `dev`.
5. Every code change must go through a Pull Request.
6. Only reviewed code can be merged into `dev`.
7. `dev` is the integration branch.
8. `main` is the release branch.
9. Always update your feature branch with the latest `dev` before creating a Pull Request.
10. A Jira issue is considered complete only after it satisfies the project's **Definition of Done**.

---
