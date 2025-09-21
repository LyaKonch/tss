# Open Questions

to decide before the project starts

## Q1: Package Structure Pattern

DDD/Feature-Based or Technical Layering or a mix? If mixed, how?  
Technical layering then DDD/Feature-Based or vice versa?
<details>
  <summary>Explanation (by Copilot)</summary>

### 🧬 Technical Layering (by type)

<details>
  <summary>Example Structure</summary>

```
src/
├── controllers/
│   ├── WorkEntryController.ts
│   └── ApprovalController.ts
├── services/
│   ├── WorkEntryService.ts
│   └── ApprovalService.ts
├── repositories/
│   ├── WorkEntryRepository.ts
│   └── ApprovalRepository.ts
├── models/
│   ├── WorkEntry.ts
│   └── Approval.ts
├── dtos/
│   ├── CreateWorkEntryDTO.ts
│   └── ApproveEntryDTO.ts
├── utils/
│   └── DateHelper.ts
└── index.ts
```

</details>

**Pros:** Familiar, easy to onboard.  
**Cons:** WorkEntry logic is scattered across multiple folders.

### 🧱 DDD / Feature-Based (by domain)

<details>
  <summary>Example Structure</summary>

```
src/
├── work-entry/
│   ├── WorkEntry.ts
│   ├── WorkEntryService.ts
│   ├── WorkEntryRepository.ts
│   ├── WorkEntryController.ts
│   ├── CreateWorkEntryDTO.ts
│   └── WorkEntryStateMachine.ts
├── approval/
│   ├── Approval.ts
│   ├── ApprovalService.ts
│   ├── ApprovalRepository.ts
│   ├── ApprovalController.ts
│   └── ApproveEntryDTO.ts
├── shared/
│   ├── utils/
│   │   └── DateHelper.ts
│   └── types/
│       └── Result.ts
└── index.ts
```

</details>

**Pros:** High cohesion, easier to reason about features.  
**Cons** Slightly more upfront structure, might feel “heavy” for small apps.
</details>

## Q2: Documentation

We have to decide what we want to document where.  
A Project Manual is required in some form.

> The project team SHALL document architectural decisions, e.g., which subsystems do exist, how is the source code
> structured, etc. in the project manual.

> The deployment process for the TSS SHALL be documented in the project manual (can be online, e.g. GitLab Wiki) such
> that the customer can install the system on a fresh Glassfish server.

> Decisions made to detail and/or change requirements SHALL be documented in the project manual.

- PDF document
- Markdown files in docs/ folder
- Gitlab Wiki pages
- ...

Sidenote:  
We can maybe also do a FAQ/Help section in the site itself. Though this isn't a direct requirement following:

> Guests may only view public information and documentation about the TSS.

## Q3: Git Workflow

I suggest using feature branches and merge requests with peer reviews.  
In most cases code should only be merged after one to two approvals.  
Everything should converge into main before we submit the project.

Everyone is welcome to open Merge Requests early, i.e. as Drafts while not yet ready to merge.

Does this align with how everyone prefers to work? Do you have alternative proposals?

## Q4: Regular Meetings (and documentation)

We need to agree on the format and frequency of regular meetings.

- In-person, online
- short status updates, peer programming sessions/collaborative work, get together

For shorter meetings, we could assign one person per meeting to take notes and document decisions.  
These notes should be shared afterward to support transparency and help those who couldn’t attend.

These notes will also need a place to live.
