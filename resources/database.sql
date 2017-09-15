CREATE TABLE poll (
 id serial primary key,
 title text,
 description text,
 "expires-at" timestamp
);

CREATE TABLE option (
 id serial primary key,
 "poll-id" bigint references poll (id),
 option text
);

CREATE TABLE answer (
  "poll-id" bigint references poll (id),
  "option-id" bigint references option (id),
  created timestamp default current_timestamp,
  comment text
);

-- Type for a result showing the option, how many votes it has and the percentage
-- of total votes.
CREATE TYPE result AS ( option text, votes bigint, percentage DECIMAL(4,1) );


CREATE VIEW "poll-results" AS
SELECT p.id, p.title, p.description, r.results
  FROM poll p
       JOIN (SELECT p.id,
                    array_agg(ROW(o.option,
                                 (SELECT COUNT(*) FROM answer a WHERE a."option-id" = o.id),
                                 (100.0 *
                                  (SELECT COUNT(*) FROM answer a WHERE a."option-id" = o.id) /
                                  (SELECT COUNT(*) FROM answer a WHERE a."poll-id" = o."poll-id"))
                                  )::result) AS results
               FROM poll p
                    JOIN option o ON o."poll-id" = p.id
              GROUP BY p.id) r ON r.id=p.id;


--- test data

INSERT INTO poll (title) values ('What part looks like pacman?');
INSERT INTO option ("poll-id", option) values (1, 'Looks like pacman'), (1, 'Does not look like pacman');
INSERT INTO answer ("poll-id", "option-id") VALUES (1, 1), (1,1), (1,1), (1,2);
