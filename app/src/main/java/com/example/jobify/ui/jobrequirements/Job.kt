data class ApiResponse(
    val status: String,   // Status of the API response
    val result: List<Job>?, // List of jobs
    val request_id: String? // Request ID (optional)
)

data class Job(
    val id: Int, // Changed from String to Int
    val name: String, // Changed from title to name
    val category: Category,
    val active_project_count: Int?,
    val seo_url: String?,
    val seo_info: String?,
    val local: Boolean?,
    val questions: List<String>? // Assuming questions is a list of strings
)

data class Category(
    val id: Int,
    val name: String
)