-- Healthcheck script for Oracle database
-- Used by Docker healthcheck to verify database is running properly

-- Exit with success code if database is accessible
WHENEVER SQLERROR EXIT FAILURE
WHENEVER OSERROR EXIT FAILURE

SELECT 'Database is healthy' FROM dual;

EXIT SUCCESS;
