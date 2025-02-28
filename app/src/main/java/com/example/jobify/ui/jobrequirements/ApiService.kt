import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("projects/{projectId}")
    suspend fun getProjectDetails(@Path("projectId") projectId: Int): ProjectDetailsResponse
    @GET("projects/")
    suspend fun getProjects(
        @Query("projects[]") projectIds: List<Int>? = null, // Corrected parameter name
        @Query("sort_field") sortField: String? = "time_submitted",
        @Query("sort_order") sortOrder: String? = "desc",
        @Query("frontend_project_status[]") frontendProjectStatus: List<String>? = listOf("open")
    ): ProjectResponse
}