package com.sap.adapter.adk.genericapi.conn;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class AdapterSocketCF extends Socket {

    private static final Logger LOG = LoggerFactory.getLogger(AdapterSocketCF.class);
    private static final byte SOCKS5_VERSION = 0x05;
    private static final byte SOCKS5_JWT_AUTHENTICATION_METHOD = (byte) 0x80;
    private static final byte SOCKS5_JWT_AUTHENTICATION_METHOD_VERSION = 0x01;
    private static final byte SOCKS5_COMMAND_CONNECT_BYTE = 0x01;
    private static final byte SOCKS5_COMMAND_REQUEST_RESERVED_BYTE = 0x00;
    private static final byte SOCKS5_COMMAND_ADDRESS_TYPE_IPV4_BYTE = 0x01;
    private static final byte SOCKS5_COMMAND_ADDRESS_TYPE_DOMAIN_BYTE = 0x03;
    private static final byte SOCKS5_AUTHENTICATION_METHODS_COUNT = 0x01;
    private static final int SOCKS5_JWT_AUTHENTICATION_METHOD_UNSIGNED_VALUE = 0x80 & 0xFF;
    private static final byte SOCKS5_AUTHENTICATION_SUCCESS_BYTE = 0x00;

    private final String locationId;
    private final String proxyHost;
    private final int proxyPort;
    private final String jwtToken;

    public AdapterSocketCF(String locationId, String proxyHost, int proxyPort, String jwtToken) {
        this.locationId = Base64.encodeBase64String(locationId.getBytes());
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.jwtToken = jwtToken;
    }

    private InetSocketAddress getProxyAddress() {
        return new InetSocketAddress(proxyHost, proxyPort);
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        LOG.error("[TCP] Started Socket connection");
        super.connect(getProxyAddress(), timeout);
        LOG.error("[TCP] Connected to proxy!");
        OutputStream outputStream = getOutputStream();
        InputStream inputStream = getInputStream();
        executeSOCKS5InitialRequest(outputStream, inputStream);
        executeSOCKS5AuthenticationRequest(outputStream, locationId, jwtToken, inputStream);
        executeSOCKS5ConnectRequest(outputStream, (InetSocketAddress) endpoint, inputStream);
    }

    private void executeSOCKS5InitialRequest(OutputStream outputStream, InputStream inputStream) throws IOException {
        LOG.error("[TCP] executeSocks5init");
        byte[] initialRequest = createInitialSOCKS5Request();
        outputStream.write(initialRequest);

        assertServerInitialResponse(inputStream);
    }

    private byte[] createInitialSOCKS5Request() throws IOException {
        try (ByteArrayOutputStream byteArraysStream = new ByteArrayOutputStream()) {
            byteArraysStream.write(SOCKS5_VERSION);
            byteArraysStream.write(SOCKS5_AUTHENTICATION_METHODS_COUNT);
            byteArraysStream.write(SOCKS5_JWT_AUTHENTICATION_METHOD);
            return byteArraysStream.toByteArray();
        }
    }

    private void assertServerInitialResponse(InputStream inputStream) throws IOException {

        int versionByte = inputStream.read();
        if (SOCKS5_VERSION != versionByte) {
            throw new SocketException(
                    String.format("Unsupported SOCKS version - expected %s, but received %s", SOCKS5_VERSION, versionByte));
        }

        int authenticationMethodValue = inputStream.read();
        if (SOCKS5_JWT_AUTHENTICATION_METHOD_UNSIGNED_VALUE != authenticationMethodValue) {
            throw new SocketException(String.format("Unsupported authentication method value - expected %s, but received %s",
                    SOCKS5_JWT_AUTHENTICATION_METHOD_UNSIGNED_VALUE, authenticationMethodValue));
        }
    }

    private void executeSOCKS5AuthenticationRequest(OutputStream outputStream, String locationId, String jwtToken, InputStream inputStream) throws IOException {
        byte[] authenticationRequest = createJWTAuthenticationRequest(locationId, jwtToken);
        outputStream.write(authenticationRequest);

        assertAuthenticationResponse(inputStream);
    }

    private byte[] createJWTAuthenticationRequest(String sccLocationId, String jwtToken) throws IOException {
        try (ByteArrayOutputStream byteArraysStream = new ByteArrayOutputStream()) {
            byteArraysStream.write(SOCKS5_JWT_AUTHENTICATION_METHOD_VERSION);
            byteArraysStream.write(ByteBuffer.allocate(4).putInt(jwtToken.getBytes().length).array());
            byteArraysStream.write(jwtToken.getBytes());
            byteArraysStream.write(ByteBuffer.allocate(1).put((byte) sccLocationId.getBytes().length).array());
            byteArraysStream.write(sccLocationId.getBytes());
            return byteArraysStream.toByteArray();
        }
    }

    private void assertAuthenticationResponse(InputStream inputStream) throws IOException {

        int authenticationMethodVersion = inputStream.read();
        LOG.error("[TCP] Authentication Method Version: {}, {}", authenticationMethodVersion, SOCKS5_JWT_AUTHENTICATION_METHOD_VERSION);
        if (SOCKS5_JWT_AUTHENTICATION_METHOD_VERSION != authenticationMethodVersion) {
            throw new SocketException(String.format("Unsupported authentication method version - expected %s, but received %s",
                    SOCKS5_JWT_AUTHENTICATION_METHOD_VERSION, authenticationMethodVersion));
        }

        int authenticationStatus = inputStream.read();
        LOG.error("[TCP] Authentication status: {}, {}", authenticationStatus, SOCKS5_AUTHENTICATION_SUCCESS_BYTE);
        if (SOCKS5_AUTHENTICATION_SUCCESS_BYTE != authenticationStatus) {
            throw new SocketException("Authentication failed!");
        }
    }

    private void executeSOCKS5ConnectRequest(OutputStream outputStream, InetSocketAddress endpoint, InputStream inputStream) throws IOException {
        LOG.error("[TCP] createConnectCommand- host: {}, port:{}", endpoint.getHostName(), endpoint.getPort());
        byte[] commandRequest = createConnectCommandRequest(endpoint);
        LOG.error("[TCP] Created the command request: {}", new String(commandRequest));
        outputStream.write(commandRequest);

        assertConnectCommandResponse(inputStream);
    }

    private byte[] createConnectCommandRequest(InetSocketAddress endpoint) throws IOException {
        String host = endpoint.getHostName();
        int port = endpoint.getPort();
        LOG.error("[TCP] createConnectCommand endpoint - host: {}, port:{}", host, port);
        try (ByteArrayOutputStream byteArraysStream = new ByteArrayOutputStream()) {
            byteArraysStream.write(SOCKS5_VERSION);
            byteArraysStream.write(SOCKS5_COMMAND_CONNECT_BYTE);
            byteArraysStream.write(SOCKS5_COMMAND_REQUEST_RESERVED_BYTE);
            byte[] hostToIPv4 = parseHostToIPv4(host);
            if (hostToIPv4 != null) {
                byteArraysStream.write(SOCKS5_COMMAND_ADDRESS_TYPE_IPV4_BYTE);
                byteArraysStream.write(hostToIPv4);
            } else {
                byteArraysStream.write(SOCKS5_COMMAND_ADDRESS_TYPE_DOMAIN_BYTE);
                byteArraysStream.write(ByteBuffer.allocate(1).put((byte) host.getBytes().length).array());
                byteArraysStream.write(host.getBytes());
            }
            byteArraysStream.write(ByteBuffer.allocate(2).putShort((short) port).array());
            return byteArraysStream.toByteArray();
        }
    }

    private void assertConnectCommandResponse(InputStream inputStream) throws IOException {

        LOG.error("[TCP] Waiting for the versionByte..");
        int versionByte = inputStream.read();
        if (SOCKS5_VERSION != versionByte) {
            throw new SocketException(
                    String.format("Unsupported SOCKS version - expected %s, but received %s", SOCKS5_VERSION, versionByte));
        }

        LOG.error("[TCP] Waiting for the connect status..");
        int connectStatusByte = inputStream.read();
        LOG.error("[TCP] Socket connectStatusByte: {}", connectStatusByte);
        assertConnectStatus(connectStatusByte);

        readRemainingCommandResponseBytes(inputStream);
    }

    private void assertConnectStatus(int commandConnectStatus) throws IOException {
        if (commandConnectStatus == 0) {
            return;
        }

        String commandConnectStatusTranslation;
        switch (commandConnectStatus) {
        case 1:
            commandConnectStatusTranslation = "FAILURE";
            break;
        case 2:
            commandConnectStatusTranslation = "FORBIDDEN";
            break;
        case 3:
            commandConnectStatusTranslation = "NETWORK_UNREACHABLE";
            break;
        case 4:
            commandConnectStatusTranslation = "HOST_UNREACHABLE";
            break;
        case 5:
            commandConnectStatusTranslation = "CONNECTION_REFUSED";
            break;
        case 6:
            commandConnectStatusTranslation = "TTL_EXPIRED";
            break;
        case 7:
            commandConnectStatusTranslation = "COMMAND_UNSUPPORTED";
            break;
        case 8:
            commandConnectStatusTranslation = "ADDRESS_UNSUPPORTED";
            break;
        default:
            commandConnectStatusTranslation = "UNKNOWN";
            break;
        }
        throw new SocketException("SOCKS5 command failed with status: " + commandConnectStatusTranslation);
    }

    private byte[] parseHostToIPv4(String hostName) {
        byte[] parsedHostName = null;
        String[] virtualHostOctets = hostName.split("\\.", -1);
        int octetsCount = virtualHostOctets.length;
        if (octetsCount == 4) {
            try {
                byte[] ipOctets = new byte[octetsCount];
                for (int i = 0; i < octetsCount; i++) {
                    int currentOctet = Integer.parseInt(virtualHostOctets[i]);
                    if ((currentOctet < 0) || (currentOctet > 255)) {
                        throw new IllegalArgumentException(String.format("Provided octet %s is not in the range of [0-255]", currentOctet));
                    }
                    ipOctets[i] = (byte) currentOctet;
                }
                parsedHostName = ipOctets;
            } catch (IllegalArgumentException ex) {
                return new byte[0];
            }
        }

        return parsedHostName;
    }

    private void readRemainingCommandResponseBytes(InputStream inputStream) throws IOException {
        int read;
        int addressTypeByte = inputStream.read();
        if (SOCKS5_COMMAND_ADDRESS_TYPE_IPV4_BYTE == addressTypeByte) {
            for (int i = 0; i < 6; i++) {
                read = inputStream.read();
                LOG.debug("Read: {}", read);
            }
        } else if (SOCKS5_COMMAND_ADDRESS_TYPE_DOMAIN_BYTE == addressTypeByte) {
            int domainNameLength = inputStream.read();
            int portBytes = 2;
            read = inputStream.read(new byte[domainNameLength + portBytes], 0, domainNameLength + portBytes);
            LOG.debug("Read: {}", read);
        }
    }
}
