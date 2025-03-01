package world.gregs.voidps.engine.inv.transact

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.InventoryUpdate
import world.gregs.voidps.engine.inv.ItemChanged
import java.util.*

/**
 * Tracks the changes made to the inventory and allows for sending these changes to the appropriate recipients.
 */
class ChangeManager(
    private val inventory: Inventory
) {
    private val changes: Stack<ItemChanged> = Stack()
    private val events = mutableSetOf<Events>()

    /**
     * Track a change of an item in the inventory.
     * @param from the inventory id the item is from
     * @param index the index of the item in the inventory
     * @param previous the previous state of the item
     * @param to the inventory id the item going to
     * @param item the current state of the item
     */
    fun track(from: String, index: Int, previous: Item, to: String, item: Item) {
        changes.add(ItemChanged(inventory.id, index, previous, item, from, to))
    }

    /**
     * Adds [events] to the list of recipients of [ItemChanged] updates in this inventory.
     */
    fun bind(events: Events) {
        this.events.add(events)
    }

    /**
     * Removes [events] to the list of recipients of [ItemChanged] updates in this inventory.
     */
    fun unbind(events: Events) {
        this.events.remove(events)
    }

    /**
     * Send the tracked changes to the appropriate recipients.
     */
    fun send() {
        if (changes.isEmpty()) {
            return
        }
        val update = InventoryUpdate(inventory.id, changes)
        for (events in events) {
            events.emit(update)
            for (change in changes) {
                events.emit(change)
            }
        }
    }

    /**
     * Clear the tracked changes.
     */
    fun clear() {
        changes.clear()
    }
}