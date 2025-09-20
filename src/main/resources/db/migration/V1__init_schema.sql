CREATE TABLE question (
                          id BIGSERIAL PRIMARY KEY,
                          text VARCHAR(255) NOT NULL,
                          active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE expertise (
                           id UUID PRIMARY KEY,
                           car_id VARCHAR(64) NOT NULL,
                           created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_expertise_car_created ON expertise(car_id, created_at DESC);

CREATE TABLE answer (
                        id UUID PRIMARY KEY,
                        expertise_id UUID NOT NULL REFERENCES expertise(id) ON DELETE CASCADE,
                        question_id BIGINT NOT NULL REFERENCES question(id),
                        value BOOLEAN NOT NULL,
                        description TEXT,
                        CONSTRAINT uq_answer_expertise_question UNIQUE (expertise_id, question_id)
);

CREATE TABLE photo (
                       id UUID PRIMARY KEY,
                       answer_id UUID NOT NULL REFERENCES answer(id) ON DELETE CASCADE,
                       url TEXT NOT NULL
);