package com.github.zinc;

import com.github.zinc.command.StatusCommand
import com.github.zinc.command.QuestCommand
import com.github.zinc.command.TestCommand
import com.github.zinc.core.recipe.Recipes
import com.github.zinc.front.listener.*
import com.github.zinc.module.user.UserModule
import com.github.zinc.mongodb.MongoDB
import io.github.monun.heartbeat.coroutines.HeartbeatScope
import kotlinx.coroutines.*
import org.bukkit.command.CommandExecutor
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin;
import kotlin.coroutines.CoroutineContext

class ZincPlugin: JavaPlugin() {

    override fun onEnable() {
        plugin = this
        MongoDB.register()
        UserModule().run {
            registerCommands()
            registerListeners()
        }
        //QuestManager.registerAllQuestList()
//        registerAll(
//            ServerListener(),
//            PlayerExpListener(),
//            PlayerListener(),
//            QuestListener(),
//            PlayerToolListener(),
//            PlayerWorldListener()
//        )
//        executors(
//            "status" to StatusCommand(),
//            "test" to TestCommand(),
//            "quest" to QuestCommand()
//        )
//        QuestDAO().use(QuestDAO::questTimer)
//        Recipes.registerAll()
    }

    override fun onDisable() {
        ServerListener.execute("updateAll")
    }

    private fun registerAll(vararg listener: Listener) {
        listener.forEach { plugin.server.pluginManager.registerEvents(it, this) }
    }

    private fun executors(vararg executors: Pair<String, CommandExecutor>) {
        executors.forEach { plugin.getCommand(it.first)!!.setExecutor(it.second) }
    }
}
internal const val NAMESPACE = "zinc"
internal lateinit var plugin: JavaPlugin
internal fun info(msg: Any) { plugin.logger.info(msg.toString()) }
internal fun warn(msg: Any) { plugin.logger.warning(msg.toString()) }