DROP TABLE IF EXISTS auth_tokens CASCADE;
CREATE TABLE auth_tokens (
    id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
    ,deleted TIMESTAMP DEFAULT NULL
    ,updated TIMESTAMP DEFAULT NULL
    ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ,user_id UUID
);
COMMENT ON TABLE auth_tokens IS 'Contains auth_tokens (stored in id) mapped to user ids';
