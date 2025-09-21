# Worktime - Matthias Wellstein

## Project Setup

Creating a working template to start off with. Making sure it works in both NetBeans and IntelliJ as seamlessly as
possible.

- Always keeping NetBeans basically Plug and Play
- Testing possible/supported setups in IntelliJ using Ant/IntelliJ's Project Builder
- Testing Glassfish Setup in IntelliJ
- Checking useful/necessary Plugins and their setup/config files (e.g. JSF, persistence, EJB,...)
- Checking "Global Names" that need to be prefixed with our group name "bravo"
- Experimenting and Fixing with a million things to learn about Jakarta and it's edge cases for build/dist
- Writing an easy setup guide for everyone else in the with this created Gitlab repository
    - Git Clone
    - NetBeans Setup / Hot Reload
    - IntelliJ Setup / Hot Reload (was a bit more complicated compared to NetBeans)
    - Glassfish / MariaDB Setup (Global names require new, prefixed, jdbc entries and database)
- Preparing questions for our next meeting that need early discussion to reduce friction later

### Time spend

around 3 days or ~20 hours

### Interesting Problems

- The full setup of Ant in IntelliJ- while Ant is supported it's really not used much as it's as ancient as it gets
    - It was easier to use the build-in project structure feature to setup the build for IntelliJ
    - All the Facets and Configuration Descriptors were still a bit of a pain during the setup here
    - Running Glassfish requires a bit more setup, creating a run config, but contradictory to what i remember from the
      lecture you don't need multiple projects to run/deploy the application
        - in fact, you open a single project in IntelliJ while NetBeans opens a new Project for each module, making it
          one vs three if you want
- Naming of modules: faces & logic instead of projectname-war and projectname-ejb
    - NetBeans splits the default names in the project view- one above one below the build target / application
    - Generated distributionales have convoluted filenames like "projectname-war.war" and "projectname-ejb.jar"
    - Meanwhile everyone can tell by .war/.jar alone what is what
    - Having distinct names makes it easier "at a glance" to see what is what compared to having long prefixes in front
      of names
    - faces is a very direct reference to "Jakarta Server Faces" making this name very easily understood
    - logic is a not uncommon name for the business logic module and well.. makes logical sense to name it that
    - as a nice bonus: faces and logic are in a, including ours, default project setup right next to each other in the
      file tree
        - .idea -> build -> dist -> **faces** -> **logic** -> nbproject -> out -> src -> files
    - Generated files still keep their important bits: .war and .jar making it clear which is which
        - not that it matters how they are named since the application builds a single .ear anyway
        - this involves customizing project properties in nbproject, which is also why they are committed to git (
          similar case / easy of setup for what is committed from .idea)
    - Browsing through example projects, real websites and tutorials on Github showed that most project don't name their
      folders/modules -war/-ejb (anymore), so we follow modern developments
- Coding Standards (Style, Error Prevention, etc.)
    - This went unsolved for two reasons:
        - not sure if using tools/libraries to automated that would fall under using third party libraries
        - with Gitlab not having global runners in the University instance (to my knowledge) it would require a local
          setup for every team member via git prehooks or similar
- Project Structure (as in folder strucutre)
    - this went as a discussion question for our meeting. Deciding between the examples from the lecture using a
      technical layered structure (a directory for every type of class) vs. a feature based or DDD approach where
      distinct feature each get a directory containing all relevant classes no matter their type
    - we decided to follow the lecture examples
    - this structure was a bit of a mixed bag in the repositories i browsed through during this setup/research -
      generally larger projects seem to benefit most from a feature-based approach - so our rather small sized project
      should work fine with the tech layering

## Contract Page / Creation

- Added/Filled all necessary classes/files (DAO/DTO/Entity/Enums/Logic/LogicBean/BackingBean/xhtml)
- Changed some configuration to reflect those new files
- Implementing all necessary functionality to retrieve, show and create Contracts following the DomainModel
- To improve future merge this includes a small page-layout setup

### Time spend

02.07. 7,5h from 8pm to 3:30am (and with that its now time to go for reale to go for )
08.09. 1h from 6pm to 7pm
08.09. 2h from 11pm to 1am
11.09. 12h from 1pm to 1am (writing at 0:55, had like an hour meeting but also about the project at 5pm - finally can
create contracts and MR is open)

### Interesting Problems

- Setting up all those classes to pass objects around. Luckily not a real issue since I could take the Contacts example
  as reference.
    - Or so i thought all the interrelations between entities make for a hard start
- Noticed some minor IntelliJ Inspection hiccups and spend some time mitigating them. It's not ideal to have
  ide-specific comments, but this is still preferable to deactivating useful inspections that are plain missing in
  NetBeans and preventing those usually useful inspections from deleting "unused" xmlns declarations. No functionality
  loss, but a bunch of "unused" locales/methods that are at risk when cleaning up too fast.

### Open (Next task)

Refactor to layered architecture and streamline class usage/naming/dataflow

### Time spend

15.08. +-2days about 3 hours answering various questions and trying to troubleshoot issues remotely with Linda
22.08. 3h from 3:30pm to 6:30pm

### Interesting Problems

- None really. Mainly issues with the latest merge not booting. Fixed most stuff but will need to generate additional
  test data now that all entity relations were already defined but not all actually created/worked on yet.

### Open

Still refactor to more layer architecture.
Additionally seed the database with all entity types. Probably 3-10 entries each.

### Time spend

12.09. 10h from 1pm to 11pm (a meeting from 5pm to 8:20pm)

- reviewed the big MR from Yev and the EntryPage from Linda
- made a mockup UI for orientation because we never did that
- meeting (3 hours)
- Support IntelliJ in yev's branch. he added external libraries which was a mess + an email service that was a fine but
  a bit annoying

## In-between: TestDataSeeding

- Create a Seeder to provide some test data during dev
    - create several remaining DAOs
    - fix some entity inconsistencies
- Bidirectionality updates across entities
- Cascading updates
- Style/Order/Visibility of entity fields and methods

### Time spend

02.09. 4h from 1pm to 5pm
(Meeting) 02.09. 2,5h from 5pm to 7:30pm
07.09. 1,5h from 5pm to 6:30pm
07.09. 6h from 10pm to 4am

## Debugging with Yev

### Time spend

- 14.09. 4h from 10pm to 2am

## Review/refactor TimesheetEntryPage

- Basically everything aside from the xhtml page itself
- works now to be able to save a new Entry using the layered architecture

Also fitted a 2h merging session in, where i helped Yev setup intelliJ because it has the only decent merge interface
for something so complex as his double structure because he never updated main since idk june or sth.

### Time spend

- 15.09. 7h from 1pm to 8pm - even cancelled my plans to make food because i switched to merging at ~6pm

## Contract List Update and Contract View Modal

Figuring out how to open a modal over the page while styling it and importing primeflex.css (i made a stupid mistake
that took 30min to see).
But the biggest hurdle was mapping from dto to entity that got really messy. A big refactor by yev didn't fix it sadly,
so i had to refactor it again. Such huge changes to the codebase quickly lead to merge issues, i.e. oversights that then
resulted in a not working merged branch.

### Time spend

- 16.09. 3h from 2pm to 5pm
- 16.09. 4h from 5pm to 9pm in our meeting, 50% fixing troubleshooting with yev, 40% fixing the mail certificate,
  talking about authentication realms
- 16.09. 3h from 9:30pm to 0:30am role deletion made big problems with JPA flushes
- 19.09. 8:20h from 6pm to 2:20am, 45min break to eat, else 2h reviewing MR from yev - was a rahter large merge
  request - afterwards continued work on contract list

On the frontend side, i refactored quite a bit again today. i learned about importing enums directly instead of having
functions returns them <.<
I learned a bit more about css styling, but didn't quite figure out where some of that whitespace on the right side of
the dialog originates from.
But i can now open details for every row of the table, not just an example. That was a bit weird to initialize since the
code tried to run even if it didn't show. I learned using the rendered attribute stops this behaviour, but then it
wouldn't open no matter what. Even using the update from the button resulted in dozens of errors before i figured it
out. Place it in its own form and don't update the dialog but the form its in. Or at least when you start with a
colon (:) it might also work with formName.dialogId but i didn't try now.

## Contract Edit/Creation

Restyling Creation as a creation+edit popup over the list.
I spend now long, really really long, to get this done. JPA is really bad.
Finds trigger flushs. So you run into problems is persons are managed entities and contracts and/or roles not.
Part of the problem though was also the logic to update. Deletion of related entities was an issue. I didn't spend long
on but Yev spent several days.
I don't delete stuff right now. I reassign it. And unlink secretaries and assistants. I will need to test if they get
orphan removed. If so i maybe can remove his weird delete sql queries.

While on this journey i also streamlined entity creation/uuid assignment. Refactored equals and hash for Role's and
ContractDTO or so. Need to check those again and maybe apply everywhere.

### Time spend

- 19.09. 10:30 hours from 12am(?) mittags to 10:30pm
- 19.09. 10:30 hours from 11pm to 9:30am yes- im dead. my Rhythm will be fucked D: 21 hours in one day D:
- 20.09. 2 hours from 3pm to 5pm - now with some sleep in, double check changes and commit the mapper rework + review
  min/lindas branches
- 20.09. 1 hours from 5:40pm to 6:40pm - review open MR's, start work on filtering contractList by logged-in user and
  general polishing of its table filters and i18n'izing facesmessage -> eating break now
- 20.09 3 hours from 7:10pm to 10:10pm
    - finishing the i18n, making dateformat i18n, then trying to review new branch of yev
- 20.09. (21. basically D:?) 13h from 11:30pm to 12:30pm working on the dashboard to select contract to enter timesheet
  entries and similarily a contract selection based timesheet list with various interaction buttons
    - especially how to reuse the contract select wheel cause some trouble during setup. jakarta's structure here
      follows a different kind of style. seems you rather don't pass parameters but call the setter/setup function of
      the other bean from your current one
    - annoyingly jakarta isn't quite that good at two-way-databinding, i had for some time an issue where i logged that
      there are two contracts in my list of contracts that should be display but something or other failed... completely
      silently - apparently the call to the setup from inside the supposed parents PostConstruct setup
