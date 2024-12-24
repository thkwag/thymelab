# ThymeLab Processor

ThymeLab Processor is a development tool that enables designers and developers to create and test Thymeleaf templates without setting up a backend system. It provides real-time hot reload capabilities, making the template development process faster and more efficient.

<p align="center">
   <img src="../../docs/images/thymelab-processor.png" alt="ThymeLab Preview">
</p>

## Key Features

- **No Backend Required**: Create and test Thymeleaf templates without implementing a backend system
- **Real-time Hot Reload**: Instantly see changes in templates, JSON data, and static resources
- **Simple JSON Data Management**: Manage template variables using simple JSON files
- **Layout System Support**: Create reusable layouts and fragments
- **Static Resource Serving**: Serve and test static resources (CSS, JavaScript, images)
- **Designer-Friendly**: Focus on template design without worrying about backend implementation
- **Request Information Access**: Access request information in templates using the `req` variable

## Benefits

- **Rapid Development**: See changes instantly without server restarts
- **Easy Collaboration**: Designers can work on templates independently
- **Simple Setup**: Start creating templates with minimal configuration
- **Flexible Data Management**: Modify template variables through JSON files
- **Production-Ready Templates**: Templates created with ThymeLab can be directly used in production Spring applications

## Getting Started

### Requirements

- Java 17 or higher
- Gradle 8.x

### Configuration

Configure the following settings in the `application.yml` file:

```yaml
watch:
  directory:
    templates: /path/to/resource/templates/     # Template files directory
    thymeleaf-data: /path/to/resource/_thymeleaf/  # JSON data files directory
    static: /path/to/resource/static/           # Static resources directory
```

#### Directory Configuration Rules

- **Default Paths**: If directories are not configured, the following classpath default paths will be used:
  - templates: `classpath:/default/templates/`
  - thymeleaf-data: `classpath:/default/_thymeleaf/`
  - static: `classpath:/default/static/`

- **Absolute Paths**: Paths without the `classpath:` prefix are treated as absolute paths:
  ```yaml
  watch:
    directory:
      templates: /path/to/your/templates
      thymeleaf-data: /path/to/your/json/data
      static: /path/to/your/static/resources
  ```

### Request Information Access
Since Thymeleaf 3.1, direct access to `#request`, `#session`, `#servletContext`, and `#response` objects has been restricted for security reasons ([see Migration Guide](https://www.thymeleaf.org/doc/articles/thymeleaf31whatsnew.html#expression-objects)).

ThymeLab provides a `req` variable through `GlobalControllerAdvice` to address this limitation and provide access to request information in templates. This feature is implemented in the `GlobalControllerAdvice` class:

```java
@ControllerAdvice
public class GlobalControllerAdvice {
    
    @ModelAttribute("req")
    public Map<String, Object> getRequestInfo(HttpServletRequest request) {
        Map<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("requestURI", request.getRequestURI());
        requestInfo.put("requestURL", request.getRequestURL().toString());
        // ... other request properties
        return requestInfo;
    }
}
```

#### Using the req Variable
You can access HttpServletRequest information in templates using the `req` variable:

```html
<!-- Get current URI -->
<div th:text="${req.requestURI}"></div>

<!-- Highlight active menu item -->
<li th:classappend="${req.requestURI == item.url ? 'active' : ''}">

<!-- Get server information -->
<div th:text="${req.serverName}"></div>
```

#### Available req Properties
- `req.requestURI`: Current request URI
- `req.requestURL`: Complete URL (including protocol, host, and port)
- `req.method`: HTTP method (GET, POST, etc.)
- `req.protocol`: Protocol information
- `req.scheme`: URL scheme (http, https)
- `req.serverName`: Server name
- `req.serverPort`: Server port
- `req.contextPath`: Context path
- `req.servletPath`: Servlet path
- `req.pathInfo`: Additional path information
- `req.queryString`: Query string
- `req.remoteAddr`: Client IP address
- `req.remoteHost`: Client host
- `req.remotePort`: Client port
- `req.localAddr`: Local IP address
- `req.localName`: Local host name
- `req.localPort`: Local port

### Project Structure

```
modules/processor/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/github/thkwag/thymelab/processor/
│       │       ├── config/          # Spring configurations
│       │       ├── controller/      # Web controllers
│       │       ├── hotreload/       # Hot reload implementation
│       │       ├── interceptor/     # Web interceptors
│       │       ├── service/         # Business logic services
│       │       └── ThymeLabProcessorApplication.java
│       └── resources/
│           ├── application.yml      # Application configuration
│           └── default/            # Default resources
│               ├── _thymeleaf/     # Default JSON data files
│               ├── static/         # Default static resources
│               └── templates/      # Default Thymeleaf templates
```

## Template Development Workflow

1. **Create Templates**: Write Thymeleaf templates in the templates directory
2. **Define Data**: Create JSON files to provide template variables
3. **View Changes**: Open templates in browser and see changes in real-time
4. **Organize Layouts**: Use layout system for consistent page structure
5. **Reuse Components**: Create reusable fragments for common elements

## Template Examples

### Layout Template
`templates/layout/default.html`
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout">
<head>
    <meta charset="UTF-8">
    <title th:text="${pageTitle}">Title</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
    <header th:replace="~{fragments/header :: header}"></header>
    <main layout:fragment="content"></main>
    <footer th:replace="~{fragments/footer :: footer}"></footer>
</body>
</html>
```

### Fragment Template
`templates/fragments/header.html`
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<header th:fragment="header">
    <nav>
        <div class="logo" th:text="${siteName}">ThymeLab</div>
        <ul class="nav-links">
            <li th:each="item : ${menuItems}" th:classappend="${req.requestURI == item.url ? 'active' : ''}">
                <a th:href="@{${item.url}}" th:text="${item.name}">Menu Item</a>
            </li>
        </ul>
    </nav>
</header>
</html>
```

### Page Template
`templates/pages/about.html`
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layout/default}">
<head>
    <title th:text="${pageTitle}">About</title>
</head>
<body>
    <main layout:fragment="content">
        <div class="about-section">
            <h1 th:text="${title}">About ThymeLab</h1>
            <p th:text="${description}" class="about-description">Description</p>
        </div>
        <div class="features">
            <div th:each="feature : ${features}" class="feature-card">
                <h3 th:text="${feature.title}">Feature Title</h3>
                <p th:text="${feature.description}">Feature Description</p>
            </div>
        </div>
    </main>
</body>
</html>
```

### Global Data
`_thymeleaf/global.json`
```json
{
  "siteName": "ThymeLab",
  "menuItems": [
    {
      "name": "Home",
      "url": "/"
    },
    {
      "name": "About",
      "url": "/pages/about.html"
    }
  ]
}
```

### Page Data
`_thymeleaf/pages/about.json`
```json
{
  "pageTitle": "About - ThymeLab",
  "title": "About ThymeLab",
  "description": "A development tool designed to streamline your Thymeleaf template development process.",
  "features": [
    {
      "title": "Template Preview",
      "description": "Real-time preview of Thymeleaf templates with dynamic data binding and hot reload."
    },
    {
      "title": "Project Structure",
      "description": "Organized project structure with separate directories for templates, fragments, and static resources."
    },
    {
      "title": "Data Binding",
      "description": "Flexible data binding system using JSON files for both global and page-specific template variables."
    },
    {
      "title": "Easy Installation",
      "description": "Simple setup process with the ThymeLab Launcher, no complex configuration required."
    }
  ]
}
```

These examples demonstrate:
- Layout template with title pattern and common structure
- Reusable header fragment with dynamic navigation
- Page template using layout and displaying dynamic content
- Global JSON data for site-wide variables
- Page-specific JSON data for individual page content

## Hot Reload

Hot Reload feature automatically detects changes in:
- Template files (.html)
- JSON data files
- Static resources (CSS, JavaScript, images, etc.)

Changes are instantly reflected in the browser without manual refresh, making the development process faster and more efficient.

## Development Tips

- Use the layout system to maintain consistent page structure
- Create reusable fragments for common components
- Organize JSON data files by feature or page
- Utilize global.json for site-wide variables
- Take advantage of hot reload for rapid iterations

## Build and Run

```bash
# Build
./gradlew :processor:build

# Run
./gradlew :processor:bootRun
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
