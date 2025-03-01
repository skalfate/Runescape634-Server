package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ operate && target.id == "hamid" && option == "Talk-to" }) { player: Player ->
    player<Cheerful>("Hi!")
    npc<Talking>("Hello traveller. How can I be of assistance?")
    choice {
        option<Uncertain>("Can you heal me?") {
            npc<Talking>("You'd be better off speaking to one of the nurses.")
            npc<Cheerful>("They are so... nice... afterall!")
        }
        option<Uncertain>("What's a Monk doing in a place such as this?") {
            npc<Suspicious>("""
                Well don't tell anyone but I came here because of the
                nurses!
            """)
            player<Laugh>("Really?")
            npc<Laugh>("It beats being stuck in the monastery!")
        }
        option<Uncertain>("Which monastery do you come from?") {
            npc<Talking>("I belong to the monastery north of Falador.")
            player<Uncertain>("You're a long way from home?")
            npc<Sad>("Yeh. I miss the guys.")
        }
    }
}
