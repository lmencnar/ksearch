package com.example.ksearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.elasticsearch.core.SearchResponse
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.Transport
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.Closeable
import java.security.cert.CertificateException
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Component
class ElasticsearchClientWrapper// DANGER!!// Create a trust manager that does not validate certificate chains
    (
    @Value("\${elasticsearch.host}") host: String,
    @Value("\${elasticsearch.user}") user: String,
    @Value("\${elasticsearch.password}") password: String
) : Closeable {


    private final val transport: Transport
    private final val client: ElasticsearchClient

    init {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                return arrayOf()
            }
        })
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        val credsProv = BasicCredentialsProvider()
        credsProv.setCredentials(AuthScope.ANY, UsernamePasswordCredentials(user, password))
        val restClient = RestClient
                .builder(HttpHost(host, 9200, "https"))
                .setHttpClientConfigCallback { hc -> hc
                        .setSSLContext(sslContext)
                        .setSSLHostnameVerifier { _, _ -> true } // DANGER!!
                        .setDefaultCredentialsProvider(credsProv)
                }
                .build()
        transport = RestClientTransport(
                restClient, JacksonJsonpMapper()
        )
        client = ElasticsearchClient(transport)
    }

    fun <TDocument> search(
            request: SearchRequest,
            tDocumentClass: Class<TDocument>
    ): SearchResponse<TDocument> = client.search(request, tDocumentClass)

    override fun close() {
        transport.close()
    }
}