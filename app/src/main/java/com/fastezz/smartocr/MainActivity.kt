package com.fastezz.smartocr

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions

class MainActivity : ComponentActivity() {
    private lateinit var resultBox: EditText
    private lateinit var status: TextView
    private val latinRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }
    private val devanagariRecognizer by lazy {
        TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
    }

    private val galleryPicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) recognizeUri(uri)
        }

    private val cameraPicker =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            if (bitmap != null) recognizeBitmap(bitmap)
            else status.text = "Camera capture cancelled."
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 20)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val title = TextView(this).apply {
            text = "FASTEZZ Smart OCR"
            textSize = 25f
            setPadding(0, 0, 0, 6)
        }
        val subtitle = TextView(this).apply {
            text = "Offline text recognition • Hindi & English"
            textSize = 14f
            setPadding(0, 0, 0, 16)
        }
        status = TextView(this).apply {
            text = "Choose Camera or Gallery to start."
            textSize = 14f
            setPadding(0, 0, 0, 10)
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(status)
        root.addView(makeButton("Scan with Camera") { cameraPicker.launch(null) })
        root.addView(makeButton("Choose Image / Screenshot") { galleryPicker.launch("image/*") })

        val resultLabel = TextView(this).apply {
            text = "Recognised text (you can edit it):"
            textSize = 16f
            setPadding(0, 16, 0, 8)
        }
        root.addView(resultLabel)

        resultBox = EditText(this).apply {
            hint = "OCR result will appear here…"
            gravity = android.view.Gravity.TOP or android.view.Gravity.START
            minLines = 8
            setSingleLine(false)
            setTextIsSelectable(true)
        }
        val scroll = ScrollView(this)
        scroll.addView(resultBox)
        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        root.addView(makeButton("Copy All Text") {
            val text = resultBox.text.toString()
            if (text.isBlank()) toast("There is no text to copy.")
            else {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("OCR text", text))
                toast("Text copied.")
            }
        })
        root.addView(makeButton("Share Text") {
            val text = resultBox.text.toString()
            if (text.isBlank()) toast("There is no text to share.")
            else {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                startActivity(Intent.createChooser(intent, "Share OCR text"))
            }
        })
        root.addView(makeButton("Save Text on Device") { saveText() })

        setContentView(root)
    }

    private fun makeButton(label: String, action: () -> Unit): Button {
        return Button(this).apply {
            text = label
            setOnClickListener { action() }
        }
    }

    private fun recognizeUri(uri: Uri) {
        status.text = "Reading image…"
        try {
            val image = InputImage.fromFilePath(this, uri)
            recognizeImage(image)
        } catch (e: Exception) {
            status.text = "Could not open image."
            toast("Unable to open this image.")
        }
    }

    private fun recognizeBitmap(bitmap: Bitmap) {
        status.text = "Reading camera image…"
        recognizeImage(InputImage.fromBitmap(bitmap, 0))
    }

    private fun recognizeImage(image: InputImage) {
        val latinTask = latinRecognizer.process(image)
        val hindiTask = devanagariRecognizer.process(image)
        latinTask.addOnSuccessListener { latin ->
            hindiTask.addOnSuccessListener { hindi ->
                val parts = listOf(latin.text.trim(), hindi.text.trim())
                    .filter { it.isNotBlank() }
                    .distinct()
                resultBox.setText(parts.joinToString("\n\n"))
                status.text = if (parts.isEmpty()) "No text found. Try a clearer image."
                    else "Recognition finished. Please check names and numbers."
            }.addOnFailureListener {
                resultBox.setText(latin.text)
                status.text = "English/common text recognised. Hindi recognition was unavailable."
            }
        }.addOnFailureListener {
            hindiTask.addOnSuccessListener { hindi ->
                resultBox.setText(hindi.text)
                status.text = "Hindi text recognised. Please verify the result."
            }.addOnFailureListener {
                status.text = "Text recognition failed. Try another image."
            }
        }
    }

    private fun saveText() {
        val text = resultBox.text.toString().trim()
        if (text.isBlank()) {
            toast("There is no text to save.")
            return
        }
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "FASTEZZ_OCR_${System.currentTimeMillis()}.txt")
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/FASTEZZ Smart OCR")
        }
        val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), values)
        if (uri == null) {
            toast("Could not create a file.")
            return
        }
        try {
            contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use {
                it.write(text)
            }
            toast("Saved in Documents/FASTEZZ Smart OCR.")
        } catch (e: Exception) {
            contentResolver.delete(uri, null, null)
            toast("Save failed.")
        }
    }

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    override fun onDestroy() {
        super.onDestroy()
        latinRecognizer.close()
        devanagariRecognizer.close()
    }
}
