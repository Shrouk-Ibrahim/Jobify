import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("jobs/search/")
    fun searchJobs(
        @Query("job_names[]") jobNames: List<String>? = null, // Search by job names
        @Query("categories[]") categories: List<Int>? = null, // Filter by categories
        @Query("page") page: Int = 1, // Pagination support
        @Query("limit") limit: Int = 10 // Limit results per page
    ): Call<ApiResponse>
}