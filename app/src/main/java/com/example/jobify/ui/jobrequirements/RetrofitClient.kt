import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object FreelancerRetrofitClient {
    private const val BASE_URL = "https://www.freelancer.com/api/projects/0.1/"
    private const val AUTH_TOKEN = "rpyciI3uv6HKK8QDdfACDmHZvrikyl"

    val instance: ApiService by lazy {
        // Add logging interceptor
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Logs request and response body
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $AUTH_TOKEN")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor) // Add logging interceptor
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
// Retrofit Client for Unsplash API
object UnsplashRetrofitClient {
    private const val BASE_URL = "https://api.unsplash.com/"
    private const val ACCESS_KEY = "UUJvjKyNWX8egbLDGCCjy7KPMYQXWH6GIvYrN1XIMpk"

    val instance: UnsplashApi by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Client-ID $ACCESS_KEY")
                    .build()
                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UnsplashApi::class.java)
    }
}
