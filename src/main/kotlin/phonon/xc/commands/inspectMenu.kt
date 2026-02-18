/**
 * Command to toggle aim down sights usage.
 */

package phonon.xc.commands

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import phonon.xc.XC
import phonon.xc.util.Message
import phonon.xc.util.createCustomItemGui


private val AIM_DOWN_SIGHTS_SETTINGS = listOf(
    "menu",
)

public class InspectCommand(
    val xc: XC,
): CommandExecutor, TabCompleter {


    override fun onCommand(sender: CommandSender, cmd: Command, commandLabel: String, args: Array<String>): Boolean {

        val player = if ( sender is Player ) sender else null

        if ( player == null ) {
            Message.error(sender, "You must be a player ingame to use this command.")
            return true
        }

        // no args, print help
        if ( args.size == 0 ) {
            this.printHelp(sender)
            return true
        }
        val page = if ( args.size > 1 ) {
            (args[1].toIntOrNull() ?: 1) - 1
        } else {
            0
        }

        if ( page < 0 ) {
            Message.error(sender, "Page must be >= 1.")
            return true
        }

        // parse subcommand
        val arg = args[0].lowercase()
        when ( arg ) {
            "menu" -> player.openInventory(xc.createCustomItemGui("${ChatColor.GOLD}Inspect Menu",xc.storage.gunIds, xc.storage.gun, page).inventory)
            else -> {
                printHelp(sender)
                return true
            }
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        return AIM_DOWN_SIGHTS_SETTINGS
    }

    private fun printHelp(sender: CommandSender?) {
        Message.print(sender, "/inspect menu {page}")
        return
    }

}
