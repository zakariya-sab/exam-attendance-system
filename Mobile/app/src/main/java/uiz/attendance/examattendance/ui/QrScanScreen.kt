package uiz.attendance.examattendance.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response
import uiz.attendance.examattendance.model.ErrorResponse
import uiz.attendance.examattendance.model.ExamBlock
import uiz.attendance.examattendance.model.PresentStudentDto
import uiz.attendance.examattendance.model.ScanRequest
import uiz.attendance.examattendance.network.RetrofitClient
import uiz.attendance.examattendance.network.isNetworkConnected
import java.io.IOException
import java.util.concurrent.Executors

private const val SCAN_REJECTED_FALLBACK_MESSAGE = "Scan refused. Try again or check the exam block."
private const val REJECTED_BANNER_MILLIS = 3000L

private sealed class ScanOutcome {
    object Loading : ScanOutcome()
    data class Present(val name: String) : ScanOutcome()
    data class AlreadyPresent(val name: String) : ScanOutcome()
    data class Rejected(val message: String) : ScanOutcome()
    object NoConnection : ScanOutcome()
    object ServerUnreachable : ScanOutcome()
    object InvalidQr : ScanOutcome()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScanScreen(examBlock: ExamBlock, modifier: Modifier = Modifier, onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var scanOutcome by remember { mutableStateOf<ScanOutcome?>(null) }
    val isPaused = remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = examBlock.moduleName, style = MaterialTheme.typography.titleLarge)
                        Text(text = examBlock.sessionType, style = MaterialTheme.typography.bodyMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (hasCameraPermission) {
                CameraPreviewWithBarcodeScanning(
                    modifier = Modifier.fillMaxSize(),
                    isPaused = isPaused,
                    onBarcodeDetected = { codeApogee ->
                        isPaused.value = true
                        if (codeApogee.isBlank()) {
                            scanOutcome = ScanOutcome.InvalidQr
                        } else {
                            scanOutcome = ScanOutcome.Loading
                            coroutineScope.launch {
                                scanOutcome = performScan(context, examBlock.id, codeApogee)
                            }
                        }
                    }
                )

                val outcome = scanOutcome
                if (outcome is ScanOutcome.Rejected) {
                    LaunchedEffect(outcome) {
                        // A rejection is the server refusing the scan (e.g. exam not in
                        // progress) rather than a network/local failure, so the proctor must be
                        // able to scan the next student immediately: resume analysis as soon as
                        // the outcome is known, without waiting for a tap, then let the banner
                        // clear itself after a few seconds.
                        isPaused.value = false
                        delay(REJECTED_BANNER_MILLIS)
                        if (scanOutcome === outcome) {
                            scanOutcome = null
                        }
                    }
                    RejectedBanner(message = outcome.message)
                } else {
                    outcome?.let {
                        ScanOutcomeOverlay(
                            outcome = it,
                            onScanNext = {
                                scanOutcome = null
                                isPaused.value = false
                            }
                        )
                    }
                }
            } else {
                Text(text = "Camera permission is required to scan QR codes.")
            }
        }
    }
}

private suspend fun performScan(context: Context, examBlockId: Long, codeApogee: String): ScanOutcome {
    if (!isNetworkConnected(context)) {
        return ScanOutcome.NoConnection
    }

    return try {
        val response = RetrofitClient.apiService.scanAttendance(ScanRequest(codeApogee, examBlockId))
        val student = response.body()
        when (response.code()) {
            201 -> student?.let { ScanOutcome.Present("${it.firstName} ${it.lastName}") }
                ?: ScanOutcome.Rejected(SCAN_REJECTED_FALLBACK_MESSAGE)
            200 -> student?.let { ScanOutcome.AlreadyPresent("${it.firstName} ${it.lastName}") }
                ?: ScanOutcome.Rejected(SCAN_REJECTED_FALLBACK_MESSAGE)
            else -> ScanOutcome.Rejected(extractServerErrorMessage(response) ?: SCAN_REJECTED_FALLBACK_MESSAGE)
        }
    } catch (e: IOException) {
        ScanOutcome.ServerUnreachable
    }
}

private fun extractServerErrorMessage(response: Response<PresentStudentDto>): String? =
    runCatching { response.errorBody()?.string() }
        .getOrNull()
        ?.let { body -> runCatching { Gson().fromJson(body, ErrorResponse::class.java)?.error }.getOrNull() }
        ?.takeIf { it.isNotBlank() }

@Composable
private fun ScanOutcomeOverlay(outcome: ScanOutcome, onScanNext: () -> Unit) {
    if (outcome is ScanOutcome.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val (backgroundColor, message) = when (outcome) {
        ScanOutcome.Loading -> return
        is ScanOutcome.Present -> MaterialTheme.colorScheme.primaryContainer to "Present: ${outcome.name}"
        is ScanOutcome.AlreadyPresent -> MaterialTheme.colorScheme.secondaryContainer to "Already marked present: ${outcome.name}"
        // Not reached in practice: Rejected is routed to RejectedBanner instead (see QrScanScreen),
        // this branch only exists to keep the `when` exhaustive over the full sealed class.
        is ScanOutcome.Rejected -> MaterialTheme.colorScheme.errorContainer to outcome.message
        ScanOutcome.NoConnection -> MaterialTheme.colorScheme.errorContainer to "No internet connection"
        ScanOutcome.ServerUnreachable -> MaterialTheme.colorScheme.errorContainer to "Server unreachable"
        ScanOutcome.InvalidQr -> MaterialTheme.colorScheme.errorContainer to "Invalid QR code"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, style = MaterialTheme.typography.headlineSmall)
        Button(onClick = onScanNext) {
            Text(text = "Scan Next")
        }
    }
}

@Composable
private fun RejectedBanner(message: String) {
    // Deliberately not a full-screen block like ScanOutcomeOverlay: the camera preview must stay
    // visible underneath so the proctor can keep aiming at the next student while this is shown,
    // since scanning has already resumed (see the LaunchedEffect in QrScanScreen).
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 4.dp
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun CameraPreviewWithBarcodeScanning(
    modifier: Modifier = Modifier,
    isPaused: MutableState<Boolean>,
    onBarcodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val cameraProviderState = remember { mutableStateOf<ProcessCameraProvider?>(null) }
    DisposableEffect(Unit) {
        onDispose {
            cameraProviderState.value?.unbindAll()
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                cameraProviderState.value = cameraProvider

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(
                            cameraExecutor,
                            BarcodeAnalyzer(isPaused, onBarcodeDetected)
                        )
                    }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

private class BarcodeAnalyzer(
    private val isPaused: MutableState<Boolean>,
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    override fun analyze(imageProxy: ImageProxy) {
        if (isPaused.value) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.rawValue?.let { value ->
                    onBarcodeDetected(value)
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
