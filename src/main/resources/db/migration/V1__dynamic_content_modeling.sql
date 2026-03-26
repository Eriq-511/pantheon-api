-- Flyway migration for dynamic content modeling
-- Creates content_types, content_fields, and content_entries tables

CREATE TABLE content_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE content_fields (
    id SERIAL PRIMARY KEY,
    content_type_id INTEGER NOT NULL REFERENCES content_types(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    type VARCHAR(32) NOT NULL, -- text, rich_text, number, boolean, date, media, relation, enum, json
    required BOOLEAN DEFAULT FALSE,
    enum_values TEXT, -- comma-separated for enum
    field_order INTEGER DEFAULT 0,
    settings JSONB,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE content_entries (
    id SERIAL PRIMARY KEY,
    content_type_id INTEGER NOT NULL REFERENCES content_types(id) ON DELETE CASCADE,
    data JSONB NOT NULL, -- dynamic fields stored as JSON
    status VARCHAR(32) DEFAULT 'draft',
    published_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_content_entries_type ON content_entries(content_type_id);
