CREATE TABLE images (
    id SERIAL PRIMARY KEY,                        
    uuid UUID NOT NULL DEFAULT gen_random_uuid(), 
    meta_data JSONB NOT NULL,                     
    image_url TEXT NOT NULL,               
    label TEXT NOT NULL,                          
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_uuid UNIQUE (uuid)          
);

CREATE INDEX idx_image_url ON images (image_url);
