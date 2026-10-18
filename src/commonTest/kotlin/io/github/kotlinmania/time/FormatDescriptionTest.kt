// port-lint: tests tests/integration/format_description.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.formatdescription.BorrowedFormatItem
import io.github.kotlinmania.time.formatdescription.Component
import io.github.kotlinmania.time.formatdescription.OwnedFormatItem
import io.github.kotlinmania.time.formatdescription.modifier.Year
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FormatDescriptionTest {
    @Test
    fun borrowedFormatItemComponentConversions() {
        val component = Component.Year(Year.default())
        val item = BorrowedFormatItem.from(component)
        assertTrue(item is BorrowedFormatItem.Component && item.component == component)
        assertEquals(component, Component.tryFrom(item).getOrThrow())
        assertTrue(Component.tryFrom(BorrowedFormatItem.Literal(ByteArray(0))).isFailure)
        assertTrue(BorrowedFormatItem.Literal(ByteArray(0)).tryIntoCompound().isFailure)
    }

    @Test
    fun borrowedFormatItemCompoundConversions() {
        val compound = listOf(BorrowedFormatItem.Literal(ByteArray(0)))
        val item = BorrowedFormatItem.from(compound)
        assertTrue(item is BorrowedFormatItem.Compound && item.items == compound)
        assertEquals(compound, item.tryIntoCompound().getOrThrow())
    }

    @Test
    fun borrowedFormatItemEquality() {
        val component = Component.Year(Year.default())
        val compound = listOf(BorrowedFormatItem.Literal(ByteArray(0)))
        val componentItem = BorrowedFormatItem.from(component)
        val compoundItem = BorrowedFormatItem.from(compound)

        assertTrue(componentItem.equalsComponent(component))
        assertTrue(compoundItem.equalsSlice(compound))
    }

    @Test
    fun ownedFormatItemComponentConversions() {
        val component = Component.Year(Year.default())
        val item = OwnedFormatItem.from(component)
        assertTrue(item is OwnedFormatItem.Component && item.component == component)
        assertEquals(component, Component.tryFrom(item).getOrThrow())
        assertTrue(Component.tryFrom(OwnedFormatItem.Literal(ByteArray(0))).isFailure)
        assertTrue(OwnedFormatItem.Literal(ByteArray(0)).tryIntoCompound().isFailure)
    }

    @Test
    fun ownedFormatItemCompoundConversions() {
        val compound = listOf(OwnedFormatItem.Literal(ByteArray(0)))
        val item = OwnedFormatItem.fromOwnedItems(compound)
        assertTrue(item is OwnedFormatItem.Compound && item.items == compound)
        assertEquals(compound, item.tryIntoCompound().getOrThrow())
    }

    @Test
    fun ownedFormatItemEquality() {
        val component = Component.Year(Year.default())
        val compound = OwnedFormatItem.from(listOf(BorrowedFormatItem.Literal(ByteArray(0))))
        val componentItem = OwnedFormatItem.from(component)

        assertTrue(componentItem.equalsComponent(component))
        assertTrue(compound.equalsSlice(listOf(OwnedFormatItem.Literal(ByteArray(0)))))
    }
}
