data class ApiResponse(
    val status: String,   // Status of the API response
    val result: List<Job>?, // List of jobs
    val request_id: String? // Request ID (optional)
)


data class Job(
    val id: String = "", // Keep as String
    val name: String = "",
    val category: Category = Category(),
    val active_project_count: Int? = null,
    val seo_url: String = "",
    val seo_info: String = "",
    val local: Boolean = false,
    val questions: List<String>? = null,
    val timestamp: com.google.firebase.Timestamp? = null
){
    constructor() : this("", "", Category()) // Required for Firestore
}

// Category.kt
data class Category(
    val id: String = "",
    val name: String = ""
)