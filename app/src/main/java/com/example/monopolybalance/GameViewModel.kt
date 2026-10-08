package com.example.monopolybalance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {
    //Хранилище списка игроков
    private val _players = MutableLiveData<List<Player>>()
    val players: LiveData<List<Player>> = _players

    //Текущий выбранный игрок
    private val _selectedPlayerId = MutableLiveData<Int>(1)
    val selectedPlayerId: LiveData<Int> = _selectedPlayerId

    //Получение текущего игрока
    fun getCurrentPlayer(): Player? {
        return _players.value?.find { it.id == _selectedPlayerId.value }
    }

    //Обновление выбранного игрока
    fun setSelectedPlayer(playerId: Int) {
        _selectedPlayerId.value = playerId
    }

    //Инициализация игры со списком игроков
    fun initializeGame(players: List<Player>) {
        _players.value = players
    }

    //Добавление денег игроку
    fun addMoney(amount: Int) {
        val currentPlayer = getCurrentPlayer() ?: return

        val updatedPlayers = _players.value?.map { player ->
            if (player.id == currentPlayer.id) {
                player.copy(balance = player.balance + amount)
            } else {
                player
            }
        }

        _players.value = updatedPlayers !!
    }

    //Перевод денег между игроками или в банк
    fun transferMoney(fromPlayerId: Int, toPlayerId: Int?, amount: Int): Boolean {
        val fromPlayer = _players.value?.find { it.id == fromPlayerId } ?: return false

        // Проверка достаточности средств
        if (fromPlayer.balance < amount) {
            return false
        }

        val updatedPlayers = _players.value?.map { player ->
            when {
                player.id == fromPlayerId -> {
                    player.copy(balance = player.balance - amount)
                }
                toPlayerId != null && player.id == toPlayerId -> {
                    player.copy(balance = player.balance + amount)
                }
                else -> player
            }
        }

        _players.value = updatedPlayers !!
        return true
    }

    //Получение списка имен для спиннера
    fun getPlayerNames(): List<String> {
        return _players.value?.map { it.name } ?: emptyList()
    }

    //Получение списка получателей для перевода
    fun getRecipientsList(): List<String> {
        val recipients = mutableListOf("Банк")
        recipients.addAll(getPlayerNames())
        return recipients
    }


}
