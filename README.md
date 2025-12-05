# Dropbox Adapter for SAP Cloud Platform Integration

A custom adapter for SAP Cloud Platform Integration (CPI) that enables seamless connectivity with Dropbox using Apache Camel 3.14 and the Adapter Development Kit (ADK).

## Overview

This adapter provides comprehensive integration capabilities between SAP CPI and Dropbox, supporting both sender (inbound) and receiver (outbound) scenarios. It implements Dropbox API v2 and supports OAuth2 authentication.

## Features

### Sender Adapter (Inbound from Dropbox)
- **Download File**: Poll and download files from Dropbox with configurable post-processing
- **Download Archive**: Download entire folders as ZIP archives
- **Post-Processing Options**:
  - Delete file after processing
  - Keep file and process again
  - Move file to archive directory
  - Keep file and mark as processed (idempotent repository)
- **Configurable Polling**: Set custom polling intervals
- **Error Handling**: Optional exception raising on post-processing failures

### Receiver Adapter (Outbound to Dropbox)
- **Copy File or Folder**: Copy files/folders within Dropbox
- **Create Folder**: Create new folders in Dropbox
- **Delete File or Folder**: Remove files or folders
- **Get File URL**: Retrieve temporary download links
- **Get Metadata**: Fetch file/folder metadata
- **Get Storage Statistics**: Retrieve account storage information
- **List Folder**: List folder contents with recursive option
- **List Revisions**: Get file revision history
- **Move File or Folder**: Move files/folders within Dropbox
- **Search**: Search for files and folders
- **Upload File**: Upload files with conflict handling
- **Update Metadata**: Modify file/folder properties

## Technical Specifications

- **Apache Camel Version**: 3.14.10
- **ADK Version**: 3.14.4
- **Java Version**: 1.8
- **Dropbox API**: v2
- **Authentication**: OAuth2 Authorization Code

## Project Structure

```
dropbox-adapter/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/sap/cloud/adk/adapter/dropbox/
│   │   │       ├── DropboxComponent.java       # Camel component
│   │   │       ├── DropboxEndpoint.java        # Endpoint configuration
│   │   │       ├── DropboxProducer.java        # Receiver operations
│   │   │       ├── DropboxConsumer.java        # Sender operations
│   │   │       ├── DropboxClient.java          # HTTP client for Dropbox API
│   │   │       └── DropboxConstants.java       # Constants and configurations
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   └── services/org/apache/camel/component/
│   │       │       └── sap-dropbox             # Component descriptor
│   │       └── metadata/
│   │           └── metadata.xml                # UI configuration
│   └── test/
│       └── java/
│           └── com/sap/cloud/adk/adapter/dropbox/
│               └── DropboxComponentTest.java   # Unit tests
├── pom.xml                                      # Maven configuration
└── README.md                                    # This file
```

## Installation

### Prerequisites
- SAP Cloud Platform Integration tenant
- Dropbox account with OAuth2 credentials
- Maven 3.6+
- Java 8+

### Build Instructions

1. Clone the repository:
```bash
git clone https://github.com/mohansuresh/sample-adk-adapter.git
cd sample-adk-adapter
```

2. Build the adapter:
```bash
mvn clean install
```

3. The build will generate a deployable JAR file in the `target` directory.

### Deployment to SAP CPI

1. Navigate to SAP CPI tenant
2. Go to **Monitor** → **Integrations** → **Manage Integration Content**
3. Click **Add** → **Adapter**
4. Upload the generated JAR file
5. Deploy the adapter

## Configuration

### OAuth2 Setup

1. Create a Dropbox App at https://www.dropbox.com/developers/apps
2. Configure OAuth2 redirect URI
3. Obtain Client ID and Client Secret
4. In SAP CPI, navigate to **Monitor** → **Security Material**
5. Create OAuth2 credentials with:
   - **Credential Name**: Your chosen alias (e.g., `dropbox-oauth`)
   - **Grant Type**: Authorization Code
   - **Client ID**: From Dropbox App
   - **Client Secret**: From Dropbox App
   - **Authorization URL**: `https://www.dropbox.com/oauth2/authorize`
   - **Token URL**: `https://api.dropboxapi.com/oauth2/token`

### Sender Adapter Configuration

#### Connection Tab
- **Credential Name**: OAuth2 credential alias from Security Material
- **Timeout**: API call timeout in milliseconds (default: 300000)

#### Processing Tab
- **Operation**: 
  - Download File
  - Download Archive File
- **File Path**: Path to file in Dropbox (for Download File)
- **Folder Path**: Path to folder in Dropbox (for Download Archive)
- **Post-Processing**: Action after successful download
  - Delete File
  - Keep File and Process Again
  - Move File
  - Keep File and Mark as Processed
- **Archive Directory**: Target directory for Move operation
- **Raise Exception on Post-Processing Failure**: Enable/disable exception on failure
- **Persist Duration**: Days to keep files in idempotent repository (default: 90)

#### Scheduler Tab
- **Polling Interval**: Interval between polls in milliseconds (default: 60000)

### Receiver Adapter Configuration

#### Connection Tab
- **Credential Name**: OAuth2 credential alias from Security Material
- **Timeout**: API call timeout in milliseconds (default: 300000)

#### Processing Tab
- **Operation**: Select from 12 available operations
- **Operation-Specific Parameters**: Dynamically shown based on selected operation
- **Handling of Existing Files**: 
  - Auto Rename
  - Fail
  - Ignore
  - Overwrite
  - Dynamic (via header)
- **Response Format**: JSON or XML

## Dynamic Configuration

The adapter supports dynamic configuration through message headers:

### Common Headers
- `SAP_DropboxTimeout`: Override timeout value
- `SAP_DropboxHandling`: Override file handling behavior
- `SAP_DropboxAfterProc`: Override post-processing behavior

### Operation-Specific Headers
- `SAP_DropboxIncludeDeleted`: Include deleted files in metadata
- `SAP_DropboxRecursive`: Enable recursive folder listing
- `SAP_DropboxLimit`: Set result limit
- `SAP_DropboxOrderBy`: Set search order (relevance/modifiedTime)
- `SAP_DropboxMute`: Mute Dropbox notifications

### Post-Processing Response Headers
- `SAP_Dropbox_Archive_Response`: Archive operation result
- `SAP_Dropbox_Delete_Response`: Delete operation result
- `SAP_Dropbox_Keepfile_Response`: Keep file operation result

## Usage Examples

### Example 1: Download and Delete File

**Sender Channel Configuration:**
- Operation: Download File
- File Path: `/documents/invoice.pdf`
- Post-Processing: Delete File
- Polling Interval: 60000 ms

### Example 2: Upload File with Auto-Rename

**Receiver Channel Configuration:**
- Operation: Upload File
- File Path: `/uploads/report.xlsx`
- Handling of Existing Files: Auto Rename
- Response Format: JSON

### Example 3: Search and List Results

**Receiver Channel Configuration:**
- Operation: Search File or Folder
- Search Query: `invoice`
- Search Path: `/documents`
- Max Results: 100
- Order By: Last Modified Time

### Example 4: Copy with Dynamic Handling

**Receiver Channel Configuration:**
- Operation: Copy File or Folder
- Source Path: `/source/file.txt`
- Destination Path: `/backup/file.txt`
- Handling of Existing Files: Dynamic

**Set Header in Integration Flow:**
```xml
<setHeader name="SAP_DropboxHandling">
    <constant>overwrite</constant>
</setHeader>
```

## Error Handling

The adapter provides comprehensive error handling:

1. **Connection Errors**: Timeout and network issues
2. **Authentication Errors**: Invalid or expired OAuth2 tokens
3. **API Errors**: Dropbox API-specific errors (file not found, quota exceeded, etc.)
4. **Post-Processing Errors**: Configurable exception raising

Error messages include:
- HTTP status codes
- Dropbox error responses
- Detailed stack traces in logs

## Logging

The adapter uses SLF4J for logging. Log levels:

- **INFO**: Operation start/completion, file processing
- **DEBUG**: API calls, parameter values
- **TRACE**: Request/response bodies, detailed flow
- **ERROR**: Exceptions and failures

Enable trace logging in SAP CPI for detailed debugging.

## Limitations

1. **File Size**: 
   - Upload: Max 150 MB per file (Dropbox API limitation)
   - Download Archive: Max 20 GB folder size
2. **API Rate Limits**: Subject to Dropbox API rate limits
3. **OAuth2**: Requires manual token refresh if not using SAP CPI OAuth2 management
4. **Idempotent Repository**: In-memory implementation (production should use persistent storage)

## Requirements from Specification

This adapter implements all requirements from the Dropbox Adapter Requirements document:

✅ OAuth2 Authorization Code support  
✅ 2 Sender operations (Download File, Download Archive)  
✅ 12 Receiver operations (Copy, Create Folder, Delete, Get File URL, Get Metadata, Get Storage Stats, List Folder, List Revisions, Move, Search, Upload, Update Metadata)  
✅ Dynamic header and property support  
✅ JSON/XML response format conversion  
✅ Editable dropdowns for operations  
✅ Handling of existing files (Auto Rename, Fail, Ignore, Overwrite)  
✅ Post-processing file handling  
✅ Header propagation control  
✅ MPL behavior configuration  
✅ HTTP session handling  
✅ Tracing support  
✅ Binary content upload support  

## Development

### Building from Source

```bash
mvn clean compile
```

### Running Tests

```bash
mvn test
```

### Creating Distribution

```bash
mvn clean package
```

## Support

For issues, questions, or contributions:
- GitHub Issues: https://github.com/mohansuresh/sample-adk-adapter/issues
- Pull Requests: https://github.com/mohansuresh/sample-adk-adapter/pulls

## License

This project is provided as-is for use with SAP Cloud Platform Integration.

## Version History

### Version 1.0.0 (Initial Release)
- Complete implementation of Dropbox API v2 integration
- Support for 2 sender and 12 receiver operations
- OAuth2 authentication
- Comprehensive error handling and logging
- Dynamic configuration support
- JSON/XML response format conversion

## References

- [Dropbox API v2 Documentation](https://www.dropbox.com/developers/documentation/http/documentation)
- [SAP CPI Adapter Development Kit](https://help.sap.com/docs/CLOUD_INTEGRATION/368c481cd6954bdfa5d0435479fd4eaf/482286e544c24c4b8e0f1f7c2e5c6e6e.html)
- [Apache Camel 3.14 Documentation](https://camel.apache.org/manual/camel-3x-upgrade-guide-3_14.html)

## Contributors

- Initial implementation based on SAP ADK best practices
- Follows SAP CPI adapter development guidelines
- Implements Dropbox API v2 specifications

---

**Note**: This adapter requires proper OAuth2 configuration in SAP CPI Security Material. Ensure you have the necessary Dropbox API credentials before deployment.
