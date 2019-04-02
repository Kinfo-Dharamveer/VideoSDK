package com.lib.adloader.retrofit


import com.lib.adloader.utils.AppConstants
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

import java.util.concurrent.TimeUnit
import javax.net.ssl.*

class ApiClient {


    private var httpClient = OkHttpClient.Builder()
    private var retrofit: Retrofit? = null
    private val builder = Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(Gson()))


    fun <S> createService(serviceClass: Class<S>): S {
        try {

                httpClient.sslSocketFactory(getSSLSocketFactory())

                httpClient.hostnameVerifier(object : HostnameVerifier {
                    override fun verify(hostname: String, session: SSLSession): Boolean {
                        return true
                    }
                })


                httpClient.addInterceptor(Interceptor { chain ->
                    val original = chain.request()
                    // Request customization: add request headers
                    val requestBuilder = original.newBuilder()
                            .header("mow-referer", "https://manualdohomemmoderno.com.br/")
                            .method(original.method(), original.body())

                    if (requestBuilder != null) {
                        val request = requestBuilder.build()
                        return@Interceptor chain.proceed(request)
                    }
                    null
                })

                httpClient.addNetworkInterceptor { chain ->
                    val request = chain.request().newBuilder().addHeader("Connection", "close").build()
                    chain.proceed(request)
                }



            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client =
                    httpClient.addInterceptor(interceptor)

                            .connectTimeout(60, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .build()

            retrofit = builder
                    .client(client)
                    .build()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return retrofit!!.create(serviceClass)
    }

    private fun getSSLSocketFactory(): SSLSocketFactory {

        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }

                /*    val acceptedIssuers: Array<java.security.cert.X509Certificate>
                        get() = arrayOf()*/

                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                }
            })

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())

            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.getSocketFactory()

            return sslSocketFactory;



        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }



}
