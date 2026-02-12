# Chat Noir API

**A modern cat media API with REST + GraphQL support (work in progress..)**

---

## Quick Start

```bash
git clone https://github.com/mousty00/chat-noir-api.git
cd chat-noir-api
./mvnw spring-boot:run
```

**API:** `http://localhost:8080/api`  
**GraphQL:** `http://localhost:8080/api/graphql`

---

## Tech Stack

| Component | Version |
|-----------|---------|
| Java | 25 |
| Spring Boot | 4.0.0 |
| DB | PostgreSQL 16+ |
| API | REST + GraphQL |

---

## Core Features

- **Multi-format media** - GIFs, PNGs, JPEGs
- **Device optimized** - Desktop & Mobile wallpapers
- **Dual API** - REST endpoints + GraphQL queries
- **UUID keys** - Distributed-ready IDs
- **Subscription plans** - Free, Pro, Enterprise
- **2 auth methods** - JWT (users) + API keys (3rd party)

---

## Data Model

```
cat_category ──┬── cat ──┬── cat_media
               │         │
user_role ──┬── user ────┴── user_favorite
            │      │
subscription_plan ─┴── user_api_key
```

---

## REST Endpoints

| Method | Endpoint | Description |
|--------|---------|-------------|
| `GET` | `/api/cats` | List cats (paginated) |
| `GET` | `/api/cats/random` | Random cat |
| `GET` | `/api/cats/{id}` | Get by ID |
| `POST` | `/api/cats` | Create (admin) |
| `PUT` | `/api/cats/{id}` | Update (admin) |
| `DELETE` | `/api/cats/{id}` | Delete (admin) |

**Filters:** `?category=Fluffy&color=black&page=0&size=20`

---

## GraphQL

**Endpoint:** `POST /api/graphql`

```graphql
{
  catById(id: '') {
    id, name, color, category, image, sourceNam
  }
}
```

---

## Error Codes

| Code | Status | Meaning |
|------|--------|---------|
| `CAT_001` | 404 | Cat not found |
| `CAT_002` | 404 | Category not found |
| `AUTH_001` | 401 | Invalid credentials |
| `DB_001` | 409 | Duplicate entry |
| `VAL_001` | 400 | Validation failed |

---

## Subscription Plans

| Plan | Rate/hr | Favorites | GIF | Wallpaper |
|------|---------|-----------|-----|-----------|
| Free | 60 | 20 | ✅ | ❌ |
| Pro | 1,000 | 200 | ✅ | ✅ |
| Enterprise | Custom | ∞ | ✅ | ✅ |

---

## Auth

```
# JWT (users)
Authorization: Bearer <token>
```

```
# API Key
X-API-Key: <key>
```

---

## DB Schema (Core)

```sql
cat (id UUID, name, color, category_id)
cat_media (id UUID, cat_id, media_format, content_url)
cat_category (id UUID, name, media_type_hint)
user (id UUID, username, email, plan_id)
subscription_plan (id UUID, name, monthly_price, api_rate_limit)
```

---

## Prerequisites

- Java 25
- PostgreSQL 16+
- Maven/Gradle


Purr-fect cat media, served fast.
