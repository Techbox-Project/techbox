package io.github.techbox.utils

import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path


object FileUtils {
    val jsonMapper: ObjectMapper = ObjectMapper()
        .configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true)
        .enable(SerializationFeature.INDENT_OUTPUT)

    fun read(path: Path?): String? {
        return Files.readString(path, StandardCharsets.UTF_8)
    }

    fun write(path: Path?, contents: String?) {
        Files.writeString(path, contents, StandardCharsets.UTF_8)
    }
}