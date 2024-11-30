package it.fast4x.rimusic.enums

enum class ThumbnailCoverType {
    Vinyl,
    CD;

    val textName: String
        get() = when (this) {
            Vinyl -> "Vinyl"
            CD -> "CD"
        }
}