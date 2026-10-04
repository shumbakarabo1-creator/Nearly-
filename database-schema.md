# Nearly - Database Architecture & Schema Specification

This document details the production database architecture designed for scaling Nearly from the current V1 MVP (LocalStorage & Room SQLite) to a distributed cloud database (PostgreSQL / Supabase or Firebase Firestore / MongoDB).

---

## 1. Relational Schema (PostgreSQL)

### 1.1 `users` Table
Stores authenticated user accounts and their preferences.

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    phone_number VARCHAR(30),
    avatar_url TEXT,
    default_area VARCHAR(100) DEFAULT 'Rosebank, Johannesburg',
    default_latitude DECIMAL(10, 8),
    default_longitude DECIMAL(11, 8),
    theme_preference VARCHAR(15) DEFAULT 'system' CHECK (theme_preference IN ('system', 'light', 'dark')),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);
```

---

### 1.2 `categories` Table
Hierarchical categorization for businesses, services, and everyday search intent.

```sql
CREATE TABLE categories (
    id VARCHAR(50) PRIMARY KEY, -- e.g. 'electronics', 'printing', 'repairs'
    display_name VARCHAR(100) NOT NULL,
    slug VARCHAR(60) UNIQUE NOT NULL,
    icon_name VARCHAR(50) NOT NULL,
    badge_color VARCHAR(10) NOT NULL,
    parent_category_id VARCHAR(50) REFERENCES categories(id) ON DELETE SET NULL,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_categories_slug ON categories(slug);
```

---

### 1.3 `businesses` Table
Core directory of local shops, artisans, repair centres, and service providers with spatial indexing.

```sql
CREATE TABLE businesses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    category_id VARCHAR(50) NOT NULL REFERENCES categories(id),
    short_description TEXT NOT NULL,
    full_description TEXT,
    address TEXT NOT NULL,
    area VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20),
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    -- PostGIS geospatial column for distance queries:
    location GEOGRAPHY(POINT, 4326),
    phone VARCHAR(30) NOT NULL,
    email VARCHAR(255),
    website_url TEXT,
    opening_hours JSONB NOT NULL, 
    -- Example JSONB: {"mon": "08:30-17:30", "sat": "09:00-14:00", "sun": "closed"}
    is_open_now BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    average_rating DECIMAL(3, 2) DEFAULT 4.5,
    total_reviews INT DEFAULT 0,
    tags TEXT[] NOT NULL DEFAULT '{}',
    services TEXT[] NOT NULL DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Geospatial index for fast radial searches (<1km, <3km, <5km)
CREATE INDEX idx_businesses_location ON businesses USING GIST(location);
CREATE INDEX idx_businesses_area ON businesses(area);
CREATE INDEX idx_businesses_category ON businesses(category_id);
CREATE INDEX idx_businesses_tags ON businesses USING GIN(tags);
```

---

### 1.4 `searches` Table
Analytics and natural language processing log for real user intents.

```sql
CREATE TABLE searches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    session_id VARCHAR(100),
    raw_query TEXT NOT NULL,
    matched_category_id VARCHAR(50) REFERENCES categories(id),
    results_count INT NOT NULL DEFAULT 0,
    search_latitude DECIMAL(10, 8),
    search_longitude DECIMAL(11, 8),
    area VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_searches_query ON searches(raw_query);
CREATE INDEX idx_searches_created_at ON searches(created_at DESC);
```

---

### 1.5 `saved_places` Table
User bookmarks allowing offline caching and quick access.

```sql
CREATE TABLE saved_places (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    note VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id, business_id)
);

CREATE INDEX idx_saved_places_user ON saved_places(user_id);
```

---

### 1.6 `reviews` Table
Community ratings and verified testimonials.

```sql
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    photos TEXT[] DEFAULT '{}',
    is_verified_visit BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(business_id, user_id)
);

CREATE INDEX idx_reviews_business ON reviews(business_id);
```

---

## 2. NoSQL / Document Schema (Firebase Firestore)

For rapid real-time synchronization, the equivalent Firestore collections structure:

```
users/
  {userId}/
    name: "Karabo Shumba"
    email: "shumba@example.com"
    selectedArea: "Rosebank, Johannesburg"
    themeMode: "system"
    savedCount: 3

businesses/
  {businessId}/
    name: "Matrix Warehouse Rosebank"
    category: "Electronics"
    description: "Computer components, phone chargers..."
    address: "Shop LG12, Rosebank Mall, 50 Bath Ave"
    area: "Rosebank, Johannesburg"
    geopoint: GeoPoint(-26.1458, 28.0416)
    phone: "+27 11 880 4321"
    openingHours: "Mon-Sat: 09:00 - 18:00"
    isOpen: true
    rating: 4.7
    reviewCount: 142
    tags: ["phone charger", "type-c", "usb"]
    services: ["Phone charger replacement", "Data cables"]

categories/
  {categoryId}/
    displayName: "Electronics"
    icon: "laptop"
    color: "#2563EB"

users/{userId}/savedPlaces/
  {businessId}/
    savedAt: Timestamp
    businessRef: DocumentReference

searches/
  {searchId}/
    query: "I need a phone charger"
    matchedCategory: "Electronics"
    timestamp: Timestamp
    area: "Rosebank, Johannesburg"
```
