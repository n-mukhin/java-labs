ALTER TABLE vehicle_owners DROP CONSTRAINT vehicle_owners_vehicle_id_fkey;
ALTER TABLE vehicle_owners DROP CONSTRAINT vehicle_owners_user_id_fkey;

DELETE FROM vehicle_owners;
DELETE FROM vehicles;
DELETE FROM users;

ALTER SEQUENCE users_id_seq RESTART WITH 1;
ALTER SEQUENCE vehicles_id_seq RESTART WITH 1;

ALTER TABLE vehicle_owners
    ADD CONSTRAINT vehicle_owners_vehicle_id_fkey
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE;
ALTER TABLE vehicle_owners
    ADD CONSTRAINT vehicle_owners_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
