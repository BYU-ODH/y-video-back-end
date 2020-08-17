DROP TABLE IF EXISTS user_type_exceptions CASCADE;
CREATE TABLE user_type_exceptions (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,deleted TIMESTAMP DEFAULT NULL
   ,updated TIMESTAMP DEFAULT NULL
   ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,username TEXT
   ,account_type INTEGER
);
COMMENT ON TABLE user_type_exceptions IS 'Marks users as lab assistants or admins. Overrides account-type from api on user creation.';
