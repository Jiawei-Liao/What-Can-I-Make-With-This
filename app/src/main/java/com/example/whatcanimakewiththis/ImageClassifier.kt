import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ImageProcessing {
    // Result of the last image classified
    private val _classificationResult = MutableLiveData<String?>()
    val classificationResult: LiveData<String?> = _classificationResult

    // Flag to indicate whether an image is being processed
    private val _isProcessing = MutableLiveData<Boolean>()
    val isProcessing: LiveData<Boolean> = _isProcessing

    // List of items
    private val _itemsList = MutableLiveData<List<String>>()
    val itemsList: LiveData<List<String>> = _itemsList

    // Create CoroutineScope to manage background process of classifying
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Current classification job
    private var currentProcessingJob: Job? = null

    fun processImage(byteArray: ByteArray) {
        // When new image is provided, cancel any current jobs
        cancelCurrentProcessing()

        // Reset the result
        _classificationResult.value = null
        _isProcessing.value = true

        // Start a new processing job
        currentProcessingJob = coroutineScope.launch(Dispatchers.IO) {
            if (isActive) {
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                val result = classifyImage(bitmap)

                withContext(Dispatchers.Main) {
                    _classificationResult.value = result
                    _isProcessing.value = false
                }
            }
        }
    }

    private fun cancelCurrentProcessing() {
        currentProcessingJob?.cancel()
        currentProcessingJob = null
        _isProcessing.value = false
    }

    private fun classifyImage(bitmap: Bitmap): String {
        // Initialise ML Kit classifier
        val labeler = ImageLabeling.getClient(
            ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.7f)
                .build()
        )
        val image = InputImage.fromBitmap(bitmap, 0)

        // Get label
        val labels = Tasks.await(labeler.process(image))
        return if (labels.isNotEmpty()) {
            labels[0].text
        } else {
            "UNKNOWN"
        }
    }

    fun addItem(item: String) {
        val newList = _itemsList.value?.toMutableList() ?: mutableListOf()
        if (!newList.contains(item)) {
            newList.add(item)
            _itemsList.value = newList
        }
    }

    fun removeItem(position: Int) {
        val currentList = _itemsList.value?.toMutableList() ?: mutableListOf()
        if (position in currentList.indices) {
            currentList.removeAt(position)
            _itemsList.value = currentList
        }
    }

    fun cleanup() {
        coroutineScope.cancel()
    }
}