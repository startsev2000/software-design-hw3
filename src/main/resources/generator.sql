CREATE TABLE students (
                          id SERIAL PRIMARY KEY,
                          name VARCHAR(50) NOT NULL
);

INSERT INTO students (name) VALUES
                                ('Elizabeth'),
                                ('Mark'),
                                ('Evgeny'),
                                ('Anastasia'),
                                ('Beaver');

CREATE TABLE grades (
                        id SERIAL PRIMARY KEY,
                        student_name VARCHAR(50) NOT NULL,
                        grade INTEGER NOT NULL
);