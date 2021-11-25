ALTER TABLE files ADD COLUMN aspect_ratio VARCHAR;

INSERT INTO files (id, resource_id, filepath, file_version, metadata, aspect_ratio) VALUES ('00000000-0000-0000-0000-000000000000', '00000000-0000-0000-0000-000000000000', 'empty', 'English', '', '0,0');

ALTER TABLE contents ADD COLUMN file_id UUID DEFAULT '00000000-0000-0000-0000-000000000000' REFERENCES files(id);

CREATE OR REPLACE VIEW files_undeleted AS SELECT * FROM files;

CREATE OR REPLACE VIEW contents_undeleted AS SELECT * FROM contents;