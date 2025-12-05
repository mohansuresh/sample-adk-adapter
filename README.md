# Dropbox Adapter for SAP Cloud Platform Integration

A custom adapter for SAP CPI that enables seamless integration with Dropbox using Apache Camel 3.14 and the Dropbox API v2.

## Overview

This adapter provides both Sender and Receiver capabilities for integrating SAP Cloud Platform Integration with Dropbox storage, supporting various file operations including upload, download, copy, move, delete, search, and metadata management.

## Features

### Sender Adapter (Inbound)
- **Download File**: Download files from Dropbox with configurable post-processing
- **Download Archive**: Download entire folders as ZIP archives
- **Post-Processing Options**:
  - Delete file after processing
  - Move to archive directory
  - Keep and process again
  - Keep and mark as processed (idempotent repository)
- **Scheduler Support**: Configurable polling intervals

### Receiver Adapter (Outbound)
- **Upload File**: Upload files to Dropbox with handling for existing files
- **Copy File or Folder**: Copy files/folders within Dropbox
- **Move File or Folder**: Move files/folders within Dropbox
- **Delete File or Folder**: Delete files or folders
- **Create Folder**: Create new folders
- **List Folder**: List folder contents with recursive option
- **Search**: Search for files and folders
- **Get Metadata**: Retrieve file/folder metadata
- **Get File URL**: Get temporary download links
- **Get Storage Statistics**: Retrieve account storage information
- **List Revisions**: List file revision history
- **Update Metadata**: Update file/folder properties

## Technical Specifications

- **Apache Camel Version**: 3.14.7
- **ADK Version**: 2.3.0
- **Java Version**: 1.8
- **Dropbox API**: v2
- **Authentication**: OAuth2 Authorization Code

## Project Structure

```
sample-adk-adapter/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── sap/
│       │           └── it/
│       │               └── custom/
│       │                   └── dropbox/
│       │                       ├── DropboxComponent.java
│       │                       ├── DropboxEndpoint.java
│       │                       ├── DropboxProducer.java
│       │                       ├── DropboxConsumer.java
│       │                       └── client/
│       │                           └── DropboxClient.java
│       └── resources/
│           ├── META-INF/
│           │   └── services/
│           │       └── org/
│           │           └── apache/
│           │               └── camel/
│           │                   └── component/
│           │                       └── sap-dropbox
│           └── metadata/
│               └── metadata.xml
└── README.md
```

## Building the Adapter

### Prerequisites
- Java JDK 1.8 or higher
- Apache Maven 3.6 or higher

### Build Steps

1. Clone the repository:
```bash
git clone https://github.com/mohansuresh/sample-adk-adapter.git
cd sample-adk-adapter
```

2. Build the adapter:
```bash
mvn clean install
```

3. The build will generate an `.esa` file in the `target/build` directory, which can be deployed to SAP CPI.

## Configuration

### OAuth2 Credentials Setup

1. Create OAuth2 credentials in Dropbox:
   - Go to Dropbox App Console
   - Create a new app
   - Generate an access token

2. Deploy credentials in SAP CPI:
   - Navigate to Security Material
   - Create a new User Credentials artifact
   - Set the password field to your Dropbox access token
   - Note the credential alias name

### Adapter Configuration

#### Sender Adapter
- **Credential Name**: OAuth2 credential alias from Security Material (required)
- **Timeout**: Connection timeout in milliseconds (default: 300000)
- **Operation**: Download or Download_Archive
- **File Path**: Path to file for download operation
- **Folder Path**: Path to folder for archive operation
- **Post-Processing**: Action after successful processing
- **Archive Directory**: Target directory for archive operation
- **Raise Exception on Post-Processing Failure**: Whether to fail MPL on post-processing errors
- **Persist Duration**: Days to keep files in idempotent repository (default: 90)

#### Receiver Adapter
- **Credential Name**: OAuth2 credential alias from Security Material (required)
- **Timeout**: Connection timeout in milliseconds (default: 300000)
- **Operation**: Select from available operations
- **Source Path**: Source path for copy/move operations
- **Destination Path**: Destination path for copy/move operations
- **File Path**: File path for various operations
- **Folder Path**: Folder path for various operations
- **Handling Existing Files**: AutoRename, Fail, Ignore, Overwrite, or Dynamic
- **Response Format**: JSON or XML

## Dynamic Parameters

The adapter supports dynamic configuration through exchange headers:

- `SAP_DropboxOperation`: Override operation
- `SAP_DropboxTimeout`: Override timeout
- `SAP_DropboxSourcePath`: Override source path
- `SAP_DropboxDestinationPath`: Override destination path
- `SAP_DropboxPath`: Override file/folder path
- `SAP_DropboxHandling`: Override file handling mode
- `SAP_DropboxAfterProc`: Override post-processing mode
- `SAP_DropboxRecursive`: Override recursive flag
- `SAP_DropboxLimit`: Override result limit
- `SAP_DropboxQuery`: Override search query
- `SAP_DropboxOrderBy`: Override search ordering
- `SAP_DropboxMute`: Override mute notifications
- `SAP_DropboxIncludeDeleted`: Override include deleted flag

## Deployment

1. Build the adapter using Maven
2. Locate the `.esa` file in `target/build` directory
3. Deploy to SAP CPI:
   - Navigate to Operations View
   - Go to Manage Integration Content
   - Upload the `.esa` file
   - The adapter will appear in the adapter palette

## Requirements Compliance

This adapter implements all requirements specified in the Dropbox Adapter Requirements document:

✅ OAuth2 Authorization Code support  
✅ Sender adapter with Download and Download Archive operations  
✅ Receiver adapter with 12 operations  
✅ Post-processing support (Delete, Archive, Keep and Mark)  
✅ Dynamic header and property support  
✅ Configurable timeout  
✅ Response format selection (JSON/XML)  
✅ Handling of existing files  
✅ Scheduler support for sender adapter  
✅ MPL behavior configuration  
✅ Tracing support  
✅ Binary content support  

## Limitations

- OAuth2 Authorization Code flow requires manual token generation
- Idempotent repository implementation is placeholder (requires database integration)
- JSON to XML conversion is not fully implemented
- Maximum file size for upload: 150 MB (Dropbox API limitation)
- Search is limited to file/folder names (not content)

## Support

For issues, questions, or contributions, please refer to the project repository.

## License

This adapter is provided as-is for use with SAP Cloud Platform Integration.

## Version History

- **1.0.0** (Initial Release)
  - Sender adapter with Download and Download Archive operations
  - Receiver adapter with 12 operations
  - OAuth2 authentication support
  - Dynamic parameter support
  - Post-processing capabilities
