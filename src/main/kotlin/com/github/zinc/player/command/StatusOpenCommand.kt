package com.github.zinc.player.command

import com.github.zinc.player.PlayerContainer
import com.github.zinc.player.domain.StatusType
import com.github.zinc.player.fx.StatusFx
import com.github.zinc.player.manager.PlayerStatusManager
import io.github.monun.invfx.openFrame
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

class StatusOpenCommand: TabExecutor {
    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): MutableList<String> {
        val list = ArrayList<String>()
        val player = if(sender is Player) sender else return list

        if(player.isOp) {
            when(args.size) {
                1 -> return StringUtil.copyPartialMatches(args[0], listOf("open", "view", "add"), list)
                2 -> if(args[0] == "add")
                    return StringUtil.copyPartialMatches(args[1], listOf("str", "swt", "bal", "con"), list)
            }
        } else {
            when(args.size) {
                1 -> return StringUtil.copyPartialMatches(args[0], listOf("open", "view"), list)
            }
        }
        return list
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            return false
        }
        when (args.size) {
            1 -> {
                when(args[0]) {
                    "open" -> {
                        sender.openFrame(StatusFx.getStatusFrame(sender))
                        return true
                    }
                    "view" -> {
                        val playerDTO = PlayerContainer[sender.name]!!
                        sender.sendMessage(
                            "${sender.name}의 스테이터스 :\n" +
                            "| Strength: ${playerDTO.playerStrength}\n" +
                            "| Swiftness: ${playerDTO.playerSwiftness}\n" +
                            "| Balance: ${playerDTO.playerBalance}\n" +
                            "| Concentration: ${playerDTO.playerConcentration}\n" +
                            "\n" +
                            "잔여스탯: ${playerDTO.playerStatusRemain}"
                        )
                        return true
                    }
                }
            }
        }
        if(sender.isOp) when (args.size) {
            //ex) add str 3
            3 -> {
                val amount = args[2].toIntOrNull() ?: return false
                val manager = PlayerStatusManager(PlayerContainer[sender.name]!!)
                when(args[1]) {
                    "str" -> {
                        manager.updateStatus(StatusType.STRENGTH to amount)
                        manager.applyStatus(StatusType.STRENGTH)
                        return true
                    }
                    "swt" -> {
                        manager.updateStatus(StatusType.SWIFTNESS to amount)
                        manager.applyStatus(StatusType.SWIFTNESS)
                        return true
                    }
                    "bal" -> {
                        manager.updateStatus(StatusType.BALANCE to amount)
                        manager.applyStatus(StatusType.BALANCE)
                        return true
                    }
                    "con" -> {
                        manager.updateStatus(StatusType.CONCENTRATION to amount)
                        manager.applyStatus(StatusType.CONCENTRATION)
                        return true
                    }
                }
            }
        }
        return false
    }
}