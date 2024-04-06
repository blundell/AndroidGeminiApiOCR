package com.blundell.devpost

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.blundell.devpost.ui.theme.BlundellTheme
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.ResponseStoppedException
import com.google.ai.client.generativeai.type.ServerException
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private val modelName = listOf(
    "gemini-pro",
    "gemini-pro-vision",
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BlundellTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Gemini Android")
                        Image(
                            painterResource(id = R.drawable.image),
                            contentDescription = "",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        var answer by remember { mutableStateOf("") }
                        Button(onClick = {
                            answer = "loading..."
                            CoroutineScope(
                                context = Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
                                    throwable.printStackTrace()
                                },
                            ).launch {
                                answer = analyseImage(
                                    image = BitmapFactory.decodeResource(resources, R.drawable.image),
                                    question = "Extract the " +
                                            "power consumption (in watts), " +
                                            "temperature (in degrees celsius) " +
                                            "and relative humidity (RH %) from the image. " +
                                            "Answer with a JSON object."
                                )
                            }
                        }) {
                            Text(text = "Analyse with Gemini")
                        }
                        Text(
                            text = answer,
                        )
                    }
                }
            }
        }
    }

    private suspend fun analyseImage(image: Bitmap, question: String): String {
        val generativeModel = GenerativeModel(
            modelName = modelName[1],
            apiKey = BuildConfig.apiKey,
        )
        val inputContent = content {
            image(image)
            text(question)
        }
        Log.d("TUT", "Generating content")
        try {
            val response = generativeModel.generateContent(inputContent)
            Log.e("TUT", "${response.text}")
            return response.text ?: "No answer."
        } catch (e: ResponseStoppedException) {
            Log.e("TUT", "Caught 'A request was stopped during generation for some reason.'", e)
            return "Error ${e.message}"
        } catch (e: ServerException) {
            Log.e("TUT", "Caught 'The server responded with a non 200 response code.'", e)
            return "Error ${e.message}"
        } catch (e: Exception) {
            Log.e("TUT", "Caught 'General Exception'", e)
            throw e
        }
    }
}