-- Create user for Stripe-Lunar application
ALTER SESSION SET CONTAINER = XEPDB1;

-- Create user
CREATE USER stripe_lunar IDENTIFIED BY password;

-- Grant necessary privileges
GRANT CONNECT, RESOURCE, CREATE SESSION, CREATE TABLE, CREATE VIEW, CREATE SEQUENCE TO stripe_lunar;
GRANT UNLIMITED TABLESPACE TO stripe_lunar;

-- Set default tablespace
ALTER USER stripe_lunar DEFAULT TABLESPACE USERS;

-- Create tablespaces if needed
-- CREATE TABLESPACE stripe_lunar_data DATAFILE 'stripe_lunar_data.dbf' SIZE 100M AUTOEXTEND ON;
-- ALTER USER stripe_lunar DEFAULT TABLESPACE stripe_lunar_data;

COMMIT;
