package world.gregs.voidps.world.map.lumbridge


import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.world.activity.bank.hasBanked
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.activity.quest.refreshQuestJournal
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.*

on<NPCOption>({ operate && target.id == "duke_horacio" && option == "Talk-to" }) { player: Player ->
    npc<Talking>("Greetings. Welcome to my castle.")
    when (player.quest("rune_mysteries")) {
        "unstarted" -> unstarted()
        "started" -> started()
        else -> completed()
    }
}

suspend fun CharacterContext.started() {
    choice {
        option<Unsure>("What did you want me to do again?") {
            if (player.hasBanked("air_talisman")) {
                npc<Talking>("""
                    Take that talisman I gave you to Sedridor at the
                    Wizards' Tower. You'll find it south west of here,
                    across the bridge from Draynor Village.
                """)
                player<Cheerful>("Okay, will do.")
                return@option
            }
            npc<Unsure>("Did you take that talisman to Sedridor?")
            player<Sad>("No, I lost it.")
            npc<Talking>("""
                Ah, well that explains things. One of my servants found
                it outside, and it seemed too much of a coincidence that
                another would suddenly show up.
            """)
            if (player.inventory.isFull()) {
                item("""
                    The Duke tries to hand you the talisman, but you don't
                    have enough room to take it.
                """, "air_talisman", 600)
                return@option
            }
            npc<Talking>("""
                Here, take it to the Wizards' Tower, south west of here.
                Please try not to lose it this time.
            """)
            player.inventory.add("air_talisman")
            item("The Duke hands you the talisman.", "air_talisman", 600)
        }
        findMoney()
    }
}

suspend fun CharacterContext.unstarted() {
    choice {
        option<Unsure>("Have you any quests for me?") {
            npc<Uncertain>("""
                Well, I wouldn't describe it as a quest, but there is
                something I could use some help with.
            """)
            player<Unsure>("What is it?")
            npc<Talking>("""
                We were recently sorting through some of the things
                stored down in the cellar, and we found this old
                talisman.
            """)
            item("The Duke shows you a talisman.", "air_talisman", 600)
            npc<Talking>("""
                The Order of Wizards over at the Wizards' Tower
                have been on the hunt for magical artefacts recently. I
                wonder if this might be just the kind of thing they're
                after.
            """)
            npc<Unsure>("Would you be willing to take it to them for me?")
            startQuest()
        }
        findMoney()
    }
}

suspend fun CharacterContext.completed() {
    choice {
        option<Unsure>("Have you any quests for me?") {
            npc<Talking>("""
                The only job I had was the delivery of that talisman, so
                I'm afraid not.
            """)
        }
        findMoney()
    }
}

suspend fun PlayerChoice.findMoney() : Unit = option<Unsure>("Where can I find money?") {
    npc<Talking>("""
        I've heard that the blacksmiths are prosperous amongst
        the peasantry. Maybe you could try your hand at
        that?
    """)
}

suspend fun CharacterContext.startQuest() {
    choice("Start the Rune Mysteries quest?") {
        option<Cheerful>("Sure, no problem.") {
            if (player.inventory.isFull()) {
                item("""
                            The Duke tries to hand you the talisman, but you don't
                            have enough room to take it.
                        """, "air_talisman", 600)
                return@option
            }
            player["rune_mysteries"] = "started"
            player.inventory.add("air_talisman")
            npc<Talking>("""
                        Thank you very much. You'll find the Wizards' Tower
                        south west of here, across the bridge from Draynor
                        Village. When you arrive, look for Sedridor. He is the
                        Archmage of the wizards there.
                    """)
            player.refreshQuestJournal()
            item("The Duke hands you the talisman.", "air_talisman", 600)
        }
        option<Talking>("Not right now.") {
            npc<Sad>("As you wish. Hopefully I can find someone else to help.")
        }
    }
}