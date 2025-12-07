package com.sap.adapter.adk.genericapi.factory;

import com.sap.adapter.adk.genericapi.conn.AdapterSocketCF;
import com.sap.adapter.adk.genericapi.conn.AdapterSocketNeo;
import com.sap.it.api.ccs.adapter.CloudConnectorProperties;
import com.sap.it.api.ccs.adapter.enums.AuthType;
import com.sap.it.api.ccs.adapter.exception.CloudConnectorPropertiesException;

import java.net.Socket;

public class AdapterSocketFactory {

    public static Socket getSocket(CloudConnectorProperties cloudConnectorProperties, String locationId)
            throws CloudConnectorPropertiesException {
        Socket socket;
        AuthType authType = cloudConnectorProperties.getAuthType();
        if (AuthType.PasswordAuthentication.equals(authType)) {
            socket = new AdapterSocketNeo(cloudConnectorProperties.getProxyHost(), cloudConnectorProperties.getProxyPort(),
                    cloudConnectorProperties.getUserName(locationId));
        } else {
            socket = new AdapterSocketCF(locationId, cloudConnectorProperties.getProxyHost(), cloudConnectorProperties.getProxyPort(),
                    cloudConnectorProperties.getJWTToken());
        }
        return socket;
    }
}
