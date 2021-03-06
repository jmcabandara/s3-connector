/**
 * (c) 2003-2015 MuleSoft, Inc. The software in this package is
 * published under the terms of the CPAL v1.0 license, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */

package org.mule.module.s3.connection.strategy;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.apache.commons.lang.StringUtils;
import org.mule.api.ConnectionException;
import org.mule.api.ConnectionExceptionCode;
import org.mule.api.annotations.*;
import org.mule.api.annotations.components.ConnectionManagement;
import org.mule.api.annotations.display.Placement;
import org.mule.api.annotations.param.ConnectionKey;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;
import org.mule.module.s3.simpleapi.SimpleAmazonS3;
import org.mule.module.s3.simpleapi.SimpleAmazonS3AmazonDevKitImpl;

@ReconnectOn(exceptions = {AmazonClientException.class})
@ConnectionManagement(friendlyName = "Configuration Management")
public class S3ConnectionManagement {

    /**
     * The optional communication protocol to use when sending requests to AWS.
     * Communication over HTTPS is the default
     */
    @Configurable
    @Optional
    private Protocol protocol;

    /**
     * The optional proxy port
     */
    @Configurable
    @Optional
    @Placement(group = "Proxy Settings")
    private String proxyHost;

    /**
     * The optional proxy port
     */
    @Configurable
    @Optional
    @Placement(group = "Proxy Settings")
    private Integer proxyPort;

    /**
     * The optional proxy username
     */
    @Configurable
    @Optional
    @Placement(group = "Proxy Settings")
    private String proxyUsername;

    /**
     * The optional proxy password
     */
    @Configurable
    @Optional
    @Placement(group = "Proxy Settings")
    private String proxyPassword;

    /**
     * The optional proxy domain
     */
    @Configurable
    @Optional
    @Placement(group = "Proxy Settings")
    private String proxyDomain;

    /**
     * The optional proxy workstation
     */
    @Configurable
    @Optional
    @Placement(group = "Proxy Settings")
    private String proxyWorkstation;

    /**
     * The amount of time to wait (in milliseconds) for data to be transferred
     * over an established, open connection before the connection is timed out.
     * A value of 0 means infinity, and is not recommended.
     */
    @Configurable
    @Default("50000")
    private Integer socketTimeout;

    /**
     * The amount of time to wait (in milliseconds) when initially establishing
     * a connection before giving up and timing out. A value of 0 means
     * infinity, and is not recommended.
     */
    @Configurable
    @Default("50000")
    private Integer connectionTimeout;

    /**
     * Sets the maximum number of allowed open HTTP connections.
     */
    @Configurable
    @Default("50")
    private Integer maxConnections;

    private SimpleAmazonS3 client;

    /**
     * Login to Amazon S3
     *
     * @param accessKey The access key provided by Amazon, needed for non anonymous operations
     * @param secretKey The secrete key provided by Amazon, needed for non anonymous operations
     * @throws org.mule.api.ConnectionException
     */
    @Connect
    @TestConnectivity
    public void connect(@ConnectionKey String accessKey, String secretKey) throws ConnectionException {
        try {
            if (getClient() == null) {
                setClient(new SimpleAmazonS3AmazonDevKitImpl(createAmazonS3(accessKey, secretKey)));
            }
            getClient().listBuckets();
        } catch (Exception e) {
            throw new ConnectionException(ConnectionExceptionCode.INCORRECT_CREDENTIALS, null, e.getMessage(), e);
        }
    }

    @Disconnect
    public void disconnect() {
        if (getClient() != null) {
            setClient(null);
        }
    }

    @ValidateConnection
    public boolean isConnected() {
        return getClient() != null;
    }

    @ConnectionIdentifier
    public String connectionId() {
        return "amazon_s3-";
    }


    /**
     * Creates an {@link com.amazonaws.services.s3.AmazonS3} client. If accessKey and secretKey are not set,
     * the resulting client is anonymous
     *
     * @return a new {@link com.amazonaws.services.s3.AmazonS3}
     */
    private AmazonS3 createAmazonS3(String accessKey, String secretKey) {
        ClientConfiguration clientConfig = new ClientConfiguration();

        if (StringUtils.isNotBlank(getProxyUsername())) {
            clientConfig.setProxyUsername(getProxyUsername());
        }
        if (getProxyPort() != null) {
            clientConfig.setProxyPort(proxyPort);
        }
        if (StringUtils.isNotBlank(getProxyPassword())) {
            clientConfig.setProxyPassword(getProxyPassword());
        }
        if (StringUtils.isNotBlank(getProxyHost())) {
            clientConfig.setProxyHost(getProxyHost());
        }
        if (StringUtils.isNotBlank(getProxyDomain())) {
            clientConfig.setProxyDomain(getProxyDomain());
        }
        if (StringUtils.isNotBlank(getProxyWorkstation())) {
            clientConfig.setProxyWorkstation(getProxyWorkstation());
        }
        if (getProtocol() != null) {
            clientConfig.setProtocol(getProtocol());
        }
        if (getConnectionTimeout() != null) {
            clientConfig.setConnectionTimeout(getConnectionTimeout());
        }
        if (getSocketTimeout() != null) {
            clientConfig.setSocketTimeout(getSocketTimeout());
        }

        if (getMaxConnections() != null) {
            clientConfig.setMaxConnections(getMaxConnections());
        }

        AmazonS3Client amazonS3Client = new AmazonS3Client(createCredentials(accessKey, secretKey),
                clientConfig);
        return amazonS3Client;
    }

    private AWSCredentials createCredentials(String accessKey, String secretKey) {
        if (StringUtils.isEmpty(accessKey) && StringUtils.isEmpty(secretKey)) {
            return null;
        }
        return new BasicAWSCredentials(accessKey, secretKey);
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(Integer socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public SimpleAmazonS3 getClient() {
        return client;
    }

    public void setClient(SimpleAmazonS3 client) {
        this.client = client;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public String getProxyDomain() {
        return proxyDomain;
    }

    public void setProxyDomain(String proxyDomain) {
        this.proxyDomain = proxyDomain;
    }

    public String getProxyWorkstation() {
        return proxyWorkstation;
    }

    public void setProxyWorkstation(String proxyWorkstation) {
        this.proxyWorkstation = proxyWorkstation;
    }

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }
}
