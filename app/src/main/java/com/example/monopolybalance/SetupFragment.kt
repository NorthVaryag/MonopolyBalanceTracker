package com.example.monopolybalance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController

class SetupFragment : Fragment() {

    private lateinit var etPlayerName: EditText
    private lateinit var btnAddPlayer: Button
    private lateinit var btnStartGame: Button
    private lateinit var lvPlayers: ListView
    private lateinit var tvPlayerCount: TextView

    companion object {
        fun newInstance() = SetupFragment()
    }

    private val viewModel: SetupViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_setup, container, false)
        initializeViews(view)
        setupObservers()
        setupAddPlayerListener()
        setupStartGameListener()

        return view
    }

    private val adapter by lazy {
        ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, mutableListOf<String>())
    }

    private fun initializeViews(view: View) {
        etPlayerName = view.findViewById(R.id.etPlayerName)
        btnAddPlayer = view.findViewById(R.id.btnAddPlayer)
        btnStartGame = view.findViewById(R.id.btnStartGame)
        lvPlayers = view.findViewById(R.id.lvPlayers)
        tvPlayerCount = view.findViewById(R.id.tvPlayerCount)

        lvPlayers.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.playerNames.observe(viewLifecycleOwner) { names ->
            adapter.clear()
            adapter.addAll(names)
            adapter.notifyDataSetChanged()
        }

        viewModel.playerCount.observe(viewLifecycleOwner) { count ->
            tvPlayerCount.text = "Добавлено игроков: $count"
            btnStartGame.isEnabled = viewModel.canStartGame()
        }
    }

    private fun setupAddPlayerListener() {
        btnAddPlayer.setOnClickListener {
            val name = etPlayerName.text.toString().trim()

            if (viewModel.addPlayer(name)) {
                etPlayerName.text.clear()
                Toast.makeText(context, "Игрок добавлен", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Невозможно добавить игрока", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupStartGameListener() {
        btnStartGame.setOnClickListener {
            val players = viewModel.getPlayersForGame()

            val bundle = Bundle().apply {
                putParcelableArrayList("players", ArrayList(players))
            }

            findNavController().navigate(
                R.id.action_setup_to_game,
                bundle
            )
        }
    }
}
