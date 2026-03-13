# Pantheon API Integration Guide

Base URL: `https://pantheon-api-22ig.onrender.com`

## Health Check
- **GET /**
  - Returns: `Pantheon API is running.`
  - Use to verify backend is up.

- **GET /api**
  - Returns: `{ "status": "Pantheon API is running." }`
  - Use for frontend integration check (requires authentication unless security config is updated).

## Authentication
- **POST /api/auth/register**
  - Body: `{ "username": string, "password": string, ... }`
  - Registers a new user.

- **POST /api/auth/login**
  - Body: `{ "username": string, "password": string }`
  - Returns: JWT token on success.

- **POST /api/auth/logout**
  - Logs out the user (token-based).

- **GET /api/auth/me**
  - Returns: Current user info (requires JWT in Authorization header).

## Pages
- **GET /api/pages**
  - Returns: List of pages (public).

- **GET /api/pages/id/{id}**
  - Returns: Page by ID (public).

- **GET /api/pages/{slug}**
  - Returns: Page by slug (public).

- **POST /api/pages**
  - Create a new page (requires authentication).

- **PUT /api/pages/{id}**
  - Update a page (requires authentication).

- **DELETE /api/pages/{id}**
  - Delete a page (requires authentication).

## Menu
- **GET /api/menu**
  - Returns: Menu items (public).

- **POST /api/menu**
  - Create a menu item (requires authentication).

- **PUT /api/menu/reorder**
  - Reorder menu items (requires authentication).

- **PUT /api/menu/{id}**
  - Update menu item (requires authentication).

- **DELETE /api/menu/{id}**
  - Delete menu item (requires authentication).

## Settings
- **GET /api/settings**
  - Returns: Settings (public).

## Products
- **GET /api/products**
  - Returns: List of products (public).

- **GET /api/products/{id}**
  - Returns: Product by ID (public).

- **POST /api/products**
  - Create a product (requires authentication).

- **PUT /api/products/{id}**
  - Update product (requires authentication).

- **DELETE /api/products/{id}**
  - Delete product (requires authentication).

## Images
- **GET /api/images**
  - Returns: List of images (requires authentication).

- **POST /api/images/upload**
  - Upload an image (requires authentication).

- **PUT /api/images/{id}**
  - Update image (requires authentication).

- **DELETE /api/images/{id}**
  - Delete image (requires authentication).

## Site Analysis
- **POST /api/site-analysis**
  - Analyze site data (requires authentication).

---

### Authentication
- Most endpoints require a JWT token in the `Authorization: Bearer <token>` header.
- Public endpoints are marked above.

### Error Handling
- 401 Unauthorized: Returned if JWT is missing or invalid for protected endpoints.
- 404 Not Found: Returned if resource does not exist.
- 400 Bad Request: Returned for invalid input.

---

For further details, see the backend source code or contact the maintainer.
