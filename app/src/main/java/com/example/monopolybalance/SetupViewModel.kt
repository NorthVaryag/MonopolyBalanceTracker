package com.example.monopolybalance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class SetupViewModel : ViewModel() {
    //Инициализация списка имен игроков
    private val _playerNames = MutableLiveData<MutableList<String>>(mutableListOf())
    val playerNames: LiveData<MutableList<String>> = _playerNames

    //Свойство для отслеживания количества игроков
    val playerCount: LiveData<Int> = _playerNames.map { it.size }

    //проверка возможности добавления игрока
    fun canAddPlayer(): Boolean {
        return _playerNames.value?.size ?: 0 < 4
    }

    //добавление игрока
    fun addPlayer(name: String): Boolean{
        if (!canAddPlayer() || name.isBlank())
            return false

        val currentList = _playerNames.value ?: mutableListOf()
        currentList.add(name)
        _playerNames.value = currentList

        return true
    }

    //проверка возможности игры
    fun canStartGame(): Boolean {
        return (_playerNames.value?.size ?: 0) >= 2
    }

    //получение всех игроков для передачи во второй экран
    fun getPlayersForGame(): List<Player> {
        return _playerNames.value?.mapIndexed { index, name ->
            Player(id = index + 1, name = name, balance = 1500)
        } ?: emptyList()
    }
}