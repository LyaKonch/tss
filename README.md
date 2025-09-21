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
