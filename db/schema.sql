CREATE SEQUENCE brands_id_seq;
CREATE SEQUENCE products_id_seq;

CREATE TABLE brands (
    id INTEGER PRIMARY KEY DEFAULT nextval('brands_id_seq'),
    name VARCHAR(255),
    description TEXT,
    logo_url TEXT,
    official_website_url TEXT
);

CREATE TABLE cosing_substances (
    id INTEGER PRIMARY KEY,
    inci_name TEXT,
    description TEXT,
    cas_number TEXT,
    ec_number TEXT,
    identified_ingredients TEXT,
    cosmetics_regulation_provisions TEXT,
    functions TEXT,
    sccs_opinions TEXT,
    url TEXT
);

CREATE TABLE products (
    id INTEGER PRIMARY KEY DEFAULT nextval('products_id_seq'),
    name VARCHAR(500),
    brand_id INTEGER REFERENCES brands(id),
    description TEXT,
    image_url TEXT,
    product_page_url TEXT
);

CREATE TABLE product_ingredients (
    id SERIAL PRIMARY KEY,
    product_id INTEGER NOT NULL REFERENCES products(id),
    ingredient_id INTEGER NOT NULL REFERENCES cosing_substances(id),
    position INTEGER NOT NULL,
    concentration VARCHAR(100),
    UNIQUE (product_id, position)
);

CREATE TABLE product_similarities (
    product_id INTEGER REFERENCES products(id),
    similar_product_id INTEGER REFERENCES products(id),
    number_of_matches INTEGER NOT NULL,
    is_golden_match BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (product_id, similar_product_id)
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    api_key VARCHAR(36) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    active BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE product_submissions (
    id BIGSERIAL PRIMARY KEY,
    product_name VARCHAR(500) NOT NULL,
    normalized_name VARCHAR(500) NOT NULL,
    brand_name VARCHAR(255) NOT NULL,
    normalized_brand VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    submitted_at TIMESTAMP NOT NULL DEFAULT now(),
    ingredient_fingerprint TEXT NOT NULL,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE submission_ingredients (
    id BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL REFERENCES product_submissions(id) ON DELETE CASCADE,
    cosing_id INTEGER NOT NULL REFERENCES cosing_substances(id),
    raw_inci_name VARCHAR(500),
    position INTEGER NOT NULL
);

CREATE TABLE change_proposals (
    id BIGSERIAL PRIMARY KEY,
    product_id INTEGER NOT NULL REFERENCES products(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    submitted_at TIMESTAMP NOT NULL DEFAULT now(),
    ingredient_fingerprint TEXT NOT NULL,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE proposal_ingredients (
    id BIGSERIAL PRIMARY KEY,
    proposal_id BIGINT NOT NULL REFERENCES change_proposals(id) ON DELETE CASCADE,
    cosing_id INTEGER NOT NULL REFERENCES cosing_substances(id),
    raw_inci_name VARCHAR(500),
    position INTEGER NOT NULL
);

CREATE INDEX idx_products_name ON products USING gin(to_tsvector('english', name));
CREATE INDEX idx_product_ingredients_product_id ON product_ingredients(product_id);
CREATE INDEX idx_product_ingredients_ingredient_id ON product_ingredients(ingredient_id);
CREATE INDEX idx_product_ingredients_position ON product_ingredients(product_id, position);
CREATE INDEX idx_product_similarities_product_id ON product_similarities(product_id, number_of_matches DESC);
CREATE INDEX idx_product_similarities_golden ON product_similarities(product_id) WHERE is_golden_match = TRUE;
CREATE INDEX idx_users_api_key ON users(api_key);
CREATE INDEX idx_submissions_group ON product_submissions(normalized_name, normalized_brand, ingredient_fingerprint);
CREATE INDEX idx_submissions_submitted_at ON product_submissions(submitted_at);
CREATE INDEX idx_proposals_group ON change_proposals(product_id, ingredient_fingerprint);
CREATE INDEX idx_proposals_submitted_at ON change_proposals(submitted_at);
CREATE INDEX idx_submission_ingredients_submission ON submission_ingredients(submission_id);
CREATE INDEX idx_proposal_ingredients_proposal ON proposal_ingredients(proposal_id);
