import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JobViewModel : ViewModel() {

    private val _jobs = MutableLiveData<List<Job>?>()
    val jobs: LiveData<List<Job>?> get() = _jobs

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private var currentSearchQuery: String? = null
    private var currentCategories: List<Int>? = null

    fun fetchJobs(
        query: String? = null,
        categories: List<Int>? = null,
        page: Int = 1,
        limit: Int = 10
    ) {
        currentSearchQuery = query
        currentCategories = categories
        _isLoading.value = true

        val jobNames = if (!query.isNullOrEmpty()) listOf(query) else null

        RetrofitClient.instance.searchJobs(
            jobNames = jobNames,
            categories = categories,
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