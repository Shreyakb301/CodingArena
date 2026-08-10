package com.codingarena.core.common

import kotlin.random.Random

/**
 * Id source for locally created rows.
 *
 * Ids are generated on-device because attempts are written offline and only
 * later pushed to the backend; the client has to be able to name a row before
 * the server has ever seen it.
 */
interface IdGenerator {
    fun newId(): String
}

class RandomIdGenerator(
    private val random: Random = Random.Default,
) : IdGenerator {
    override fun newId(): String = buildString(ID_LENGTH) {
        repeat(ID_LENGTH) { append(ALPHABET[random.nextInt(ALPHABET.length)]) }
    }

    private companion object {
        const val ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz"
        const val ID_LENGTH = 24
    }
}

/** Test double: predictable, ordered ids. */
class SequentialIdGenerator(private val prefix: String = "id") : IdGenerator {
    private var counter = 0
    override fun newId(): String = "$prefix-${++counter}"
}
