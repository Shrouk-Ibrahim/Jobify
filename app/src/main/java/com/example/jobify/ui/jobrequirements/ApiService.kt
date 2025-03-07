import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("projects/{projectId}")
    suspend fun getProjectDetails(@Path("projectId") projectId: Int): ProjectDetailsResponse


    @GET("projects/active/")
    suspend fun getActiveProjects(
        @Query("project_statuses[]") status: String = "active"
    ): ProjectResponse
}