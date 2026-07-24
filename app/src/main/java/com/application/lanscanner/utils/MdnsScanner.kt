package com.application.lanscanner.utils

import android.util.Log

object MDnsParser {

    /**
     * Parse UDP byte array to get Hostname of device.
     */
    fun extractHostname(buffer: ByteArray, packetLength: Int): String? {
        try {
            if (packetLength < 12) return null // check header length == 12

            var offset = 12 // start from first byte after DNS header

            Log.d("mdnsScanner", "response packet content: " + readName(buffer, offset))

            val appleService: Set<String> = setOf(
                "_airplay._tcp.local",
                "_companion-link._tcp.local",
                "_raop._tcp.local",
            )

            val serviceType = readName(buffer, offset)

            if(serviceType.startsWith("_googlecast")) {
                return "Google Cast Device (Android)"
            }
            else if(appleService.contains(serviceType)){
                return "Apple Device"
            }

            val qdCount = ((buffer[4].toInt() and 0xFF) shl 8) or (buffer[5].toInt() and 0xFF) // question (bytes 4-5)
            val anCount = ((buffer[6].toInt() and 0xFF) shl 8) or (buffer[7].toInt() and 0xFF) // answer (bytes 6-7)

            // skip Questions section
            for (i in 0 until qdCount) {
                offset = skipName(buffer, offset)
                offset += 4
            }

            // scan Answers to find hostname
            for (i in 0 until anCount) {
                offset = skipName(buffer, offset)

                val type = ((buffer[offset].toInt() and 0xFF) shl 8) or (buffer[offset + 1].toInt() and 0xFF)
                offset += 8

                val dataLen = ((buffer[offset].toInt() and 0xFF) shl 8) or (buffer[offset + 1].toInt() and 0xFF)
                offset += 2

                // Type 12 (PTR) or 33 (SRV) often contain hostname
                if (type == 12 || type == 33) {
                    val resolvedName = readName(buffer, offset)

                    // Filter out strings that are merely generic service names. (_http._tcp.local)
                    if (!resolvedName.startsWith("_")) {
                        return resolvedName
                            .replace(".local", "")
                            .replace("\\", "")
                            .trim()
                    }
                }
                offset += dataLen // jump to next Answer
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return null
    }

    /**
     * Domain Name reader which support DNS Pointer (string compression).
     */
    private fun readName(buffer: ByteArray, startOffset: Int): String {
        val sb = StringBuilder()
        var currentOffset = startOffset
        var maxJumps = 5 // Prevent infinite loops caused by spoofed packets.

        while (maxJumps > 0) {
            val len = buffer[currentOffset].toInt() and 0xFF
            if (len == 0) break

            // if first 2 bit = 11 (0xC0) => Pointer
            if ((len and 0xC0) == 0xC0) {
                val pointerOffset = ((len and 0x3F) shl 8) or (buffer[currentOffset + 1].toInt() and 0xFF)
                currentOffset = pointerOffset
                maxJumps--
                continue
            }

            // read label
            currentOffset++
            val label = String(buffer, currentOffset, len, Charsets.UTF_8)
            sb.append(label).append(".")
            currentOffset += len
        }

        return sb.toString().removeSuffix(".")
    }

    /**
     * skip a domain name function to move the offset cursor to the next field.
     */
    private fun skipName(buffer: ByteArray, startOffset: Int): Int {
        var currentOffset = startOffset
        while (true) {
            val len = buffer[currentOffset].toInt() and 0xFF
            if (len == 0) return currentOffset + 1

            if ((len and 0xC0) == 0xC0) return currentOffset + 2

            currentOffset += len + 1
        }
    }
}