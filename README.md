# SharePoint Adapter for SAP Cloud Platform Integration

A custom adapter for connecting SAP Cloud Platform Integration (CPI) to Microsoft SharePoint Online using Apache Camel 3.14 and the SAP Adapter Development Kit (ADK).

## Overview

This adapter enables seamless integration between SAP CPI and Microsoft SharePoint Online, supporting both sender (polling) and receiver operations. It leverages the Microsoft Graph API to interact with SharePoint sites, lists, files, and other resources.

## Features

### Authentication
- **OAuth2 Client Credentials Flow**: Secure authentication using Azure AD application credentials
- **OAuth2 Authorization Code Flow**: Support for user-delegated permissions
- Token management with automatic refresh

### Supported Operations

#### Sender Operations (Polling)
- **Download Files**: Poll and download files from SharePoint document libraries
- **List Files**: Retrieve file listings from SharePoint
- **Get Drive Items**: Fetch drive items with metadata

#### Receiver Operations

**Sites Operations**
- Get Site
- Search Sites
- Get Subsites
- Get Analytics
- Get Lists

**Lists Operations**
- Get List
- Create List
- Get List Items

**List Items Operations**
- Get List Item
- Create List Item
- Update List Item
- Delete List Item
- Get/Update Column Values

**Files Operations**
- Get Drive
- Get Drive Item
- List Children
- Upload File (up to 250MB)
- Update Drive Item
- Delete Drive Item
- Create Folder
- Copy Drive Item
- Move Drive Item
- Search Drive Items

**Permissions Operations**
- List Permissions
- Get Permission
- Create Permission
- Update Permission
- Delete Permission

**Columns Operations**
- List Columns in Site
- List Columns in List

**Content Types Operations**
- List Content Types
- Get Content Type
- Create Content Type
- Update Content Type
- Delete Content Type

### Additional Features
- **Pagination Support**: Handle large result sets efficiently
- **Post-Processing**: Configure file handling after download (Keep, Delete, Mark as Processed)
- **Dynamic Parameters**: Support for dynamic headers and properties
- **Proxy Support**: HTTP and SOCKS proxy configuration
- **Tracing**: Enable detailed logging for troubleshooting
- **Error Handling**: Comprehensive error messages and MPL logging

## Prerequisites

- SAP Cloud Platform Integration tenant
- Apache Camel 3.14.7
- SAP Adapter Development Kit (ADK) 2.3.0
- Java 8 or higher
- Maven 3.6+
- Microsoft 365 subscription with SharePoint Online
- Azure AD application registration

## Azure AD Application Setup

1. **Register an Application in Azure AD**
   - Navigate to Azure Portal > Azure Active Directory > App registrations
   - Click "New registration"
   - Provide a name (e.g., "SAP CPI SharePoint Adapter")
   - Select supported account types
   - Click "Register"

2. **Configure API Permissions**
   - Go to "API permissions"
   - Add permissions for Microsoft Graph:
     - `Sites.Read.All` or `Sites.ReadWrite.All`
     - `Files.Read.All` or `Files.ReadWrite.All`
     - `User.Read` (if using delegated permissions)
   - Grant admin consent

3. **Create Client Secret**
   - Go to "Certificates & secrets"
   - Click "New client secret"
   - Provide a description and expiration period
   - Copy the secret value (you won't be able to see it again)

4. **Note the Following Values**
   - Application (client) ID
   - Directory (tenant) ID
   - Client secret value

## Installation

### Building the Adapter

```bash
# Clone the repository
git clone <repository-url>
cd sharepoint-adapter

# Build the adapter
mvn clean install
```

This will generate the adapter archive in the `target` directory.

### Deploying to SAP CPI

1. Navigate to SAP CPI Web UI
2. Go to "Monitor" > "Integrations"
3. Select "Adapter Development Kit"
4. Click "Add" and upload the generated `.esa` file
5. Deploy the adapter

## Configuration

### Sender Adapter Configuration

**Connection Tab:**
- **Site URL**: SharePoint site URL (e.g., `https://contoso.sharepoint.com/sites/sitename`)
- **Authentication Type**: Select `OAuth2ClientCredentials`
- **Tenant ID**: Your Azure AD tenant ID
- **Client ID**: Application (client) ID from Azure AD
- **Client Secret**: Client secret value from Azure AD

**Processing Tab:**
- **Operation Name**: Select operation (e.g., `DownloadFile`, `ListFiles`)
- **Drive ID**: SharePoint drive identifier
- **Folder Path**: Path to the folder to poll
- **Post Processing**: Action after file download (`Keep`, `Delete`, `MarkProcessed`)

**Scheduler:**
- **Delay**: Polling interval in milliseconds (default: 60000)
- **Initial Delay**: Delay before first poll in milliseconds (default: 1000)

### Receiver Adapter Configuration

**Connection Tab:**
- **Site URL**: SharePoint site URL
- **Authentication Type**: Select `OAuth2ClientCredentials`
- **Tenant ID**: Your Azure AD tenant ID
- **Client ID**: Application (client) ID from Azure AD
- **Client Secret**: Client secret value from Azure AD

**Processing Tab:**
- **Operation**: Select operation category (`Sites`, `Lists`, `Files`, etc.)
- **Operation Name**: Specific operation to perform
- **Response Format**: `JSON` or `XML`

**Resource Settings:**
- **List ID**: SharePoint list identifier (if applicable)
- **Drive ID**: SharePoint drive identifier (if applicable)
- **Item ID**: SharePoint item identifier (if applicable)
- **File Name**: File name for upload operations (if applicable)

**Pagination:**
- **Enable Pagination**: Enable for large result sets
- **Page Size**: Number of items per page (default: 100)

## Usage Examples

### Example 1: Download Files from SharePoint

**Sender Adapter Configuration:**
```
Site URL: https://contoso.sharepoint.com/sites/mysite
Authentication Type: OAuth2ClientCredentials
Tenant ID: <your-tenant-id>
Client ID: <your-client-id>
Client Secret: <your-client-secret>
Operation Name: DownloadFile
Drive ID: <drive-id>
Folder Path: /Documents/Incoming
Post Processing: Delete
Delay: 300000 (5 minutes)
```

### Example 2: Upload File to SharePoint

**Receiver Adapter Configuration:**
```
Site URL: https://contoso.sharepoint.com/sites/mysite
Authentication Type: OAuth2ClientCredentials
Tenant ID: <your-tenant-id>
Client ID: <your-client-id>
Client Secret: <your-client-secret>
Operation: Files
Operation Name: UploadFile
Drive ID: <drive-id>
Item ID: <parent-folder-id>
File Name: ${header.fileName}
```

**Integration Flow:**
```groovy
from("timer:upload?period=60000")
    .setBody(constant("File content here"))
    .setHeader("fileName", constant("test.txt"))
    .to("sap-sharepoint:upload?operation=Files&operationName=UploadFile")
```

### Example 3: Create List Item

**Receiver Adapter Configuration:**
```
Site URL: https://contoso.sharepoint.com/sites/mysite
Operation: ListItems
Operation Name: CreateListItem
List ID: <list-id>
```

**Request Body (JSON):**
```json
{
  "fields": {
    "Title": "New Item",
    "Description": "Item created from SAP CPI"
  }
}
```

## Supported SharePoint Operations

### Sites
- `GetSite` - Get site information
- `SearchSites` - Search for sites
- `GetSubsites` - Get subsites
- `GetAnalytics` - Get site analytics
- `GetLists` - Get lists in site

### Lists
- `GetList` - Get list information
- `CreateList` - Create a new list
- `GetListItems` - Get items in a list

### List Items
- `GetListItem` - Get a specific list item
- `CreateListItem` - Create a new list item
- `UpdateListItem` - Update an existing list item
- `DeleteListItem` - Delete a list item
- `GetColumnValues` - Get column values
- `UpdateColumnValues` - Update column values

### Files
- `GetDrive` - Get drive information
- `GetDriveItem` - Get drive item details
- `ListChildren` - List children of a folder
- `UploadFile` - Upload a file
- `UpdateDriveItem` - Update drive item metadata
- `DeleteDriveItem` - Delete a drive item
- `CreateFolder` - Create a new folder
- `CopyDriveItem` - Copy a drive item
- `MoveDriveItem` - Move a drive item
- `SearchDriveItems` - Search for drive items

## Error Handling

The adapter provides comprehensive error handling with meaningful error messages:

- **Authentication Errors**: Clear messages for OAuth2 token acquisition failures
- **API Errors**: Detailed HTTP status codes and response bodies
- **Configuration Errors**: Validation of required parameters at design time
- **Runtime Errors**: Proper exception handling with MPL logging

## Limitations

- Maximum file upload size: 250MB (per Microsoft Graph API limits)
- Pagination is required for result sets larger than 5000 items
- Rate limiting applies as per Microsoft Graph API throttling policies
- On-premises SharePoint is not supported (SharePoint Online only)

## Troubleshooting

### Enable Tracing

Set `traceEnabled=true` in the adapter configuration to enable detailed logging.

### Common Issues

**Authentication Failed**
- Verify Azure AD application credentials
- Ensure API permissions are granted and admin consent is provided
- Check that the tenant ID is correct

**Operation Not Found**
- Verify the operation name matches the supported operations list
- Check that required parameters (List ID, Drive ID, etc.) are provided

**File Upload Failed**
- Ensure file size is within limits (250MB)
- Verify drive ID and parent folder ID are correct
- Check that the application has write permissions

## Development

### Project Structure

```
sharepoint-adapter/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/sap/adapter/adk/sharepoint/
│   │   │       ├── SharePointComponent.java
│   │   │       ├── SharePointEndpoint.java
│   │   │       ├── SharePointProducer.java
│   │   │       ├── SharePointConsumer.java
│   │   │       ├── auth/
│   │   │       │   └── OAuth2AuthenticationHandler.java
│   │   │       ├── handler/
│   │   │       │   └── SharePointHttpClient.java
│   │   │       └── exception/
│   │   │           ├── SharePointException.java
│   │   │           └── SharePointAuthenticationException.java
│   │   └── resources/
│   │       ├── metadata/
│   │       │   └── metadata.xml
│   │       └── META-INF/services/org/apache/camel/component/
│   │           └── sap-sharepoint
│   └── test/
│       └── java/
├── pom.xml
└── README.md
```

### Building from Source

```bash
mvn clean install
```

### Running Tests

```bash
mvn test
```

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the Apache License 2.0.

## Support

For issues and questions:
- Create an issue in the GitHub repository
- Contact SAP support for CPI-related questions

## References

- [Microsoft Graph API Documentation](https://docs.microsoft.com/en-us/graph/api/overview)
- [SharePoint REST API Reference](https://docs.microsoft.com/en-us/sharepoint/dev/sp-add-ins/get-to-know-the-sharepoint-rest-service)
- [SAP CPI Documentation](https://help.sap.com/docs/CLOUD_INTEGRATION)
- [Apache Camel Documentation](https://camel.apache.org/manual/latest/)

## Version History

### 1.0.0 (Initial Release)
- OAuth2 authentication support
- Sender and receiver adapters
- Support for Sites, Lists, Files, and Permissions operations
- Pagination support
- Post-processing for sender adapter
- Comprehensive error handling

## Authors

- SAP CPI Development Team

## Acknowledgments

- Microsoft Graph API team for comprehensive API documentation
- SAP ADK team for the Adapter Development Kit
- Apache Camel community
