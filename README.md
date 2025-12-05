# Google Drive Adapter for SAP CPI Integration Suite

## Overview

This custom adapter enables seamless integration between SAP Cloud Platform Integration (CPI) and Google Drive, allowing you to perform file operations such as upload, download, list, and delete files directly from your integration flows.

## Features

- **Upload Files**: Upload files from SAP CPI to Google Drive with support for folder organization
- **Download Files**: Download files from Google Drive to SAP CPI for further processing
- **List Files**: Retrieve a list of files from Google Drive or specific folders
- **Delete Files**: Remove files from Google Drive programmatically
- **Secure Authentication**: Uses Google Service Account authentication stored in SAP CPI Secure Store
- **Flexible Configuration**: Support for dynamic file names, MIME types, and folder IDs

## Technical Specifications

- **Apache Camel Version**: 3.14.7
- **ADK Version**: 2.3.0
- **Java Version**: 1.8
- **Adapter Type**: Receiver (outbound from SAP CPI to Google Drive)
- **URI Scheme**: `sap-googledrive`

## Prerequisites

1. **Google Cloud Platform Setup**:
   - Create a Google Cloud Project
   - Enable Google Drive API
   - Create a Service Account
   - Download the Service Account JSON key file
   - Grant the Service Account access to the Google Drive folders you want to access

2. **SAP CPI Setup**:
   - Access to SAP Cloud Platform Integration tenant
   - Permission to deploy custom adapters
   - Access to Security Material (Secure Store)

## Installation

### Step 1: Build the Adapter

```bash
mvn clean install
```

This will generate the adapter archive file in the `target` directory.

### Step 2: Deploy to SAP CPI

1. Log in to your SAP CPI tenant
2. Navigate to **Monitor** → **Integrations**
3. Go to **Manage Security Material**
4. Create a new **User Credentials** entry:
   - **Name**: Choose a meaningful name (e.g., `GoogleDriveServiceAccount`)
   - **User**: Can be any value (not used)
   - **Password**: Paste the entire content of your Google Service Account JSON key file

5. Navigate to **Design** → **Custom Adapters**
6. Click **Add** and upload the generated `.esa` file from the `target` directory
7. Deploy the adapter

### Step 3: Configure Integration Flow

1. Create or edit an integration flow
2. Add a **Receiver** channel
3. Select **Google Drive** as the adapter type
4. Configure the connection parameters (see Configuration section below)

## Configuration

### Connection Parameters

#### Authentication Group

| Parameter | Required | Description | Example |
|-----------|----------|-------------|---------|
| **Credential Name** | Yes | Name of the credential stored in SAP CPI Secure Store containing the Google Service Account JSON key | `GoogleDriveServiceAccount` |
| **Service Account Email** | No | Email address of the Google Service Account (for reference) | `my-service@project.iam.gserviceaccount.com` |
| **Application Name** | No | Application name for Google Drive API client | `SAP-CPI-Google-Drive-Adapter` |

#### Operation Settings Group

| Parameter | Required | Description | Example |
|-----------|----------|-------------|---------|
| **Operation** | Yes | Google Drive operation to perform | `upload`, `download`, `list`, `delete` |
| **Folder ID** | No | Google Drive folder ID for upload and list operations | `1a2b3c4d5e6f7g8h9i0j` |
| **File Name** | No | File name for upload operation (can also be set via `CamelFileName` header) | `document.pdf` |
| **MIME Type** | No | MIME type of the file for upload operation | `application/pdf` |

## Usage Examples

### Example 1: Upload a File to Google Drive

**Integration Flow Configuration**:
- **Operation**: `upload`
- **Credential Name**: `GoogleDriveServiceAccount`
- **Folder ID**: `1a2b3c4d5e6f7g8h9i0j` (optional)
- **File Name**: `report.pdf` (or set via header)
- **MIME Type**: `application/pdf`

**Groovy Script (before adapter call)**:
```groovy
import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    // Set file name dynamically
    message.setHeader("CamelFileName", "monthly_report_" + new Date().format("yyyyMMdd") + ".pdf")
    
    // File content should be in message body
    return message
}
```

### Example 2: Download a File from Google Drive

**Integration Flow Configuration**:
- **Operation**: `download`
- **Credential Name**: `GoogleDriveServiceAccount`

**Groovy Script (before adapter call)**:
```groovy
import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    // Set the Google Drive file ID to download
    message.setHeader("GoogleDriveFileId", "1a2b3c4d5e6f7g8h9i0j")
    return message
}
```

### Example 3: List Files in a Folder

**Integration Flow Configuration**:
- **Operation**: `list`
- **Credential Name**: `GoogleDriveServiceAccount`
- **Folder ID**: `1a2b3c4d5e6f7g8h9i0j`

The adapter will return a formatted list of files in the message body.

### Example 4: Delete a File

**Integration Flow Configuration**:
- **Operation**: `delete`
- **Credential Name**: `GoogleDriveServiceAccount`

**Groovy Script (before adapter call)**:
```groovy
import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    // Set the Google Drive file ID to delete
    message.setHeader("GoogleDriveFileId", "1a2b3c4d5e6f7g8h9i0j")
    return message
}
```

## Headers and Properties

### Input Headers

| Header Name | Operation | Description |
|-------------|-----------|-------------|
| `CamelFileName` | upload | File name for the uploaded file (overrides configuration) |
| `GoogleDriveFileId` | download, delete | Google Drive file ID for the operation |

### Output Headers

| Header Name | Operation | Description |
|-------------|-----------|-------------|
| `GoogleDriveFileId` | upload | ID of the uploaded file |
| `CamelFileName` | download | Name of the downloaded file |
| `GoogleDriveMimeType` | download | MIME type of the downloaded file |

### Exchange Properties

| Property Name | Description |
|---------------|-------------|
| `GOOGLE_DRIVE_OPERATION_STATUS` | Status of the operation (`SUCCESS` or `FAILED`) |
| `GOOGLE_DRIVE_ERROR_MESSAGE` | Error message if operation failed |

## Supported Operations

### 1. Upload
Uploads a file from SAP CPI to Google Drive.

**Input**: File content in message body (String, byte[], or InputStream)  
**Output**: Success message with file ID and name

### 2. Download
Downloads a file from Google Drive to SAP CPI.

**Input**: File ID in `GoogleDriveFileId` header  
**Output**: File content in message body (InputStream)

### 3. List
Lists files in Google Drive or a specific folder.

**Input**: Optional folder ID in configuration  
**Output**: Formatted list of files with IDs, names, and MIME types

### 4. Delete
Deletes a file from Google Drive.

**Input**: File ID in `GoogleDriveFileId` header  
**Output**: Success message with deleted file name

## Error Handling

The adapter includes comprehensive error handling:

- **Authentication Errors**: Thrown if credentials are invalid or not found
- **API Errors**: Google Drive API errors are logged and propagated
- **Validation Errors**: Missing required parameters result in clear error messages
- **Network Errors**: Connection issues are caught and reported

All errors set the `GOOGLE_DRIVE_OPERATION_STATUS` property to `FAILED` and include details in `GOOGLE_DRIVE_ERROR_MESSAGE`.

## Security Considerations

1. **Service Account Key**: Store the Google Service Account JSON key securely in SAP CPI Secure Store
2. **Least Privilege**: Grant the Service Account only the necessary permissions in Google Drive
3. **Key Rotation**: Regularly rotate Service Account keys
4. **Audit Logging**: Monitor adapter usage through SAP CPI monitoring tools

## Troubleshooting

### Common Issues

1. **"Credential not found" Error**
   - Verify the credential name matches exactly (case-sensitive)
   - Ensure the credential is deployed in the Secure Store

2. **"Authentication Failed" Error**
   - Verify the Service Account JSON key is valid
   - Check that the Google Drive API is enabled in your Google Cloud Project
   - Ensure the Service Account has access to the target folders

3. **"File ID required" Error**
   - For download/delete operations, ensure `GoogleDriveFileId` header is set
   - Verify the file ID is correct and the Service Account has access

4. **"Unsupported body type" Error**
   - Ensure the message body for upload is String, byte[], or InputStream

## Dependencies

The adapter uses the following key dependencies:

- Apache Camel 3.14.7
- Google API Client 2.2.0
- Google Drive API v3
- Google Auth Library 1.19.0
- SAP ADK 2.3.0

## Version History

### Version 1.0.0 (Initial Release)
- Support for upload, download, list, and delete operations
- Service Account authentication via SAP CPI Secure Store
- Comprehensive error handling and logging
- Support for folder organization
- Dynamic file naming via headers

## Support and Contribution

For issues, questions, or contributions, please contact the development team or create an issue in the project repository.

## License

This adapter is provided as-is for use with SAP Cloud Platform Integration.

---

**Note**: This adapter is a custom implementation and is not officially supported by SAP or Google. Use in production environments should be thoroughly tested.
