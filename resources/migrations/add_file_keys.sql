DROP TABLE IF EXISTS file_keys CASCADE;
CREATE TABLE file_keys (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,deleted TIMESTAMP DEFAULT NULL
   ,updated TIMESTAMP DEFAULT NULL
   ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,file_id UUID REFERENCES files(id)
   ,user_id UUID REFERENCES users(id)
);
COMMENT ON TABLE file_keys IS 'Volatile keys to represent files for streaming to front end';
