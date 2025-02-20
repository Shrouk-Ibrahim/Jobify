import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JobViewModel : ViewModel() {
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private var currentSearchQuery: String? = null
    private var currentCategories: List<String>? = null
    private val _jobs = MutableLiveData<List<Job>?>()
    val jobs: LiveData<List<Job>?> get() = _jobs
    private val _savedJobs = MutableLiveData<List<Job>>()
    val savedJobs: LiveData<List<Job>> get() = _savedJobs

    private val db = FirebaseFirestore.getInstance()

    fun fetchSavedJobs(userId: String, query: String? = null, categories: List<String>? = null) {
        _isLoading.value = true

        db.collection("users").document(userId).collection("savedJobs")
            .get()
            .addOnSuccessListener { documents ->
                val jobsList = documents.toObjects(Job::class.java)

                // Apply query filter if provided
                val filteredByQuery = if (!query.isNullOrEmpty()) {
                    jobsList.filter { job ->
                        job.name.contains(query, ignoreCase = true) || job.seo_info.contains(query, ignoreCase = true)
                    }
                } else {
                    jobsList
                }



                // Apply category filter if provided
                val filteredByCategories = if (!categories.isNullOrEmpty()) {
                    filteredByQuery.filter { job ->
                        categories.contains(job.category.id)
                    }
                } else {
                    filteredByQuery
                }

                _savedJobs.value = filteredByCategories
                _isLoading.value = false
            }
            .addOnFailureListener { exception ->
                _savedJobs.value = emptyList() // Set to empty list on failure
                _isLoading.value = false
                Log.e("JobViewModel", "Error fetching saved jobs", exception)
                _errorMessage.value = "Error fetching saved jobs: ${exception.message}"
            }
    }

    fun fetchJobs(
        query: String? = null,
        categories: List<String>? = null,
        page: Int = 1,
        limit: Int = 10
    ) {
        currentSearchQuery = query

        _isLoading.value = true

        val jobNames = if (!query.isNullOrEmpty()) listOf(query) else null

        val categoriesAsInt = categories?.map { it.toInt() } // Convert List<String> to List<Int>
        FreelancerRetrofitClient.instance.searchJobs(
            jobNames = jobNames,
            categories = categoriesAsInt, // Pass the converted list
            page = page,
            limit = limit
        ).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                _isLoading.value = false

                if (response.isSuccessful) {
                    handleSuccessfulResponse(response)
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                handleNetworkFailure(t)
            }
        })
    }

    private fun handleSuccessfulResponse(response: Response<ApiResponse>) {
        val apiResponse = response.body()
        Log.d("JobViewModel", "API Response: ${apiResponse?.result}")

        if (apiResponse?.result?.isNotEmpty() == true) {
            _jobs.value = apiResponse.result
        } else {
            _errorMessage.value = "No jobs found"
            _jobs.value = emptyList()
        }
    }

    private fun handleErrorResponse(response: Response<ApiResponse>) {
        val errorBody = response.errorBody()?.string()
        Log.e("JobViewModel", "API Error: $errorBody")
        _errorMessage.value = "Error: ${response.code()} - ${errorBody?.substring(0..50)}..."
        _jobs.value = emptyList()
    }

    private fun handleNetworkFailure(t: Throwable) {
        _isLoading.value = false
        Log.e("JobViewModel", "Network Failure: ${t.message}", t)
        _errorMessage.value = "Network error: ${t.message}"
        _jobs.value = emptyList()
    }
}