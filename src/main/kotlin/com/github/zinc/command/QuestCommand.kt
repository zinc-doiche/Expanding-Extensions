package com.github.zinc.command

import com.github.zinc.info
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class QuestCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            return false
        }
        info(sender.name)
//        sender.openFrame(QuestFx.getQuestMainFx(PlayerContainer[sender.name]!!))

        return true
    }
}