DROP TABLE IF EXISTS subtitles CASCADE;
CREATE TABLE subtitles (
                         id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
                         ,deleted TIMESTAMP DEFAULT NULL
                         ,updated TIMESTAMP DEFAULT NULL
                         ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                         ,title TEXT
                         ,language TEXT
                         ,content TEXT
                         ,content_id UUID REFERENCES contents(id))
;
COMMENT ON TABLE subtitles IS 'Contains subtitles to be applied over contents';

DROP TABLE IF EXISTS content_subtitles_assoc CASCADE;
DROP VIEW IF EXISTS subtitles_by_content;
DROP VIEW IF EXISTS cont_res_sub;


DROP VIEW IF EXISTS subtitles_by_resource;
CREATE VIEW subtitles_by_resource AS
    SELECT subtitles_undeleted.*, r.id AS resource_id
    FROM subtitles_undeleted JOIN contents_undeleted
    ON subtitles_undeleted.content_id = contents_undeleted.id
    JOIN resources_undeleted AS r
    ON r.id = contents_undeleted.resource_id;
