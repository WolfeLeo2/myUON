package com.wolfeleo2.myuon.data.model

import kotlinx.serialization.Serializable
import java.security.MessageDigest

@Serializable
enum class AvatarProvider(val displayName: String, val tagLine: String) {
    DICEBEAR_PORTRAITS("Illustrated Portraits", "Hand-drawn vector characters and artistic portraits"),
    DICEBEAR_CREATURES("Robots & Pixel Art", "Modular robots, 8-bit arcade characters and emojis"),
    GRAVATAR("Gravatar Classic", "Automattic's procedural cartoon faces and monster avatars"),
    ROBOHASH("RoboHash Studio", "Procedural robotic, monster, and feline designs")
}

@Serializable
data class AvatarConfig(
    val provider: AvatarProvider = AvatarProvider.DICEBEAR_PORTRAITS,
    val style: String = "notionists"
) {
    fun buildUrl(seed: String): String {
        val cleanSeed = seed.trim().ifBlank { "UON-STUDENT" }
        return when (provider) {
            AvatarProvider.DICEBEAR_PORTRAITS,
            AvatarProvider.DICEBEAR_CREATURES -> {
                "https://api.dicebear.com/9.x/$style/svg?seed=$cleanSeed"
            }
            AvatarProvider.GRAVATAR -> {
                val hash = md5(cleanSeed.lowercase())
                "https://www.gravatar.com/avatar/$hash?d=$style&f=y"
            }
            AvatarProvider.ROBOHASH -> {
                "https://robohash.org/$cleanSeed.png?set=$style"
            }
        }
    }

    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}

data class AvatarStyleOption(
    val provider: AvatarProvider,
    val styleKey: String,
    val displayName: String,
    val description: String
)

object AvatarCatalog {
    val options: List<AvatarStyleOption> = listOf(
        // Illustrated Portraits (DiceBear)
        AvatarStyleOption(
            provider = AvatarProvider.DICEBEAR_PORTRAITS,
            styleKey = "notionists",
            displayName = "Notionists",
            description = "Clean, hand-drawn Notion-inspired character portraits"
        ),
        AvatarStyleOption(
            provider = AvatarProvider.DICEBEAR_PORTRAITS,
            styleKey = "avataaars",
            displayName = "Avataaars",
            description = "Expressive comic style avatars with customizable outfits by Pablo Stanley"
        ),
        AvatarStyleOption(
            provider = AvatarProvider.DICEBEAR_PORTRAITS,
            styleKey = "open-peeps",
            displayName = "Open Peeps",
            description = "Hand-drawn diverse human character illustrations by Pablo Stanley"
        ),
        AvatarStyleOption(
            provider = AvatarProvider.DICEBEAR_PORTRAITS,
            styleKey = "micah",
            displayName = "Micah",
            description = "Soft minimalist contemporary vector faces by Micah Lanier"
        ),
        AvatarStyleOption(
            provider = AvatarProvider.DICEBEAR_PORTRAITS,
            styleKey = "lorelei",
            displayName = "Lorelei",
            description = "Sleek and elegant modern line portraits"
        ),
        AvatarStyleOption(
            provider = AvatarProvider.DICEBEAR_PORTRAITS,
            styleKey = "adventurer",
            displayName = "Adventurer",
            description = "Fantasy RPG hero & quest character designs"
        ),
        AvatarStyleOption(
            provider = AvatarProvider.DICEBEAR_PORTRAITS,
            styleKey = "personas",
            displayName = "Personas",
            description = "Playful abstract character silhouettes by Draftbit"
        ),
        AvatarStyleOption(
            provider = AvatarProvider.DICEBEAR_PORTRAITS,
            styleKey = "miniavs",
            displayName = "Miniavs",
            description = "Cute miniature rounded character avatars"
        ),
        AvatarStyleOption(
            provider = AvatarProvider.DICEBEAR_PORTRAITS,
            styleKey = "croodles",
            displayName = "Croodles",
            description = "Quirky doodle sketch portraits"
        ),

        // Robots & Pixel Art (DiceBear)
        AvatarStyleOption(
            provider = AvatarProvider.DICEBEAR_CREATURES,
            styleKey = "bottts",
            displayName = "Bottts",
            description = "Vibrant modular robot illustrations by Pablo Stanley"
        ),
        AvatarStyleOption(
            provider = AvatarProvider.DICEBEAR_CREATURES,
            styleKey = "bottts-neutral",
            displayName = "Bottts Neutral",
            description = "Monochrome outline mechanical bots"
        ),
        AvatarStyleOption(
            provider = AvatarProvider.DICEBEAR_CREATURES,
            styleKey = "pixel-art",
            displayName = "Pixel Art",
            description = "Retro 8-bit arcade style characters"
        ),
        AvatarStyleOption(
            provider = AvatarProvider.DICEBEAR_CREATURES,
            styleKey = "thumbs",
            displayName = "Thumbs",
            description = "Playful round thumb-shaped doodle faces"
        ),
        AvatarStyleOption(
            provider = AvatarProvider.DICEBEAR_CREATURES,
            styleKey = "fun-emoji",
            displayName = "Fun Emoji",
            description = "Quirky animated sticker emojis"
        ),

        // Gravatar Classic
        AvatarStyleOption(
            provider = AvatarProvider.GRAVATAR,
            styleKey = "wavatar",
            displayName = "Wavatar Cartoon",
            description = "Automattic's procedural cartoon faces with distinct hairstyles and expressions"
        ),
        AvatarStyleOption(
            provider = AvatarProvider.GRAVATAR,
            styleKey = "monsterid",
            displayName = "MonsterID",
            description = "Procedurally generated colorful pixel monster characters"
        ),
        AvatarStyleOption(
            provider = AvatarProvider.GRAVATAR,
            styleKey = "retro",
            displayName = "Retro 8-Bit",
            description = "Classic 8-bit pixel arcade face sprites"
        ),

        // RoboHash Studio
        AvatarStyleOption(
            provider = AvatarProvider.ROBOHASH,
            styleKey = "set1",
            displayName = "Classic Robots",
            description = "Procedural mechanical robots generated from your student ID"
        ),
        AvatarStyleOption(
            provider = AvatarProvider.ROBOHASH,
            styleKey = "set2",
            displayName = "Friendly Monsters",
            description = "Colorful quirky monster character illustrations"
        ),
        AvatarStyleOption(
            provider = AvatarProvider.ROBOHASH,
            styleKey = "set3",
            displayName = "Disembodied Heads",
            description = "Stylized robot and alien portrait heads"
        ),
        AvatarStyleOption(
            provider = AvatarProvider.ROBOHASH,
            styleKey = "set4",
            displayName = "Retro Kittens",
            description = "Procedurally generated feline avatar characters"
        )
    )
}
