// port-lint: source format_description/owned_format_item.rs
package io.github.kotlinmania.time.formatdescription

import io.github.kotlinmania.time.error.DifferentVariant
import io.github.kotlinmania.time.formatting.Formattable
import io.github.kotlinmania.time.parsing.Parsable

/**
 * A complete description of how to format and parse a type.
 */
sealed class OwnedFormatItem : Formattable, Parsable {
    /**
     * Bytes that are formatted as-is.
     *
     * **Note**: These bytes **should** be UTF-8, but are not required to be.
     */
    class Literal(
        val bytes: ByteArray,
    ) : OwnedFormatItem() {
        constructor(str: String) : this(str.encodeToByteArray())

        override fun equals(other: Any?): Boolean =
            other is Literal && bytes.contentEquals(other.bytes)

        override fun hashCode(): Int = bytes.contentHashCode()

        override fun toString(): String = bytes.decodeToString()
    }

    /** A minimal representation of a single non-literal item. */
    data class Component(
        val component: io.github.kotlinmania.time.formatdescription.Component,
    ) : OwnedFormatItem() {
        override fun toString(): String = component.toString()
    }

    /**
     * A series of literals or components that collectively form a partial or complete
     * description.
     */
    class Compound(
        val items: List<OwnedFormatItem>,
    ) : OwnedFormatItem() {
        constructor(items: Array<OwnedFormatItem>) : this(items.toList())

        override fun equals(other: Any?): Boolean =
            other is Compound && items == other.items

        override fun hashCode(): Int = items.hashCode()

        override fun toString(): String = items.toString()
    }

    /**
     * A `FormatItem` that may or may not be present when parsing. If parsing fails, there
     * will be no effect on the resulting `struct`.
     *
     * This variant has no effect on formatting, as the value is guaranteed to be present.
     */
    data class Optional(
        val item: OwnedFormatItem,
    ) : OwnedFormatItem() {
        override fun toString(): String = "Optional($item)"
    }

    /**
     * A series of `FormatItem`s where, when parsing, the first successful parse is used. When
     * formatting, the first element of the list is used. An empty list is a no-op when
     * formatting or parsing.
     */
    class First(
        val items: List<OwnedFormatItem>,
    ) : OwnedFormatItem() {
        constructor(items: Array<OwnedFormatItem>) : this(items.toList())

        override fun equals(other: Any?): Boolean =
            other is First && items == other.items

        override fun hashCode(): Int = items.hashCode()

        override fun toString(): String = "First($items)"
    }

    fun tryIntoComponent(): Result<io.github.kotlinmania.time.formatdescription.Component> =
        when (this) {
            is Component -> Result.success(component)
            else -> Result.failure(DifferentVariant())
        }

    fun tryIntoCompound(): Result<List<OwnedFormatItem>> =
        when (this) {
            is Compound -> Result.success(items)
            else -> Result.failure(DifferentVariant())
        }

    fun equalsComponent(rhs: io.github.kotlinmania.time.formatdescription.Component): Boolean =
        this is Component && component == rhs

    fun equalsSlice(rhs: List<OwnedFormatItem>): Boolean =
        this is Compound && items == rhs

    companion object {
        fun from(item: BorrowedFormatItem): OwnedFormatItem =
            when (item) {
                is BorrowedFormatItem.Literal -> Literal(item.bytes.copyOf())
                is BorrowedFormatItem.Component -> Component(item.component)
                is BorrowedFormatItem.Compound -> Compound(item.items.map { from(it) })
                is BorrowedFormatItem.Optional -> Optional(from(item.item))
                is BorrowedFormatItem.First -> First(item.items.map { from(it) })
            }

        fun from(items: List<BorrowedFormatItem>): OwnedFormatItem =
            Compound(items.map { from(it) })

        fun from(component: io.github.kotlinmania.time.formatdescription.Component): OwnedFormatItem =
            Component(component)

        fun fromOwnedItems(items: List<OwnedFormatItem>): OwnedFormatItem =
            Compound(items)
    }
}

