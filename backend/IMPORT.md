# Exercise import

Set DB_URL, DB_USERNAME, DB_PASSWORD, CLOUDINARY_CLOUD_NAME,
CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET in the process environment.
After building the boot JAR, run:

```sh
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar --exercise-import.enabled=true --server.port=0
```

Normal startup does not import. The command exits with 0 on success, 2 on
partial failure, and 1 on fatal failure or concurrent execution. A PostgreSQL
session advisory lock coordinates instances. Re-run the command to retry missing
images; deterministic public IDs and overwrite=false prevent duplicate assets.
On an empty database, Flyway creates the base schema with V1 and then applies
V2, V3 and V4 automatically at startup. No manual schema import is needed.
Previously applied migrations must remain unchanged. Existing installations with
the original schema but no Flyway history must be baselined at version 1 before
running V2; a baseline records the version and does not create missing tables.
V3 rejects duplicate external identities rather than deleting data. V4 replaces
the equipment and exercise_equipment tables with an enum column
exercises.equipment and adds exercises.force. It fails if any exercise has more
than one equipment row or an equipment name outside the known list.
