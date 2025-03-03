import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class JobViewModel(private val apiService: ApiService) : ViewModel() {

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _projects = MutableLiveData<List<Project>>()
    val projects: LiveData<List<Project>> get() = _projects

    private val _savedJobs = MutableLiveData<List<Project>>()
    val savedJobs: LiveData<List<Project>> get() = _savedJobs

    private val db = FirebaseFirestore.getInstance()

    fun fetchProjects() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.getActiveProjects()
                _projects.value = response.result.projects
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                Log.e("JobViewModel", "API Error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchSavedJobs(userId: String) {
        _isLoading.value = true
        db.collection("users").document(userId).collection("savedJobs")
            .get()
            .addOnSuccessListener { documents ->
                val jobsList = documents.toObjects(Project::class.java)
                _savedJobs.value = jobsList
                _isLoading.value = false
            }
            .addOnFailureListener { exception ->
                _savedJobs.value = emptyList()
                _isLoading.value = false
                Log.e("JobViewModel", "Error fetching saved jobs", exception)
                _errorMessage.value = "Error fetching saved jobs: ${exception.message}"
            }
    }

    private val _projectDetails = MutableLiveData<Project>()
    val projectDetails: LiveData<Project> get() = _projectDetails

    fun fetchProjectDetails(projectId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.getProjectDetails(projectId)
                _projectDetails.value = response.result.project
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                Log.e("JobViewModel", "API Error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}