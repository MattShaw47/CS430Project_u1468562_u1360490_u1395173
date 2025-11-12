package com.example.drawingapp

import com.example.drawingapp.data.VisionResponse
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class VisionResponseParsingTest {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Test
    fun parses_object_label_text_annotations() {
        val payload = """
        {
          "responses": [{
            "localizedObjectAnnotations": [{
              "mid": "/m/01yrx",
              "name": "Cat",
              "score": 0.92,
              "boundingPoly": {
                "normalizedVertices": [
                  {"x":0.10,"y":0.20},
                  {"x":0.60,"y":0.20},
                  {"x":0.60,"y":0.70},
                  {"x":0.10,"y":0.70}
                ]
              }
            }],
            "labelAnnotations": [
              {"description":"pet","score":0.98},
              {"description":"animal","score":0.96}
            ],
            "textAnnotations": [
              {"description":"HELLO","score":0.80}
            ]
          }]
        }
        """.trimIndent()

        val parsed: VisionResponse = json.decodeFromString(payload)
        assertNotNull(parsed.responses)
        val r0 = parsed.responses!!.first()

        val obj = r0.localizedObjectAnnotations!!.first()
        assertEquals("Cat", obj.name)
        assertEquals(0.92f, obj.score!!, 1e-4f)
        assertEquals(4, obj.boundingPoly!!.normalizedVertices!!.size)

        val labels = r0.labelAnnotations!!
        assertEquals(2, labels.size)
        assertEquals("pet", labels[0].description)

        val texts = r0.textAnnotations!!
        assertEquals(1, texts.size)
        assertEquals("HELLO", texts[0].description)
    }
}
