package khome.core.clients

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post

class RestApiClient(delegate: HttpClient) {
    val underlyingClient = delegate

    suspend inline fun <reified T> get(block: HttpRequestBuilder.() -> Unit = {}): T = underlyingClient.get(block = block)
    suspend inline fun <reified T> post(block: HttpRequestBuilder.() -> Unit = {}): T = underlyingClient.post(block = block)
}
