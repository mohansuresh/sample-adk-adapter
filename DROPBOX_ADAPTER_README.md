# Dropbox Custom Adapter for SAP CPI Integration Suite

## Overview

This custom adapter enables seamless integration between SAP Cloud Platform Integration (CPI) and Dropbox using Apache Camel 3.14.7. The adapter supports both sender (inbound) and receiver (outbound) scenarios with comprehensive file operations.

## Version Information

- **Adapter Version**: 1.0.0
- **Apache Camel Version**: 3.14.7
- **ADK Version**: 2.3.0
- **Vendor**: SAP

## Features

### Sender Adapter (Inbound Operations)
- **Download File**: Download individual files from Dropbox
- **Download Archive**: Download entire folders as ZIP archives
- **Post-Processing Options**:
  - Delete: Remove file after successful processing
  - Archive: Move file to archive directory
  - Keep and Process: Process file on every poll
  - Keep and Mark: Mark file as processed (idempotent)
- **Scheduler Support**: Configurable polling intervals

### Receiver Adapter (Outbound Operations)
- **Upload**: Upload files to Dropbox
- **Copy**: Copy files or folders
- **Move**: Move files or folders
- **Delete**: Delete files or folders
- **Create Folder**: Create new folders
- **List Folder**: List folder contents
- **Search**: Search for files and folders
- **Get Metadata**: Retrieve file/folder metadata
- **Get File URL**: Get temporary download links
- **Get Storage Statistics**: Retrieve account storage information
- **List Revisions**: List file revision history
- **Update Metadata**: Update file properties

## Architecture

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         SAP Cloud Platform Integration                       │
│                                                                               │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                        Integration Flow                              │   │
│  │                                                                       │   │
│  │  ┌──────────────┐         ┌──────────────┐         ┌─────────────┐ │   │
│  │  │   Sender     │────────▶│   Content    │────────▶│  Receiver   │ │   │
│  │  │   Adapter    │         │   Modifier   │         │   Adapter   │ │   │
│  │  │  (Inbound)   │         │              │         │  (Outbound) │ │   │
│  │  └──────┬───────┘         └──────────────┘         └──────┬──────┘ │   │
│  │         │                                                   │        │   │
│  └─────────┼───────────────────────────────────────────────────┼────────┘   │
│            │                                                   │            │
│            │                                                   │            │
│  ┌─────────▼───────────────────────────────────────────────────▼────────┐   │
│  │                    Dropbox Custom Adapter (ADK)                      │   │
│  │                                                                       │   │
│  │  ┌─────────────────────────────────────────────────────────────┐    │   │
│  │  │              Apache Camel Component Layer                   │    │   │
│  │  │                                                               │    │   │
│  │  │  ┌──────────────────┐         ┌──────────────────┐          │    │   │
│  │  │  │ DropboxComponent │────────▶│ DropboxEndpoint  │          │    │   │
│  │  │  │                  │         │                  │          │    │   │
│  │  │  │ - createEndpoint │         │ - Configuration  │          │    │   │
│  │  │  │ - setProperties  │         │ - Parameters     │          │    │   │
│  │  │  └──────────────────┘         └────────┬─────────┘          │    │   │
│  │  │                                        │                     │    │   │
│  │  │                          ┌─────────────┴─────────────┐      │    │   │
│  │  │                          │                           │      │    │   │
│  │  │                ┌─────────▼─────────┐   ┌────────────▼──────┐    │   │
│  │  │                │ DropboxConsumer   │   │  DropboxProducer  │    │   │
│  │  │                │   (Sender Mode)   │   │  (Receiver Mode)  │    │   │
│  │  │                │                   │   │                   │    │   │
│  │  │                │ - poll()          │   │ - process()       │    │   │
│  │  │                │ - download files  │   │ - 14 operations   │    │   │
│  │  │                │ - post-process    │   │ - dynamic config  │    │   │
│  │  │                │ - scheduler       │   │ - header support  │    │   │
│  │  │                └─────────┬─────────┘   └────────────┬──────┘    │   │
│  │  │                          │                          │           │    │
│  │  └──────────────────────────┼──────────────────────────┼───────────┘    │
│  │                             │                          │                │
│  │                             └──────────┬───────────────┘                │
│  │                                        │                                │
│  │  ┌─────────────────────────────────────▼──────────────────────────┐    │
│  │  │                    DropboxClient (HTTP Layer)                  │    │
│  │  │                                                                 │    │
│  │  │  - OAuth2 Authentication (via SAP Secure Store)                │    │
│  │  │  - executeApiCall() - Regular API operations                   │    │
│  │  │  - executeContentApiCall() - Content upload/download           │    │
│  │  │  - Error handling & logging                                    │    │
│  │  │  - Apache HttpClient 4.5.13                                    │    │
│  │  └─────────────────────────────────────┬───────────────────────────┘    │
│  │                                        │                                │
│  └────────────────────────────────────────┼────────────────────────────────┘
│                                           │                                 │
│  ┌────────────────────────────────────────▼────────────────────────────┐   │
│  │                    SAP Security Material                            │   │
│  │                                                                      │   │
│  │  ┌────────────────────────────────────────────────────────────┐    │   │
│  │  │  User Credentials (OAuth2 Token Storage)                   │    │   │
│  │  │  - Credential Name: dropbox_oauth                          │    │   │
│  │  │  - Password: OAuth2 Access Token                           │    │   │
│  │  └────────────────────────────────────────────────────────────┘    │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└───────────────────────────────────────┬─────────────────────────────────────┘
                                        │
                                        │ HTTPS (OAuth2)
                                        │
┌───────────────────────────────────────▼─────────────────────────────────────┐
│                            Dropbox API v2                                    │
│                                                                               │
│  ┌─────────────────────────────────┐    ┌──────────────────────────────┐   │
│  │  API Endpoint                   │    │  Content Endpoint            │   │
│  │  https://api.dropboxapi.com/2   │    │  https://content.dropbox...  │   │
│  │                                  │    │                              │   │
│  │  - files/list_folder            │    │  - files/upload              │   │
│  │  - files/search                 │    │  - files/download            │   │
│  │  - files/get_metadata           │    │  - files/download_zip        │   │
│  │  - files/copy_v2                │    │                              │   │
│  │  - files/move_v2                │    │                              │   │
│  │  - files/delete_v2              │    │                              │   │
│  │  - files/create_folder_v2       │    │                              │   │
│  │  - files/get_temporary_link     │    │                              │   │
│  │  - users/get_space_usage        │    │                              │   │
│  │  - files/list_revisions         │    │                              │   │
│  │  - file_properties/...          │    │                              │   │
│  └─────────────────────────────────┘    └──────────────────────────────┘   │
│                                                                               │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                        Dropbox Storage                                │  │
│  │  - Files and Folders                                                  │  │
│  │  - Metadata and Properties                                            │  │
│  │  - Revision History                                                   │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────────────────┘
```

### Component Structure

```
reference-adapter/
├── config.adk                          # Adapter configuration
├── pom.xml                             # Maven build configuration
├── extlibs/                            # External libraries
│   ├── commons-codec-1.15.jar
│   ├── commons-logging-1.2.jar
│   ├── httpclient-4.5.13.jar
│   └── httpcore-4.4.14.jar
└── src/
    └── main/
        ├── java/
        │   └── com/sap/it/custom/dropbox/
        │       ├── DropboxComponent.java      # Camel component
        │       ├── DropboxEndpoint.java       # Endpoint configuration
        │       ├── DropboxProducer.java       # Receiver adapter logic
        │       ├── DropboxConsumer.java       # Sender adapter logic
        │       └── client/
        │           └── DropboxClient.java     # Dropbox API client
        └── resources/
            ├── metadata/
            │   └── metadata.xml               # SAP CPI UI metadata
            └── META-INF/services/org/apache/camel/component/
                └── sap-dropbox                # Component registration
```

### Detailed Component Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          SENDER ADAPTER FLOW                                 │
└─────────────────────────────────────────────────────────────────────────────┘

1. Scheduler triggers poll
         │
         ▼
2. DropboxConsumer.poll()
         │
         ├──▶ Read configuration (filePath/folderPath, operation)
         │
         ├──▶ DropboxClient.executeContentApiCall()
         │         │
         │         ├──▶ Retrieve OAuth2 token from SAP Secure Store
         │         │
         │         ├──▶ HTTP POST to Dropbox Content API
         │         │    (https://content.dropboxapi.com/2/files/download)
         │         │
         │         └──▶ Return file content + metadata
         │
         ├──▶ Create Camel Exchange with file content
         │
         ├──▶ Process message through integration flow
         │
         └──▶ Post-Processing
                  │
                  ├──▶ Delete: Remove file from Dropbox
                  ├──▶ Archive: Move file to archive directory
                  ├──▶ KeepAndProcess: No action (process again next poll)
                  └──▶ KeepAndMark: Mark as processed (idempotent)


┌─────────────────────────────────────────────────────────────────────────────┐
│                         RECEIVER ADAPTER FLOW                                │
└─────────────────────────────────────────────────────────────────────────────┘

1. Integration flow sends message
         │
         ▼
2. DropboxProducer.process(Exchange)
         │
         ├──▶ Read configuration & headers (operation, paths, parameters)
         │
         ├──▶ Determine operation type (14 operations supported)
         │
         ├──▶ Build API request
         │         │
         │         ├──▶ For Upload/Download: Use Content API
         │         └──▶ For Others: Use Regular API
         │
         ├──▶ DropboxClient.executeApiCall() / executeContentApiCall()
         │         │
         │         ├──▶ Retrieve OAuth2 token from SAP Secure Store
         │         │
         │         ├──▶ HTTP POST to Dropbox API
         │         │    - API: https://api.dropboxapi.com/2/...
         │         │    - Content: https://content.dropboxapi.com/2/...
         │         │
         │         └──▶ Return response (JSON/XML or binary content)
         │
         ├──▶ Process response
         │         │
         │         ├──▶ Set response headers (status, metadata)
         │         └──▶ Set message body (response data)
         │
         └──▶ Return to integration flow
```

### Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            DATA FLOW                                         │
└─────────────────────────────────────────────────────────────────────────────┘

SENDER (Inbound):
┌──────────┐    ┌──────────────┐    ┌─────────────┐    ┌──────────────┐
│ Dropbox  │───▶│   Dropbox    │───▶│   Camel     │───▶│ Integration  │
│ Storage  │    │   Consumer   │    │  Exchange   │    │    Flow      │
└──────────┘    └──────────────┘    └─────────────┘    └──────────────┘
   (File)         (Poll & Get)        (Message)          (Process)
                       │
                       ▼
                 Post-Process
                 (Delete/Archive/
                  Keep/Mark)


RECEIVER (Outbound):
┌──────────────┐    ┌─────────────┐    ┌──────────────┐    ┌──────────┐
│ Integration  │───▶│   Camel     │───▶│   Dropbox    │───▶│ Dropbox  │
│    Flow      │    │  Exchange   │    │   Producer   │    │ Storage  │
└──────────────┘    └─────────────┘    └──────────────┘    └──────────┘
  (Trigger)           (Message)          (Execute Op)        (Result)
                                              │
                                              ▼
                                        14 Operations:
                                        - Upload
                                        - Download
                                        - Copy/Move/Delete
                                        - Create Folder
                                        - List/Search
                                        - Metadata Ops
                                        - etc.
```

### Authentication Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        OAUTH2 AUTHENTICATION FLOW                            │
└─────────────────────────────────────────────────────────────────────────────┘

1. Adapter Initialization
         │
         ▼
2. DropboxClient Constructor
         │
         ├──▶ Call retrieveAccessToken(credentialName)
         │
         ├──▶ ITApiFactory.getService(SecureStoreService.class)
         │
         ├──▶ secureStoreService.getUserCredential(credentialName)
         │         │
         │         └──▶ Returns UserCredential object
         │
         ├──▶ Extract password field (contains OAuth2 token)
         │
         └──▶ Store token for API calls

3. API Call Execution
         │
         ├──▶ Set Authorization header: "Bearer <token>"
         │
         ├──▶ Execute HTTP request to Dropbox API
         │
         └──▶ Dropbox validates token and processes request


Configuration in SAP CPI Security Material:
┌────────────────────────────────────────┐
│  User Credential                       │
│  ─────────────────                     │
│  Name: dropbox_oauth                   │
│  User: dropbox (any value)             │
│  Password: dbtk_xxxxx... (OAuth token) │
└────────────────────────────────────────┘
```

### Key Classes

#### 1. DropboxComponent
- Extends `DefaultComponent`
- Manages endpoint lifecycle
- Creates and configures endpoints

#### 2. DropboxEndpoint
- Extends `DefaultPollingEndpoint`
- Defines all configuration parameters
- Creates producers and consumers
- Supports both sender and receiver modes

#### 3. DropboxProducer (Receiver Adapter)
- Extends `DefaultProducer`
- Handles outbound operations
- Supports 14 different operations
- Dynamic parameter support via headers

#### 4. DropboxConsumer (Sender Adapter)
- Extends `ScheduledPollConsumer`
- Polls Dropbox for files
- Supports post-processing actions
- Configurable polling intervals

#### 5. DropboxClient
- HTTP client for Dropbox API v2
- OAuth2 authentication via SAP Secure Store
- Handles both API and content endpoints
- Error handling and logging

## Configuration

### Connection Settings

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| credentialName | String | Yes | - | OAuth2 credential alias from Security Material |
| timeout | Integer | No | 300000 | Timeout in milliseconds |

### Sender Adapter Settings

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| operation | String | Yes | - | Download or Download_Archive |
| filePath | String | Conditional | - | File path for download operation |
| folderPath | String | Conditional | - | Folder path for archive operation |
| postProcessing | String | No | KeepAndProcess | Delete, Archive, KeepAndProcess, KeepAndMark |
| archiveDirectory | String | Conditional | - | Required when postProcessing=Archive |
| raiseExceptionOnPostProcessingFailure | Boolean | No | false | Raise exception on post-processing failure |
| persistDuration | Integer | No | 90 | Days to persist in idempotent repository |
| delay | Long | No | 60000 | Polling delay in milliseconds |
| initialDelay | Long | No | 1000 | Initial delay before first poll |

### Receiver Adapter Settings

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| operation | String | Yes | - | Operation to perform (see operations list) |
| filePath | String | Conditional | - | File path for operations |
| folderPath | String | Conditional | - | Folder path for operations |
| sourcePath | String | Conditional | - | Source path for copy/move |
| destinationPath | String | Conditional | - | Destination path for copy/move |
| handlingExistingFiles | String | No | Fail | AutoRename, Fail, Ignore, Overwrite, Dynamic |
| query | String | Conditional | - | Search query string |
| limit | Integer | No | 1000 | Limit for list operations |
| recursive | Boolean | No | false | Recursive folder listing |
| responseFormat | String | No | JSON | JSON or XML |

## Dropbox API Operations

### File Operations
1. **Upload**: Upload file content from message body
2. **Download**: Download file content to message body
3. **Copy**: Copy file or folder to new location
4. **Move**: Move file or folder to new location
5. **Delete**: Delete file or folder

### Folder Operations
6. **Create_Folder**: Create a new folder
7. **List_Folder**: List folder contents
8. **Download_Archive**: Download folder as ZIP

### Metadata Operations
9. **Get_Metadata**: Get file/folder metadata
10. **Update_Metadata**: Update file properties
11. **List_Revisions**: List file revision history

### Search & Discovery
12. **Search**: Search for files and folders
13. **Get_File_URL**: Get temporary download link
14. **Get_Storage_Statistics**: Get account storage info

## Authentication

The adapter uses OAuth2 authentication with Dropbox. You must:

1. Create a Dropbox App in the Dropbox App Console
2. Generate an OAuth2 access token
3. Store the token in SAP CPI Security Material as a User Credential:
   - **User**: Any value (e.g., "dropbox")
   - **Password**: Your OAuth2 access token

## Dynamic Configuration

The adapter supports dynamic configuration via message headers:

### Common Headers
- `SAP_DropboxOperation`: Override operation
- `SAP_DropboxTimeout`: Override timeout
- `SAP_DropboxPath`: Override file/folder path
- `SAP_DropboxSourcePath`: Override source path
- `SAP_DropboxDestinationPath`: Override destination path

### Operation-Specific Headers
- `SAP_DropboxHandling`: Override file handling mode
- `SAP_DropboxQuery`: Override search query
- `SAP_DropboxLimit`: Override result limit
- `SAP_DropboxRecursive`: Override recursive flag
- `SAP_DropboxMute`: Override mute notifications

### Response Headers
- `SAP_DropboxStatusCode`: HTTP status code
- `SAP_DropboxMetadata`: File metadata (for downloads)
- `SAP_Dropbox_Delete_Response`: Delete operation status
- `SAP_Dropbox_Archive_Response`: Archive operation status

## Usage Examples

### Example 1: Upload File (Receiver)

**Configuration:**
- Operation: Upload
- File Path: `/documents/invoice.pdf`
- Handling Existing Files: Overwrite
- Credential Name: dropbox_oauth

**Message Body:** Binary file content

**Result:** File uploaded to Dropbox at specified path

### Example 2: Download File (Sender)

**Configuration:**
- Operation: Download
- File Path: `/documents/invoice.pdf`
- Post-Processing: Archive
- Archive Directory: `/processed`
- Polling Delay: 60000 ms

**Result:** File downloaded and moved to archive folder

### Example 3: Search Files (Receiver)

**Configuration:**
- Operation: Search
- Query: invoice
- Folder Path: `/documents`
- Max Results: 100

**Result:** JSON response with matching files

### Example 4: List Folder (Receiver)

**Configuration:**
- Operation: List_Folder
- Folder Path: `/documents`
- Recursive: true
- Limit: 1000

**Result:** JSON response with folder contents

## Building the Adapter

### Prerequisites
- Java 8 or higher
- Maven 3.6 or higher
- SAP ADK 2.3.0

### Build Commands

```bash
cd reference-adapter
mvn clean install
```

### Build Output
- **JAR**: `target/Dropbox-1.0.0.jar`
- **ESA**: `target/build/reference-adapter.esa`

The ESA (Enterprise Service Archive) file is ready for deployment to SAP CPI.

## Deployment

1. Build the adapter using Maven
2. Locate the ESA file: `reference-adapter/target/build/reference-adapter.esa`
3. Deploy to SAP CPI:
   - Navigate to SAP CPI tenant
   - Go to Monitor → Integrations → Manage Integration Content
   - Click "Add" → "Adapter"
   - Upload the ESA file
   - Activate the adapter

## Error Handling

The adapter includes comprehensive error handling:

- **Connection Errors**: Logged with details, exception thrown
- **Authentication Errors**: Clear error messages for credential issues
- **API Errors**: HTTP status codes and error messages returned
- **Post-Processing Errors**: Configurable exception handling
- **Timeout Errors**: Configurable timeout with proper cleanup

## Logging

The adapter uses SLF4J for logging:

- **INFO**: Operation start/completion, successful operations
- **DEBUG**: Detailed parameter values, API arguments
- **ERROR**: Failures, exceptions with stack traces

## Best Practices

1. **Credentials**: Always use Security Material for OAuth tokens
2. **Timeouts**: Adjust based on file sizes and network conditions
3. **Polling**: Set appropriate delays to avoid API rate limits
4. **Post-Processing**: Use "KeepAndMark" for idempotent processing
5. **Error Handling**: Enable exception raising for critical operations
6. **Monitoring**: Monitor adapter logs for issues
7. **Testing**: Test thoroughly in development before production

## Limitations

1. **File Size**: Large files may require timeout adjustments
2. **Rate Limits**: Dropbox API has rate limits (check Dropbox documentation)
3. **Concurrent Access**: Multiple consumers may process same file
4. **Idempotent Repository**: Currently logs only (requires implementation)

## Dependencies

### Maven Dependencies
- Apache Camel Core 3.14.7
- Apache Camel Support 3.14.7
- Apache HttpClient 4.5.13
- SAP ADK 2.3.0
- SAP ADK Public API (LATEST)
- SLF4J 1.7.32

### External Libraries (Bundled)
- httpclient-4.5.13.jar
- httpcore-4.4.14.jar
- commons-logging-1.2.jar
- commons-codec-1.15.jar

## Troubleshooting

### Issue: "Credential not found"
**Solution**: Verify credential name matches Security Material entry

### Issue: "Access token is empty"
**Solution**: Ensure OAuth token is stored in password field

### Issue: "Timeout errors"
**Solution**: Increase timeout parameter for large files

### Issue: "Post-processing failed"
**Solution**: Check archive directory exists and is writable

### Issue: "Operation not supported"
**Solution**: Verify operation name matches supported operations

## Support

For issues or questions:
1. Check adapter logs in SAP CPI
2. Verify Dropbox API documentation
3. Review SAP CPI adapter development guidelines
4. Contact SAP support for CPI-related issues

## License

This adapter is provided as-is for SAP CPI integration purposes.

## Version History

### 1.0.0 (Current)
- Initial release
- Apache Camel 3.14.7 support
- 14 Dropbox operations
- Sender and Receiver adapters
- OAuth2 authentication
- Comprehensive error handling
- SAP CPI UI integration

## References

- [Dropbox API v2 Documentation](https://www.dropbox.com/developers/documentation/http/documentation)
- [Apache Camel Documentation](https://camel.apache.org/manual/latest/)
- [SAP CPI Adapter Development Kit](https://help.sap.com/docs/CLOUD_INTEGRATION)
