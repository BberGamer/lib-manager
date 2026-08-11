# JSP/Servlet Coding Convention
Priority Order:
1. Reuse existing code.
2. Refactor existing code if necessary.
3. Only write new code when no reusable implementation exists.

## Mandatory instruction loading

Before answering or acting on any request that involves creating, editing,
reviewing, refactoring, debugging, testing, or explaining project code, read
this `AGENTS.md` file completely.

- Do not implement or propose code changes until all applicable project rules
  have been reviewed.
- Search from the repository root to the target file for nested `AGENTS.md`
  files. Read and apply every applicable file; rules closest to the target file
  take precedence when instructions conflict.
- Re-read the relevant rule sections when the task scope changes or additional
  files become involved.
- Before completing a task, verify all changes and recommendations against every
  applicable rule in this file.
- If a user request conflicts with a project rule, clearly identify the conflict
  before proceeding and follow higher-priority system or user instructions.
- In the completion report, briefly confirm that the applicable project rules
  were reviewed and followed. Mention any rule that could not be satisfied and
  explain why.

## 1. Scope and priorities

These rules apply to the entire repository. All new code and modified code must
follow them. When existing code conflicts with these conventions, improve only
the code touched by the current task unless a broader refactor is explicitly
requested.

Preserve the existing NetBeans/Ant layout. Do not migrate the project to Maven,
Gradle, Spring, or another framework unless explicitly requested.

Before implementing a change:

1. Inspect nearby code, `build.xml`, `web.xml`, server configuration, and current
   package names.
2. Determine whether the project uses Java EE (`javax.*`) or Jakarta EE
   (`jakarta.*`). Never mix the two API namespaces.
3. Reuse existing conventions and dependencies when they are compatible with
   these rules.
4. Make the smallest complete change and preserve unrelated user changes.

## 2. Standard project structure

Use the following layout for new code:

```text
src/
|-- conf/
|   `-- MANIFEST.MF
`-- java/
    `-- <base-package>/
        |-- controller/     # HttpServlet request/response orchestration
        |-- dao/            # JDBC persistence, one DAO per entity
        |-- model/          # Domain entities, DTOs, and JavaBeans
        |-- service/        # Business logic and transaction boundaries
        `-- utils/          # Small, stateless, general-purpose helpers
web/
|-- assets/
|   |-- css/
|   |   `-- *.css
|   |-- js/
|   |   `-- *.js
|   `-- images/
|-- META-INF/
|   `-- context.xml
|-- WEB-INF/
|   |-- views/
|   |   |-- fragments/      # Header, footer, navbar, sidebar, pagination
|   |   `-- <module>/       # JSP pages grouped by business module
|   |-- lib/                # JARs only when needed by the Ant project
|   `-- web.xml
`-- index.jsp               # Optional entry point; must remain logic-free
```

- Do not put Java source files under `web/`.
- Do not put JSP files under `src/java/`.
- Backend Java code must be organized only into `controller`, `dao`, `model`,
  `service`, and `utils`; do not create additional top-level backend packages.
- Put servlets, HTTP filters, and application/session listeners in `controller`.
- Put entities, DTOs, enums, and model-specific constants in `model`.
- Put business-specific exceptions with their owning service, and put reusable
  stateless technical helpers or technical constants in `utils`.
- Put protected JSP files under `WEB-INF/views` so they cannot be requested
  directly by URL.
- Access a protected JSP only through a controller using
  `RequestDispatcher.forward()`.
- Organize views and assets by feature when the application becomes large.

## 3. Naming conventions

Use names that express business intent. Avoid vague names such as `Test`,
`Handle`, `Process`, `Data`, `Obj`, `temp`, or `x` outside very small scopes.

| Element | Convention | Example |
|---|---|---|
| Package | lowercase | `com.example.product.controller` |
| Class/interface | PascalCase | `ProductService` |
| Servlet | PascalCase + responsibility | `ProductListServlet` |
| Service | Entity + `Service` | `ProductService` |
| DAO | Entity + `Dao` | `ProductDao` |
| Model/entity | Singular noun | `Product`, `OrderItem` |
| Method/variable | camelCase | `findProductById`, `productId` |
| Boolean | `is`, `has`, `can`, `should` | `isActive`, `hasPermission` |
| Constant | UPPER_SNAKE_CASE | `DEFAULT_PAGE_SIZE` |
| Enum type/value | PascalCase / UPPER_SNAKE_CASE | `OrderStatus.PENDING` |
| JSP | lowercase kebab-case | `product-form.jsp` |
| CSS class | lowercase kebab-case | `product-card` |
| JavaScript file | lowercase kebab-case | `product-form.js` |
| Request attribute | descriptive camelCase | `productList`, `validationErrors` |
| Session attribute | descriptive camelCase | `authenticatedUser` |

Use plural resource names in URLs, for example `/products` and `/orders`.

## 4. MVC architecture and dependency direction

Follow this dependency flow:

```text
Browser -> Controller (Filter/Servlet) -> Service -> DAO -> Database
                              |
                              `-> request attributes -> JSP
```

Dependencies must point inward in that order:

- A controller may depend on a service, but not directly on a DAO.
- A service may depend on one or more DAOs.
- A DAO must not depend on a servlet, JSP, or HTTP session.
- A model must not depend on Servlet/JSP APIs.
- A JSP must not call a service, DAO, database helper, or Java method containing
  business logic.
- Utility classes must not become a dumping ground for business rules.

Keep each class and method focused on one responsibility. Split a class when it
has unrelated reasons to change, not merely because it has many lines.

## 5. Controller and Servlet rules

A servlet is responsible only for HTTP orchestration:

1. Set request/response encoding before reading parameters.
2. Read path, query, or form parameters.
3. Perform basic parsing and request-level validation.
4. Call the appropriate service method.
5. Put view data or validation errors in request attributes.
6. Forward to a JSP or redirect to another URL.

Servlets must not:

- contain SQL or create JDBC connections;
- implement pricing, permission, inventory, or other business rules;
- produce large HTML strings;
- catch `Exception` merely to hide an error;
- use `System.out.println()` for logging;
- duplicate logic that belongs in a service or validator.

Prefer `doGet()` for safe read operations and form display. Use `doPost()` for
state-changing operations. Use `doPut()`/`doDelete()` only when clients and
infrastructure support them consistently.

Set response details explicitly when appropriate:

```java
request.setCharacterEncoding("UTF-8");
response.setContentType("text/html;charset=UTF-8");
```

Use annotations (`@WebServlet`, `@WebFilter`, `@WebListener`) when annotations
are the established project convention. Otherwise update `web.xml`. Do not
define conflicting mappings in both places.

## 6. Service rules

The service layer owns business behavior:

- business validation and invariants;
- authorization decisions that depend on domain data;
- coordination of multiple DAOs;
- transaction boundaries;
- conversion between domain objects and DTOs when required;
- business-specific exceptions and outcomes.

Service method names should describe a use case, such as `createOrder`,
`changePassword`, or `cancelBooking`. Do not leak JDBC classes such as
`ResultSet`, `Connection`, or `SQLException` through the service API. Translate
persistence failures to a meaningful application exception and preserve the
original cause.

## 7. DAO and repository rules

- Each DAO manages persistence for one primary entity or aggregate.
- Put all SQL in DAO classes, never in servlets, services, JSPs, or JavaScript.
- Use `PreparedStatement` for every value supplied at runtime.
- Never build SQL by concatenating request parameters or user-controlled text.
- Use try-with-resources for `Connection`, `PreparedStatement`, and `ResultSet`.
- Prefer a server-managed `DataSource`. Keep credentials and connection strings
  out of source code.
- Map database rows to model objects in a dedicated private mapper when useful.
- Use focused method names such as `findById`, `findAll`, `insert`, `update`, and
  `deleteById`.
- Return an empty collection instead of `null` for a query with no rows.
- Use `Optional<T>` for an optional single result when compatible with the
  project's Java version and existing style.
- Do not silently return a default value when the database operation failed.

Correct parameterized SQL:

```java
String sql = "SELECT id, name FROM products WHERE id = ?";
try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
    statement.setInt(1, productId);
    try (ResultSet resultSet = statement.executeQuery()) {
        // Map the result here.
    }
}
```

For multi-step writes, define the transaction boundary in the service layer.
Commit only after all operations succeed and roll back on failure.

## 8. Model and DTO rules

- Models represent domain data and must remain independent of HTTP concerns.
- Use appropriate Java types: `LocalDate`/`LocalDateTime` for dates and
  `BigDecimal` for money.
- Do not represent numeric IDs, prices, or dates as strings after parsing.
- Keep DTOs separate from entities when form/view data differs materially from
  persisted data.
- Do not put database connections, request objects, or session objects in a
  model.
- Implement `equals()` and `hashCode()` consistently when objects are used in
  sets, maps, or equality comparisons.

## 9. JSP rules

JSP pages are presentation templates only.

- Never use scriptlets, declarations, or Java expressions: `<% ... %>`,
  `<%! ... %>`, or `<%= ... %>`.
- Use Expression Language (EL) for values: `${product.name}`.
- Use JSTL for conditions, loops, URL generation, and formatting.
- Never execute SQL, open connections, instantiate DAOs/services, or perform
  business decisions in JSP.
- Add UTF-8 page directives and HTML metadata.
- Escape untrusted output with `<c:out>` or another context-appropriate encoder.
- Use `${pageContext.request.contextPath}` or `<c:url>` for application URLs.
- Do not expose exception details, SQL messages, credentials, or internal stack
  traces to the browser.

Preferred JSP patterns:

```jsp
<c:url var="productUrl" value="/products" />

<c:if test="${not empty productList}">
    <c:forEach var="product" items="${productList}">
        <c:out value="${product.name}" />
    </c:forEach>
</c:if>
```

Avoid calling arbitrary methods from EL. Prepare display-ready values in the
controller/service where practical.

## 10. Reusable JSP fragments

Extract repeated UI into `WEB-INF/views/fragments`, including:

- header and footer;
- navbar and sidebar;
- pagination;
- flash messages and validation errors;
- common form fields or modal shells when reuse is genuine.

Use clear fragment names such as `header.jsp`, `navbar.jsp`, and
`pagination.jsp`. A fragment should receive all required values through well
named attributes; it must not depend on undocumented session state.

Do not over-fragment one-off markup. Extract a fragment when it is reused or
when it represents a stable, independently understandable UI region.

## 11. CSS and JavaScript separation

JSP files may reference CSS and JavaScript files, but must not contain their
implementation.

Forbidden in JSP/HTML:

- `<style>...</style>` blocks;
- `style="..."` attributes;
- inline `<script>...</script>` code blocks;
- inline event attributes such as `onclick`, `onchange`, `onsubmit`, or
  `onload`;
- `javascript:` URLs.

Required organization:

- Put stylesheets under `web/assets/css`.
- Put scripts under `web/assets/js`.
- Import CSS with `<link rel="stylesheet" href="...">`.
- Import JavaScript with `<script src="..." defer></script>` where appropriate.
- Bind browser events with `addEventListener()`.
- Select elements using stable classes or `data-*` attributes. Use IDs only for
  genuinely unique elements.
- Keep business decisions and server authorization out of client-side scripts.

Example:

```jsp
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/assets/css/product-form.css">
<script src="${pageContext.request.contextPath}/assets/js/product-form.js"
        defer></script>
```

```javascript
const productForm = document.querySelector('[data-product-form]');

if (productForm) {
    productForm.addEventListener('submit', (event) => {
        // Client validation improves UX; server validation remains mandatory.
    });
}
```

## 12. URL and HTTP conventions

Use meaningful, predictable mappings:

```text
GET  /products             list products
GET  /products/view?id=1   show one product
GET  /products/create      show the create form
POST /products/create      create a product
GET  /products/edit?id=1   show the edit form
POST /products/edit        update a product
POST /products/delete      delete a product
```

Prefer clear paths over a single servlet with unclear parameters such as
`/action?do=x`. Keep one convention throughout a module. A state-changing action
must not be implemented as a GET request.

Use the Post/Redirect/Get pattern after successful Create, Update, or Delete:

```java
response.sendRedirect(request.getContextPath() + "/products");
```

Use `forward()` when rendering a JSP in the same request, especially when
preserving entered values and validation errors. Use `redirect()` after a
successful state change or when a new request is intentionally required.

## 13. Validation

Validate input in both places for different purposes:

- Client-side validation provides immediate feedback and improves usability.
- Server-side validation is mandatory and is the security boundary.

For every external value:

1. Check whether it is present when required.
2. Trim text where surrounding whitespace is not meaningful.
3. Check length and allowed format.
4. Parse into the correct type with controlled error handling.
5. Check allowed ranges and business rules.
6. Reject unexpected enum/role/status values.
7. Return field-specific, user-friendly errors without exposing internals.

Do not trust hidden fields, query parameters, cookies, headers, uploaded
filenames, or client-side validation. When validation fails, keep submitted
values in a form DTO or request attributes and forward back to the form.

## 14. Request and session attributes

Use consistent, descriptive names between controllers and JSPs:

```java
request.setAttribute("productList", products);
request.setAttribute("selectedProduct", product);
request.setAttribute("validationErrors", errors);
session.setAttribute("authenticatedUser", userSession);
```

- Define a constant when an attribute name is reused across multiple classes.
- Use request scope for page-specific data.
- Use session scope only for data that must survive multiple requests, such as
  the authenticated user or a short-lived flash message.
- Do not store large lists, DAOs, services, connections, request objects, or
  mutable application-wide data in the session.
- Remove one-time session messages after displaying them.

## 15. Authentication, authorization, and session management

- Store only the minimum authenticated-user representation in the session.
- Never store a password, password hash, access token, or sensitive database
  record unnecessarily in the session.
- Regenerate/change the session ID after successful login when supported.
- Invalidate the session on logout.
- Set an inactivity timeout appropriate for the application.
- Use a filter in the `controller` package to protect route groups and verify
  authentication.
- Check authorization on every protected operation, including direct URL access.
- Do not rely on hidden buttons or menus as authorization controls.
- Verify ownership when a user accesses or changes a record by ID.

## 16. Security requirements

- Hash passwords with an established password-hashing algorithm/library such as
  bcrypt, scrypt, Argon2, or PBKDF2. Never store plain text passwords or invent a
  custom hash scheme.
- Use `PreparedStatement` against SQL injection.
- Escape untrusted output according to HTML, attribute, URL, or JavaScript
  context to prevent XSS.
- Add CSRF protection to state-changing forms and verify the token server-side.
- Validate content type, size, generated filename, and destination for uploads.
- Never trust a client-supplied filesystem path.
- Do not commit secrets, connection passwords, API keys, or machine-specific
  credentials.
- Use secure, HTTP-only, and appropriate SameSite cookie settings in production.
- Return HTTP 400, 401, 403, 404, or 500 as appropriate; do not reveal whether a
  sensitive resource exists to an unauthorized user.

## 17. Constants and enums

Do not repeat literals that represent shared rules or protocol values. Use a
constant or enum for:

- request/session attribute names used in multiple classes;
- role, status, and type values;
- servlet paths reused in Java code;
- pagination defaults and validation limits;
- configuration keys.

Do not create constants for obvious one-off literals that are clearer inline.
Use enums for a closed set of domain values and validate conversions from
external strings.

Never hardcode credentials, host-specific paths, or environment-specific URLs.
Read them from JNDI, environment-backed configuration, or the project's
established configuration mechanism.

## 18. Exception handling

- Catch the most specific useful exception.
- Catch an exception only to recover, add context, translate layers, log at an
  ownership boundary, or produce the correct HTTP response.
- Preserve the original exception as the cause when wrapping it.
- Do not use empty catch blocks.
- Do not return fake success, empty data, or `null` merely because an exception
  occurred.
- Avoid catching and logging the same exception in every layer; log once where
  enough context exists to act on it.
- Configure user-friendly error pages in `web.xml` where appropriate.
- Do not send stack traces or raw exception messages to users.

## 19. Logging

Use the logging framework already configured by the project. If none exists,
prefer `java.util.logging.Logger` rather than adding a dependency without need.

- Do not use `System.out.println()` or `printStackTrace()`.
- Use suitable levels: severe/error for failed operations, warning for abnormal
  recoverable conditions, info for important lifecycle events, and fine/debug
  for diagnostic detail.
- Include useful context such as operation and non-sensitive record ID.
- Use parameterized logging when supported.
- Never log passwords, password hashes, tokens, session IDs, full connection
  strings, payment data, or unnecessary personal data.

## 20. Code formatting and comments

- Use 4 spaces for indentation. Do not use tabs.
- Keep braces consistent and always use braces for control-flow blocks.
- Keep lines around 100-120 characters where practical.
- Put one public top-level class in each Java file.
- Remove unused imports, dead code, commented-out code, and debug output.
- Keep methods small and cohesive; use early returns to reduce deep nesting when
  clarity improves.
- Write all source-code documentation and explanatory comments in Vietnamese.
  Keep technical identifiers, API names, and code symbols in their original
  form so they remain searchable.
- Every newly created source file must start with a short documentation comment
  explaining the file's purpose, its architectural layer, and its main
  responsibility. Do not add author names, creation dates, or boilerplate
  copyright text unless the project explicitly requires them.
- Every class, interface, enum, and servlet must have Vietnamese Javadoc that
  explains what it represents, what responsibility it owns, and how it fits into
  the application. Mention important collaborators when that relationship is
  needed to understand the class.
- Every method, constructor, and JavaScript function must be documented in
  Vietnamese. State what the function does, important preconditions or side
  effects, and the meaning of its result. For Java, use Javadoc and include
  `@param`, `@return`, and `@throws` when applicable.
- Getters, setters, constructors, and overridden framework lifecycle methods
  must still have a concise Vietnamese description; avoid repeating the method
  name without adding meaning.
- At the top of each JSP, add a JSP comment (`<%-- ... --%>`) describing the
  page's purpose, the controller that renders it, and the request/session
  attributes it expects. Do not expose sensitive implementation details in HTML
  comments because HTML comments are sent to the browser.
- At the top of each CSS and JavaScript file, add a Vietnamese block comment
  explaining which page/component it supports and its responsibility.
- Documentation comments describe what a file, class, or function is
  responsible for. Inline comments should explain why a non-obvious decision,
  workaround, algorithm, or constraint exists; do not narrate each obvious line
  of code.
- Keep comments accurate whenever behavior, parameters, return values,
  exceptions, or dependencies change. Delete stale comments rather than leaving
  misleading documentation.
- Do not add documentation comments to generated files or third-party code.
- Keep TODO comments actionable and include enough context to resolve them.

## 21. Mandatory code reuse policy

This policy is mandatory for every implementation task.

Before implementing any new function, method, SQL query, validation rule,
utility, mapping, or business logic:

1. Search the entire relevant project scope for an existing implementation.
   Use symbol names, domain terms, URL paths, SQL table/column names, validation
   messages, and distinctive logic fragments as search terms.
2. Inspect the implementation and all current call sites before deciding whether
   it can be reused or safely extended.
3. Reuse an existing method directly when it already provides the required
   behavior.
4. Extend or generalize the existing implementation when the new requirement is
   a compatible variation.
5. Extract genuinely shared logic to the correct common owner when multiple
   callers need it.

You must not:

- duplicate existing code or copy and paste logic between files;
- create equivalent utility methods under different names;
- repeat the same validation, mapping, SQL query, authorization rule, or
  business rule in multiple layers or modules;
- create a parallel service, DAO, helper, or constant when an established owner
  can be extended;
- leave newly discovered duplication in place merely because it predates the
  current task.

Place reusable logic in the layer that owns the responsibility:

- shared business rules belong in a service or focused domain component;
- shared persistence behavior belongs in the relevant DAO or a focused database
  helper;
- shared request-independent business validation belongs in a service; generic
  stateless validation helpers may belong in `utils`;
- generic stateless technical behavior may belong in `utils`;
- repeated fixed values belong in a focused constants class under `utils`, or
  in an enum under `model` when they represent a closed set of domain values;
- repeated JSP presentation belongs in a reusable JSP fragment;
- repeated CSS and JavaScript behavior belongs in shared asset files or focused
  reusable functions.

When duplicated code is found during implementation, stop adding parallel logic
and refactor the duplication before continuing. Update every affected caller and
verify that behavior remains unchanged. Do not force unrelated concepts into one
abstraction merely because their current code looks similar; reuse must preserve
clear ownership and semantics.

DRY (Don't Repeat Yourself) is a mandatory coding standard for this project.
The completion report must mention the existing implementation that was reused
or state that the project was searched and no reusable implementation was found.

## 22. Git conventions

- Use a short-lived feature branch for each coherent change, for example
  `feature/product-search`, `fix/login-validation`, or `refactor/order-dao`.
- Do not mix unrelated formatting, refactoring, and feature changes in one
  commit.
- Write imperative, meaningful commit subjects, for example:
  `Add server-side validation to product form`.
- Explain the reason and important tradeoffs in the commit body when the change
  is not self-evident.
- Never commit generated build output, IDE-local settings, logs, temporary
  files, secrets, or credentials.
- Do not rewrite, reset, or discard user commits/changes unless explicitly
  requested.
- Codex must not create a branch or commit unless the user asks it to do so.

## 23. Dependency and generated-file discipline

- Do not edit generated output under `build/` or `dist/`.
- Do not edit generated sections of `nbproject/build-impl.xml`.
- Do not add a dependency when the JDK or an existing dependency adequately
  solves the problem.
- If adding a JAR is necessary, document its purpose and ensure it is included in
  the Ant build and deployment artifact.
- Keep Servlet/JSP/JSTL versions compatible with the configured application
  server.

## 24. Completion checklist

Before considering a change complete, verify all applicable items:

1. The project builds with the existing Ant target, preferably `ant clean dist`.
2. Java EE/Jakarta imports match the configured server and are not mixed.
3. Servlet mappings, URLs, form actions, forwards, redirects, and JSP paths agree.
4. Controllers contain no SQL or business logic.
5. JSPs contain no scriptlets, embedded CSS/JavaScript, or inline event handlers.
6. All runtime SQL values use `PreparedStatement` parameters.
7. Required, missing, malformed, boundary, and unauthorized inputs are handled.
8. Create/Update/Delete operations use Post/Redirect/Get after success.
9. Output derived from users is escaped and state-changing forms have CSRF
   protection when the application supports it.
10. JDBC resources and transactions close, commit, and roll back correctly.
11. No secrets, sensitive logs, debug output, dead code, or unrelated formatting
    changes were introduced.
12. The project was searched before adding logic; existing implementations were
    reused or extended, and no avoidable duplication remains.
13. Relevant manual flows or automated tests pass.

If a verification step cannot be run, report exactly what was not verified and
why. Do not claim that a build or test passed unless it was actually executed.
