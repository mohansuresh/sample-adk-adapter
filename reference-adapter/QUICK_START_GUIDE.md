# Dropbox Adapter - Quick Start Guide

## Prerequisites

Before you begin, ensure you have:

1. ✅ SAP Cloud Platform Integration (CPI) tenant access
2. ✅ Dropbox account with API access
3. ✅ OAuth2 access token from Dropbox
4. ✅ The adapter ESA file: `reference-adapter/target/build/reference-adapter.esa`

## Step 1: Generate Dropbox OAuth2 Token

1. Go to [Dropbox App Console](https://www.dropbox.com/developers/apps)
2. Click "Create app"
3. Choose:
   - **API**: Scoped access
   - **Access**: Full Dropbox or App folder (based on your needs)
   - **Name**: Give your app a unique name
4. Click "Create app"
5. In the app settings, scroll to "OAuth 2" section
6. Click "Generate" under "Generated access token"
7. **Copy the token** - you'll need it in Step 3

## Step 2: Deploy Adapter to SAP CPI

1. Log in to your SAP CPI tenant
2. Navigate to: **Monitor → Integrations → Manage Integration Content**
3. Click **"Add"** → **"Adapter"**
4. Upload the file: `reference-adapter/target/build/reference-adapter.esa`
5. Click **"Deploy"**
6. Wait for deployment to complete (status: "Started")

## Step 3: Configure Security Material

1. In SAP CPI, navigate to: **Monitor → Integrations → Security Material**
2. Click **"Create"** → **"User Credentials"**
3. Enter:
   - **Name**: `dropbox_oauth` (or any name you prefer)
   - **Type**: User Credentials
   - **User**: `dropbox` (any value)
   - **Password**: Paste your OAuth2 token from Step 1
4. Click **"Deploy"**

## Step 4: Create Your First Integration Flow

### Example: Upload File to Dropbox (Receiver)

1. Create a new Integration Flow
2. Add a **Timer** start event (for testing)
3. Add a **Content Modifier** to create test content:
   ```
   Body: Hello from SAP CPI!
   ```
4. Add **Dropbox Receiver** adapter:
   - **Connection Tab**:
     - Credential Name: `dropbox_oauth`
     - Timeout: `300000`
   - **Processing Tab**:
     - Operation: `Upload`
     - File Path: `/test/hello.txt`
     - Handling Existing Files: `Overwrite`
5. Save and Deploy
6. Check your Dropbox - file should appear at `/test/hello.txt`

### Example: Download File from Dropbox (Sender)

1. Create a new Integration Flow
2. Add **Dropbox Sender** adapter:
   - **Connection Tab**:
     - Credential Name: `dropbox_oauth`
     - Timeout: `300000`
   - **Processing Tab**:
     - Operation: `Download`
     - File Path: `/test/hello.txt`
     - Post-Processing: `KeepAndProcess`
   - **Scheduler Tab**:
     - Delay: `60000` (poll every 60 seconds)
3. Add processing steps (e.g., Content Modifier to log content)
4. Save and Deploy
5. File will be downloaded every 60 seconds

## Common Operations

### Upload File
```
Operation: Upload
File Path: /folder/filename.ext
Handling Existing Files: Overwrite
```

### Download File
```
Operation: Download
File Path: /folder/filename.ext
Post-Processing: Archive
Archive Directory: /processed
```

### List Folder
```
Operation: List_Folder
Folder Path: /folder
Recursive: true
Limit: 1000
```

### Search Files
```
Operation: Search
Query: invoice
Folder Path: /documents
Max Results: 100
```

### Copy File
```
Operation: Copy
Source Path: /source/file.txt
Destination Path: /destination/file.txt
Handling Existing Files: AutoRename
```

### Delete File
```
Operation: Delete
File Path: /folder/file.txt
```

## Dynamic Configuration with Headers

You can override configuration at runtime using message headers:

```groovy
// In a Groovy Script
import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    // Override operation
    message.setHeader("SAP_DropboxOperation", "Upload")
    
    // Override file path
    message.setHeader("SAP_DropboxPath", "/dynamic/path/file.txt")
    
    // Override handling
    message.setHeader("SAP_DropboxHandling", "Overwrite")
    
    return message
}
```

## Troubleshooting

### Issue: "Credential not found"
- ✅ Verify credential name in Security Material matches adapter configuration
- ✅ Ensure credential is deployed

### Issue: "401 Unauthorized"
- ✅ Check OAuth token is valid
- ✅ Regenerate token if expired
- ✅ Verify token has required permissions

### Issue: "404 Not Found"
- ✅ Check file/folder path is correct
- ✅ Paths must start with `/`
- ✅ Verify file exists in Dropbox

### Issue: "Timeout"
- ✅ Increase timeout value for large files
- ✅ Check network connectivity
- ✅ Verify Dropbox API is accessible

## Testing Tips

1. **Start Simple**: Begin with Upload operation to test connectivity
2. **Use Timer**: Use Timer start event for testing receiver adapters
3. **Check Logs**: Monitor message processing logs in SAP CPI
4. **Verify Dropbox**: Check Dropbox web interface to confirm operations
5. **Test Post-Processing**: Test sender adapter post-processing with test files

## Next Steps

1. ✅ Review full documentation: `DROPBOX_ADAPTER_README.md`
2. ✅ Explore all 14 operations
3. ✅ Implement error handling in your flows
4. ✅ Set up monitoring and alerting
5. ✅ Test in development before production deployment

## Support Resources

- **Full Documentation**: See `DROPBOX_ADAPTER_README.md`
- **Dropbox API**: https://www.dropbox.com/developers/documentation
- **SAP CPI Help**: https://help.sap.com/docs/CLOUD_INTEGRATION
- **Adapter Logs**: Monitor → Message Processing → All Integration Flows

## Quick Reference: All Operations

| Operation | Type | Description |
|-----------|------|-------------|
| Download | Sender | Download single file |
| Download_Archive | Sender | Download folder as ZIP |
| Upload | Receiver | Upload file |
| Copy | Receiver | Copy file/folder |
| Move | Receiver | Move file/folder |
| Delete | Receiver | Delete file/folder |
| Create_Folder | Receiver | Create new folder |
| List_Folder | Receiver | List folder contents |
| Search | Receiver | Search files/folders |
| Get_Metadata | Receiver | Get file metadata |
| Get_File_URL | Receiver | Get temporary link |
| Get_Storage_Statistics | Receiver | Get storage info |
| List_Revisions | Receiver | List file revisions |
| Update_Metadata | Receiver | Update file properties |

---

**Ready to integrate? Start with Step 1!** 🚀
