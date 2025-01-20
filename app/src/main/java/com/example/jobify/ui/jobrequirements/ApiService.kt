import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("jobs/")
    fun getJobs(
        @Query("job_ids") jobIds: List<Int>? = null, // Optional: Fetch specific job IDs
        @Query("page") page: Int = 1, // Optional: Pagination
        @Query("limit") limit: Int = 10 // Optional: Limit results
    ): Call<ApiResponse>
}