-- free-exercise-db has at most one equipment value per exercise, drawn from a
-- fixed list, so store it as an enum column instead of a lookup table.
ALTER TABLE exercises
    ADD COLUMN equipment VARCHAR(255),
    ADD COLUMN force VARCHAR(255);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM exercise_equipment GROUP BY exercise_id HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Cannot migrate: some exercises have more than one equipment row';
    END IF;
END $$;

UPDATE exercises e
SET equipment = UPPER(REGEXP_REPLACE(TRIM(eq.name), '[ -]+', '_', 'g'))
FROM exercise_equipment ee
JOIN equipment eq ON eq.id = ee.equipment_id
WHERE ee.exercise_id = e.id;

ALTER TABLE exercises ADD CONSTRAINT exercises_equipment_check CHECK (equipment IN (
    'BODY_ONLY', 'MACHINE', 'CABLE', 'BARBELL', 'DUMBBELL', 'KETTLEBELLS', 'BANDS',
    'MEDICINE_BALL', 'EXERCISE_BALL', 'FOAM_ROLL', 'E_Z_CURL_BAR', 'OTHER'
));

ALTER TABLE exercises ADD CONSTRAINT exercises_force_check CHECK (force IN ('PUSH', 'PULL', 'STATIC'));

DROP TABLE exercise_equipment;
DROP TABLE equipment;
