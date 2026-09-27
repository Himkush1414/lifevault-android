package com.lifevault.app.core.files

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class VaultPathsTest {

    @Test
    fun `shard is the first two lowercase hex characters`() {
        assertEquals("a3", VaultPaths.shardOf("A3f1c9e2-1234-5678-9abc-def012345678"))
    }

    @Test
    fun `file path nests under the version and shard directories`() {
        val id = "a3f1c9e2-1234-5678-9abc-def012345678"
        assertEquals("v1/a3/$id.bin", VaultPaths.relativeFilePath(id))
    }

    @Test
    fun `thumb path uses the same shard with a suffix`() {
        val id = "a3f1c9e2-1234-5678-9abc-def012345678"
        assertEquals("v1/a3/${id}_thumb.bin", VaultPaths.relativeThumbPath(id))
    }

    @Test
    fun `an id shorter than two characters is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { VaultPaths.shardOf("a") }
    }
}
