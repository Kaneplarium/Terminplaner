package com.terminplaner.util

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.nl.entityextraction.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MLManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val entityExtractor = EntityExtraction.getClient(
        EntityExtractorOptions.Builder(EntityExtractorOptions.GERMAN)
            .build()
    )

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractDateTime(text: String): Long? {
        try {
            entityExtractor.downloadModelIfNeeded().await()
            val params = EntityExtractionParams.Builder(text).build()
            val result = entityExtractor.annotate(params).await()
            
            for (entityAnnotation in result) {
                for (entity in entityAnnotation.entities) {
                    if (entity is DateTimeEntity) {
                        return entity.timestampMillis
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    suspend fun recognizeText(bitmap: Bitmap): String? {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = textRecognizer.process(image).await()
            result.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
