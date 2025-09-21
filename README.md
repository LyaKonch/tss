# Bravo Timesheet System (TSS)

Welcome to Team Bravo's Timesheet System, a Jakarta EE application!
The application is split into an EJB (business **logic**) and a WAR (Jakarta **faces**) module.

**Every team member** (and those hosting the app) will have to complete an [initial configuration](docs/setup.md).

There are currently open [decisions](docs/pending_decisions.md) we should make sooner rather than later.

## 📁 Project Structure

```
bravo-tss/
├── logic/         # EJB module (packaged as JAR)
├── faces/         # Web module (packaged as WAR)
├── nbproject/     # NetBeans project configuration
├── .idea/         # IntelliJ IDEA project configuration
└── docs/          # Documentation (here or wiki or ...)
```
Fullfilled requirements:
- [x] CN 1
- [x] CN 2
- [x] CN 4
- [x] CN 4a
- [x] CN 4b
- [x] CN 4c
- [x] CN 4d
- [x] CN 4e
- [x] CN 5
- [x] CN 6
- [x] CN 6a
- [x] CN 7
- [x] CN 8
- [x] CN 9
- [x] CN 10
- [x] CN 12
- [x] TS 1
- [x] TS 2
- [x] TS 3
- [x] TS 4
- [x] TS 5
- [x] TS 6
- [x] TS 7
- [x] TS 9
- [x] SG 1
- [x] SG 1a
- [x] SG 2
- [x] RE 1
- [x] RE 2
- [x] RE 3
- [x] RE 4
- [x] RE 5
- [x] AR 1
- [x] AR 2
- [x] AR 3
- [x] AR 4
- [x] AC 1
- [x] AC 3
- [x] UI 1
- [x] UI 3
- [x] UI 4
- [x] IN 1 <- all except for menu tabs
- [x] IN 2
- [x] IN 3
- [x] IN 4
- [x] IN 5
- [x] IN 6
- [x] SA 1
- [x] SA 2
- [x] SA 3
- [x] SA 4
- [x] SA 6
- [x] PM 1
- [x] PM 2
- [x] PM 3
- [x] PM 4 we didn't change any requirement up to us

Unfullfilled requirements:
- [ ] CN 3 
- we are calculating it in business logic, but just forgot to show it. Basic details about timesheet and contracts are shown in respective forms.
- [ ] CN 11
- no particular reason, it slipped out of attention
- [ ] SG 4
- it appeared to us that it's too overhead and have no room for integration in our small project
- [ ] TS 8 <- browsing printing functionality was used
- [ ] SG 3 no idea what is meant by requesting changes. the staff can just call the supervisor and ask for change if needed
- [ ] AC 2 <- we can check user group role for being STAFF, but not university STAFF member. We tried implementing uni LDAP, but were seeing no success and eventually gave up on this idea.
- [ ] UI 2 <- basic flexibility provided by primeface. Tables reflow, but text and structure are not adjusting according to the window size 
- [ ] SA 5 <- We didn't see a point in documenting architectural decisions because we discussed it in person and in discord channel. We have the project structure overview in README, otherwise we followed the Contacts example.
- [ ] PM 5 <- It was up to everyone. Some did manage to track time and tasks, but others lost the track of documenting time slots. 