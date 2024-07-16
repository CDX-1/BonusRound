package net.cdx.bonusround.config

import net.cdx.bonusround.Main
import net.cdx.bonusround.config.serializers.Title
import org.spongepowered.configurate.objectmapping.ConfigSerializable

fun lang(): Lang {
    return Main.lang
}

@ConfigSerializable
class Lang {

    var general = General()

    @ConfigSerializable
    class General {

        var prefix: String = "<color:#DB2B39><bold>B<color:#F0CEA0><bold>R<reset> <dark_gray>»<reset><gray>"
        var joinUnique: String = "<dark_gray>[<reset><green>+</green><dark_gray>]<reset> <gray>%player_name%<reset> <yellow>(#%server_unique_joins%)</yellow>"
        var join: String = "<dark_gray>[<reset><green>+</green><dark_gray>]<reset> <gray>%player_name%"
        var quit: String = "<dark_gray>[<reset><red>-</red><dark_gray>]<reset> <gray>%player_name%"
        var unknownCommand: String = "That command doesn't seem to exist! Try <color:#DB2B39><click:run_command:/help>/help!"

    }

    var commands = Commands()

    @ConfigSerializable
    class Commands {

        var queue = Queue()

        @ConfigSerializable
        class Queue {

            var noSubCommands: String = "You must select a gamemode!"
            var mustLeaveQueue: String = "You're already in a queue!"
            var joinedQueue: String = "Successfully joined queue: <color:#DB2B39>%0"
            var notInQueue: String = "You're not in a queue!"
            var leftQueue: String = "Successfully left queue!"

        }

        var discord = Discord()

        @ConfigSerializable
        class Discord {

            var serverInvite: String = "<click:open_url:https://discord.gg/naQRPHEYP7>Click here to join our official Discord server!"

        }

    }

    var games = Games()

    @ConfigSerializable
    class Games {

        var general = General()

        @ConfigSerializable
        class General {

            var matchFoundTitle: Title = Title.of("<color:#DB2B39><bold>ᴍᴀᴛᴄʜ ғᴏᴜɴᴅ", "<gray>Loading game...")
            var searchingForArena: String = "Searching for arena..."
            var arenaFound: String = "Found arena: <color:#DB2B39>%0"
            var arenaSearchTimeout: String = "You've been removed from the arena queue because your search took longer than %0 minutes"
            var arenaInstanceTimeout: String = "Your arena has timed out, this is likely a bug"

        }

    }

}