# 🧷 Requirements

- Git
- JDK 21
- GlassFish Server
- MariaDB + MariaDB Connector/J

# 🔧 Setup

**Everyone** will have to do/configure the following:

## 📥 Clone this repository

`git clone https://gitlab.uni-koblenz.de/bravo/bravo-tss.git`  
You may need to log in
or [generate](https://gitlab.uni-koblenz.de/-/user_settings/personal_access_tokens?name=JakartaEE%202025%20Course&scopes=api%2Cread_user)
a personal access token.  
You likely want to enable both read and write to repositories if you create a personal access token.

## 🐟 GlassFish Configuration

- In your MariaDB server (using cli or GUIs like HeidiSQL, ...)
    - Create a new database `bravo_tss`
- In Glassfish's [admin panel](localhost:4848) under Resources → JDBC
    - Create a JDBC connection pool
        - `bravo-tss-pool` of type "javax.sql.DataSource" and a blank driver vendor (press next)
        - "org.mariadb.jdbc.MariaDbDataSource" as Datasource Classname
        - add the following three properties at the bottom of the page
            - URL `jdbc:mariadb://localhost:3306/bravo_tss`
            - user `APP` (the user you created when setting up all tools)
            - password `APP` (the password you chose for the user you created when setting up all tools)
        - If you don't remember, look at your jakartaee-pool config from that initial setup
    - Create a JDBC resource `jdbc/bravo-tss` with the bravo-tss-pool

- Finish project import/setup for your IDE (see below for hints)

> Note: Names such as context paths, DB names, persistence unit names, etc. **must** be prefixed with `bravo` to avoid
> conflicts with other teams' deployments.

---

## 🛠 IDE Setup

- Both **NetBeans** and **IntelliJ IDEA** are supported.
- Git-supported setup; useful project files are commited; unnecessary files excluded via `.gitignore`.

### 🫘 NetBeans

- Set JDK to 21 for both modules (Properties → Libraries)
- If Glassfish wasn't added yet to the IDE, add it now
- (Optional) Set bravo-tss as the Main Project using Run → Set Main Project → bravo-tss
    - NetBeans will now Run/Build bravo-tss no matter which file/project you have selected right now
- Press Build or Play

#### Note on Hot Reload

- Make sure "Compile on Save" (Ctrl+S) is enabled
- You can "Save files when focus is lost" in Tools → Options → Editor → Autosave

### 🧠 IntelliJ

- Set Project JDK to 21 (Popup or File → Project Structure)
- Set Compiler Output to `/path/to/bravo-tss/out` (in the same menu)
- Install useful/necessary Plugins (File → Settings → Plugins)
    - Glassfish
    - Jakarta EE: Server Faces (JSF)
    - Jakarta EE: Enterprise Java Beans (EJB)
- Create a new Run Configuration of type "Glassfish: Local" (top-right: Current File → Edit Configurations...)
    - Application Server: Glassfish (Add new Server using Configure... → +)
    - Select Server Domain
    - User + Password (might be optional)
    - Press Apply then Run

In case you deployed using NetBeans before you might need to go into the Glassfish Admin Panel → Applications and
undeploy bravo-tss

#### Note on Hot Reload

- Hot Reload works best in Debug mode
    - changes in the output directory get reflected seamlessly and
    - you can apply actual Java Code HotSwaps (new code is injected via debugger, not reflected/changed in the output
      folder)
- Run Configuration — choose an action to use...
    - on "Update" (Ctrl+F10) (you can remove the dialog via checkbox for instant build)
        - Keybind can be changed in Settings → Keymap → Update Running Application
    - for "frame deactivation" aka. when you tab out/select another program
        - this way when you switch to your browser, it will automatically update
