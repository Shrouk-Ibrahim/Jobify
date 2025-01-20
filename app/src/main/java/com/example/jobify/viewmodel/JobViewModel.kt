import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JobViewModel : ViewModel() {

    // LiveData to hold the list of jobs
    private val _jobs = MutableLiveData<List<Job>?>()
    val jobs: LiveData<List<Job>?> get() = _jobs

    // LiveData to hold error messages or loading states
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // LiveData to hold loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Fetch jobs from the API
    fun fetchJobs(page: Int = 1, limit: Int = 10) {
        _isLoading.value = true

        RetrofitClient.instance.getJobs(page = page, limit = limit).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                _isLoading.value = false

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    Log.d("JobViewModel", "API Response: ${apiResponse?.result}")

                    if (apiResponse?.result?.isNotEmpty() == true) {
                        _jobs.value = apiResponse.result
                    } else {
                        _errorMessage.value = "No jobs found."
                        _jobs.value = emptyList()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("JobViewModel", "API Error: $errorBody")
                    _errorMessage.value = "Error: ${response.code()} - $errorBody"
                    _jobs.value = emptyList()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e("JobViewModel", "Network Failure: ${t.message}", t)
                _errorMessage.value = "Network error: ${t.message}"
                _jobs.value = emptyList()
            }
        })
    }
}