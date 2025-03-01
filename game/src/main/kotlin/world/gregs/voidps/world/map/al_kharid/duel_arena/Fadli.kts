package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.npc.shop.openShop

on<NPCOption>({ operate && target.id == "fadli" && option == "Talk-to" }) { player: Player ->
    player<Cheerful>("Hi.")
    npc<RollEyes>("What?")
    choice {
        option<Talking>("What do you do?") {
            npc<RollEyes>("""
			    You can store your stuff here if you want. You can
			    dump anything you don't want to carry whilst you're
		        fighting duels and then pick it up again on the way out.
			""")
            npc<RollEyes>("To be honest I'm wasted here.")
            npc<Angry>("""
			    I should be winning duels in an arena! I'm the best
			    warrior in Al Kharid!
			""")
            player<Uncertain>("Easy, tiger!")
        }
        option<Uncertain>("What is this place?") {
            npc<Angry>("Isn't it obvious?")
            npc<Talking>("This is the Duel Arena...duh!")
        }
        option<Talking>("I'd like to access my bank, please.") {
            npc<RollEyes>("Sure.")
            player.open("bank")
        }
        option<Cheerful>("I'd like to collect items.") {
            npc<RollEyes>("Yeah, okay.")
            player.open("collection_box")
        }
        option<Talking>("Do you watch any matches?") {
            npc<Talking>("When I can.")
            npc<Cheerful>("Most aren't any good so I throw rotten fruit at them!")
            player<Cheerful>("Heh. Can I buy some?")
            if (World.members) {
                npc<Laugh>("Sure.")
                player.openShop("shop_of_distaste")
                return@option
            }
            npc<RollEyes>("Nope.")
            player.message("You need to be on a members world to use this feature.")
        }
    }
}

on<NPCOption>({ operate && target.id == "fadli" && option == "Bank" }) { player: Player ->
    player.open("bank")
}

on<NPCOption>({ operate && target.id == "fadli" && option == "Collect" }) { player: Player ->
    player.open("collection_box")
}

on<NPCOption>({ operate && target.id == "fadli" && option == "Buy" }) { player: Player ->
    if (World.members) {
        player.openShop("shop_of_distaste")
        return@on
    }
    npc<RollEyes>("Sorry, I'm not interested.")
    player.message("You need to be on a members world to use this feature.")
}