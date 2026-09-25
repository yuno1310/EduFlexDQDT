SELECT format('CREATE ROLE eduflex_monitor LOGIN PASSWORD %L', :'monitor_password')
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'eduflex_monitor')
\gexec

SELECT format('ALTER ROLE eduflex_monitor PASSWORD %L', :'monitor_password')
\gexec

GRANT pg_monitor TO eduflex_monitor;
GRANT CONNECT ON DATABASE :"DBNAME" TO eduflex_monitor;
