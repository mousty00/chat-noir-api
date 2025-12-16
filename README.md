#Cat API - Chat NoirA simple yet powerful and modern API designed to serve high-quality cat media. The API supports dynamic content types (Images, GIFs, and Cartoons) and provides media files optimized for different device formats (Desktop and Phone Wallpapers).

##🚀 Technology StackThis project leverages the cutting-edge features of the latest Java and Spring Boot ecosystem.

| Component | Version | Notes |
| --- | --- | --- |
| **Java** | **25** | Utilizes the latest language features (e.g., Records, Pattern Matching). |
| **Spring Boot** | **4.0.0** |  |
| **Framework** | Spring Web MVC, Spring Data JPA |  |
| **Database** | PostgreSQL | Uses native `UUID` types for primary keys. |

##✨ Core Features* **Declarative HTTP Clients:** External API integrations are handled using Spring's declarative HTTP Interfaces (`@HttpExchange`), eliminating boilerplate code.
* **Multi-Format Media:** Each cat entry can provide multiple media files optimized for different use cases.
* **Category-Based Content:** Media style is grouped by category (`Fluffy`, `Cartoon`, `Sleek`).
* **UUID Primary Keys:** All core entities use UUIDs for modern, distributed, and secure identification.
* **User Favorites:** Support for authenticated users to manage their preferred cat media.

##💾 Data Model OverviewThe database schema (PostgreSQL) is designed for flexibility and clean data separation:

* **`cat_category`**: Defines the group (e.g., 'Cartoon') and the `media_type_hint` (e.g., 'cartoon').
* **`cat`**: The abstract concept of a cat (e.g., 'Garfield'), linked to a category. Includes the `source_name` for attribution.
* **`cat_media_file`**: Stores the actual media URLs. Crucially, it includes the `media_format` ('Desktop', 'Mobile', 'Original') and resolution details.
* **`user` & `user_favorite**`: Standard user model and a join table for saving favorite media files.

##🛠️ Getting Started###Prerequisites* Java 25 JDK
* Maven or Gradle
* PostgreSQL instance

###Running Locally. **Clone the Repository:**
```bash
git clone https://github.com/mousty00/chat_noir_api.git
cd chat_noir_api

```


2. **Configure Database:** Update `application.yml` or `application.properties` with PostgreSQL credentials.
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/catdb
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update

```


3. **Build and Run:**
```bash
./mvnw clean install
./mvnw spring-boot:run

```



The application will start on `http://localhost:8080`.

##🌍 API Endpoints

###All cats.

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/v1/cats` | Returns a list of cats paginated. |

###Random Cat RetrievalGet a single random cat.

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/v1/cats/random` | Returns a random Cat with its associated media files. |

###Filter by Category and FormatRetrieve a list of cats filtered by their category and the required media format (e.g., a phone wallpaper).

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/v1/cats/category/{category}` | Returns all cats in a specific category (e.g., `fluffy`). |
| `GET` | `/api/v1/cats/category/cartoon?format=Desktop` | Returns cartoon cats, specifically providing the desktop wallpaper link. |


