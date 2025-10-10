# Urban MCP Adapter

A Spring Boot application that provides Model Context Protocol (MCP) tools for interacting with Urban Local Governance systems, specifically designed for Public Grievance Redressal (PGR) services.

## Overview

The Urban MCP Adapter is a bridge between AI applications and Urban Local Governance systems, enabling AI agents to interact with citizen complaint management systems. It provides tools for searching complaints, fetching master data, managing tenants, and creating new complaints through a standardized MCP interface.

## Features

### Core Functionality

- **Complaint Management**: Search and retrieve citizen complaints by date range or complaint number
- **Tenant Management**: Fetch city-level tenants and localities for complaint filing
- **Master Data Access**: Retrieve complaint categories and service definitions
- **Complaint Creation**: Create new citizen complaints with proper tenant and locality mapping
- **Authentication**: Integrated token-based authentication with Urban Governance systems

### Available Tools

1. **`fetch_local_tenants`** - Get list of local tenants for search functionality
2. **`search_complaints_with_dates`** - Search complaints within a date range with pagination
3. **`search_complaint_by_number`** - Find specific complaints by complaint number
4. **`list_complaint_categories`** - Fetch master data of complaint types and subtypes
5. **`fetch_city_tenants`** - Get city-level tenants for complaint filing
6. **`fetch_address_locality`** - Retrieve localities for a given city tenant
7. **`create_citizen_complaint_with_code`** - Create new citizen complaints

## Technology Stack

- **Java 17** - Primary programming language
- **Spring Boot 3.5.5** - Application framework
- **Spring AI 1.0.1** - MCP server integration
- **Spring WebFlux** - Reactive HTTP client
- **Jackson** - JSON processing
- **Lombok** - Code generation
- **Maven** - Build tool

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Access to Urban Governance demo environment (`https://unified-demo.digit.org`)
- Local PGR services running on `http://localhost:8090` (optional)

## Installation

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd urban-mcp-adapter
   ```

2. **Build the application**:
   ```bash
   ./mvnw clean package
   ```

3. **Run the application**:
   ```bash
   ./mvnw spring-boot:run
   ```

## Configuration

The application uses the following configuration files:

### `application.properties`
```properties
spring.application.name=urban-mcp-adapter
spring.main.web-application-type=none
spring.ai.mcp.server.name=Urban-Mcp-Adapter-Demo
spring.ai.mcp.server.version=0.0.1
spring.main.banner-mode=off
spring.main.log-startup-info=false
```

### Authentication
The application uses hardcoded credentials for demo purposes:
- **Username**
- **Password**
- **Tenant**
- **User Type**

## Usage

### MCP Server Integration

The application runs as an MCP server that can be integrated with AI applications supporting the Model Context Protocol. Once running, it exposes the following tools:

### Example Tool Usage

1. **Fetch Available Cities**:
   ```json
   {
     "tool": "fetch_city_tenants",
     "arguments": {}
   }
   ```

2. **Search Complaints**:
   ```json
   {
     "tool": "search_complaints_with_dates",
     "arguments": {
       "fromDate": "2024-01-01",
       "toDate": "2024-01-31",
       "tenantId": "pg.citya",
       "offset": "0",
       "limit": "10"
     }
   }
   ```

3. **Create Complaint**:
   ```json
   {
     "tool": "create_citizen_complaint_with_code",
     "arguments": {
       "tenantId": "pg.citya",
       "serviceCode": "PGR.001",
       "localityCode": "LOC001"
     }
   }
   ```

## API Endpoints

The adapter integrates with the following Urban Governance APIs:

- **PGR Services**: `/pgr-services/v2/request/_search`, `/pgr-services/v2/request/_create`
- **MDMS Services**: `/egov-mdms-service/v1/_search`
- **Location Services**: `/egov-location/location/v11/boundarys/_search`
- **Authentication**: `/user/oauth/token`

## Project Structure

```
src/main/java/org/egov/lg/mcp/
├── ccrs/
│   ├── models/           # Data models for API responses
│   ├── utils/            # Utility classes (TokenService, ApiClient)
│   ├── UrbanMCPApp.java  # Main application class
│   ├── UrbanMCPService.java  # Core service with MCP tools
│   └── UrbanMCPConfiguration.java  # Configuration class
```

## Development

### Building from Source

```bash
# Clean and compile
./mvnw clean compile

# Run tests
./mvnw test

# Package application
./mvnw package

# Run with Maven
./mvnw spring-boot:run
```

### Adding New Tools

To add new MCP tools:

1. Add a new method to `UrbanMCPService.java`
2. Annotate with `@Tool(name="tool_name", description="...")`
3. Implement the business logic
4. Add corresponding data models if needed

## Logging

The application uses Logback for logging with the following configuration:
- All logs are routed to STDERR to keep STDOUT clean for MCP JSON-RPC
- Root log level: WARN
- Application log level: INFO

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For issues and questions:
- Create an issue in the repository
- Check the existing documentation
- Review the Spring AI MCP documentation

## Related Projects

- [Spring AI MCP Server](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)
- [Urban Governance Platform](https://unified-demo.digit.org)
- [Model Context Protocol](https://modelcontextprotocol.io/)

## Changelog

### Version 0.0.1-SNAPSHOT
- Initial release with basic PGR functionality
- MCP server integration
- Authentication with Urban Governance systems
- Core complaint management tools

