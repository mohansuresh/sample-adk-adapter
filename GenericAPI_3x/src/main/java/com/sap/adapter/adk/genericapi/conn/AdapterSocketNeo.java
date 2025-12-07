package com.sap.adapter.adk.genericapi.conn;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;

public class AdapterSocketNeo extends Socket {

    private final String userName;

    public AdapterSocketNeo(String proxyHost, int proxyPort, String userName) {
        super(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort)));
        this.userName = userName;
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        setSOCKS5ProxyAuthentication();
        super.connect(endpoint, timeout);
    }

    private void setSOCKS5ProxyAuthentication() {
        Authenticator.setDefault(new Authenticator() {

            @Override
            protected java.net.PasswordAuthentication getPasswordAuthentication() {
                return new java.net.PasswordAuthentication(userName, new char[] {});
            }
        });
    }

}
