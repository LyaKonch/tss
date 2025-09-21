- ## Contribution
- Set up entity structure, DAO/DTO objects structure and logic,  moved respecting logic to business logic beans;

- added library related to holiday calculations and used it's parser for reading configuration files specific to every Land in Germany; added land support i guess 

- added core for sending emails and reminder accordingly;
- added tasks scheduler to delete outdated timesheets, depending on archivationDuration factor looking at singedByEmployee date;
- implemented authentication checks everywhere in business methods that involves retrieving user data. Checks for being in certain role in respect for contract also. A bit of same checks in jsf rendering.
- set up jdbc realm and required entities for that;/javamailSession resource required for sending emails as well
- realised logging mechanism and userLogic
- created "Admin Page" for creating/editing/viewing persons and their group roles;
- did the backbone for dashboard page

## Time Effort
Approximately: 80 hours