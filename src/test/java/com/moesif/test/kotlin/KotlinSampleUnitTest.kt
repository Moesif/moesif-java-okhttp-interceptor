package com.moesif.test.kotlin

import com.moesif.sdk.okhttp3client.MoesifOkHttp3Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.jupiter.api.Test
import java.io.IOException


/**
 * Unit Test in Kotlin
 * Set environment variable `MOESIF_APPLICATION_ID`
 * Else no events will be sent to moesif.
 */

class KotlinSampleRun {

    /**
     * The following test executes sample OkHttp3 recpie
     * from: https://square.github.io/okhttp/recipes/#synchronous-get-kt-java
     *
     */
    @Test
    fun `Kotlin test of Moesif Integrator`() {
        val client = OkHttpClient.Builder()
                .addInterceptor(MoesifOkHttp3Interceptor())
                .build()

        val request = Request.Builder()
                .url("https://publicobject.com/helloworld.txt")
                .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException(
                    "Unexpected code $response")

            for ((name, value) in response.headers) {
                println("$name: $value")
            }
            println(response.body!!.string())
        }
    }
}