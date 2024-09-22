CREATE TYPE coordinates AS (
    x FLOAT,
    y FLOAT
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE vehicles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL CHECK (name <> ''),
    coordinates coordinates NOT NULL,
    creation_date DATE NOT NULL DEFAULT CURRENT_DATE,
    engine_power DOUBLE PRECISION NOT NULL CHECK (engine_power > 0),
    capacity DOUBLE PRECISION CHECK (capacity > 0),
    distance_travelled FLOAT CHECK (distance_travelled > 0),
    fuel_type VARCHAR(50),
    CHECK ((coordinates).x <= 700 AND (coordinates).x >= 0),
    CHECK ((coordinates).y <= 793 AND (coordinates).y >= 0)
);

CREATE TABLE vehicle_owners (
    vehicle_id INT NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ownership_date DATE NOT NULL DEFAULT CURRENT_DATE,
    PRIMARY KEY (vehicle_id, user_id)
);

