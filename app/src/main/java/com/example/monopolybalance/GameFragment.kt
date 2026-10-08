package com.example.monopolybalance

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels

class GameFragment : Fragment() {

    private lateinit var spinnerPlayers: Spinner
    private lateinit var spinnerRecipient: Spinner
    private lateinit var tvCurrentBalance: TextView
    private lateinit var btnAddMoney: Button
    private lateinit var btnRemoveMoney: Button
    private lateinit var layoutAddMoney: LinearLayout
    private lateinit var layoutRemoveMoney: LinearLayout
    private lateinit var etAddAmount: EditText
    private lateinit var btnSaveAdd: Button
    private lateinit var etRemoveAmount: EditText
    private lateinit var btnSaveTransfer: Button
    private lateinit var lvAllBalances: ListView

    private var selectedPlayerId: Int = 1
    private var selectedRecipientIndex: Int = 0

    companion object {
        fun newInstance() = GameFragment()
    }

    private val viewModel: GameViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //return inflater.inflate(R.layout.fragment_game, container, false)
        val view = inflater.inflate(R.layout.fragment_game, container, false)

        initializeViews(view)
        initializeGame()
        setupSpinnerListeners()
        setupObservers()
        setupButtonListeners()

        return view
    }

    private val playersAdapter by lazy {
        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, mutableListOf<String>())
    }

    private val recipientsAdapter by lazy {
        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, mutableListOf<String>())
    }

    private val balancesAdapter by lazy {
        ArrayAdapter(
            requireContext(), android.R.layout.simple_list_item_2,
            android.R.id.text1, mutableListOf<String>()
        )
    }

    private fun initializeViews(view: View) {
        spinnerPlayers = view.findViewById(R.id.spinnerPlayers)
        spinnerRecipient = view.findViewById(R.id.spinnerRecipient)
        tvCurrentBalance = view.findViewById(R.id.tvCurrentBalance)
        btnAddMoney = view.findViewById(R.id.btnAddMoney)
        btnRemoveMoney = view.findViewById(R.id.btnRemoveMoney)
        layoutAddMoney = view.findViewById(R.id.layoutAddMoney)
        layoutRemoveMoney = view.findViewById(R.id.layoutRemoveMoney)
        etAddAmount = view.findViewById(R.id.etAddAmount)
        btnSaveAdd = view.findViewById(R.id.btnSaveAdd)
        etRemoveAmount = view.findViewById(R.id.etRemoveAmount)
        btnSaveTransfer = view.findViewById(R.id.btnSaveTransfer)
        lvAllBalances = view.findViewById(R.id.lvAllBalances)

        spinnerPlayers.adapter = playersAdapter
        spinnerRecipient.adapter = recipientsAdapter
        lvAllBalances.adapter = balancesAdapter
    }

    private fun getArgumentsPlayers(): List<Player>? {
        return arguments?.getParcelableArrayList<Player>("players")
    }

    private fun initializeGame() {
        val players = getArgumentsPlayers()
        if (players != null) {
            viewModel.initializeGame(players)
            updateSpinners()
        }
    }

    private fun updateSpinners() {
        playersAdapter.clear()
        playersAdapter.addAll(viewModel.getPlayerNames())
        playersAdapter.notifyDataSetChanged()

        recipientsAdapter.clear()
        recipientsAdapter.addAll(viewModel.getRecipientsList())
        recipientsAdapter.notifyDataSetChanged()
    }

    private fun setupSpinnerListeners() {
        spinnerPlayers.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedPlayerId = position + 1
                viewModel.setSelectedPlayer(selectedPlayerId)
                updateCurrentBalance()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerRecipient.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedRecipientIndex = position
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun updateCurrentBalance() {
        val player = viewModel.getCurrentPlayer()
        if (player != null) {
            tvCurrentBalance.text = "Баланс: ${player.balance} золотых"
        }
    }

    private fun setupObservers() {
        viewModel.players.observe(viewLifecycleOwner) { players ->
            updateBalancesList()
            updateCurrentBalance()
        }
    }

    private fun updateBalancesList() {
        val players = viewModel.players.value ?: return

        val balances = players.map { "${it.name}: ${it.balance} золотых" }

        balancesAdapter.clear()
        balancesAdapter.addAll(balances)
        balancesAdapter.notifyDataSetChanged()
    }

    private fun showAddMoneyForm() {
        layoutAddMoney.visibility = View.VISIBLE
        layoutRemoveMoney.visibility = View.GONE
        etAddAmount.requestFocus()
    }

    private fun showRemoveMoneyForm() {
        layoutRemoveMoney.visibility = View.VISIBLE
        layoutAddMoney.visibility = View.GONE
        etRemoveAmount.requestFocus()
    }

    private fun saveAddMoney() {
        val amountStr = etAddAmount.text.toString()

        if (amountStr.isEmpty()) {
            Toast.makeText(context, "Введите сумму", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toIntOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(context, "Введите корректную сумму", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.addMoney(amount)

        Toast.makeText(context, "Добавлено $amount золотых", Toast.LENGTH_SHORT).show()

        etAddAmount.text.clear()
        layoutAddMoney.visibility = View.GONE
    }

    private fun saveTransfer() {
        val amountStr = etRemoveAmount.text.toString()

        if (amountStr.isEmpty()) {
            Toast.makeText(context, "Введите сумму", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toIntOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(context, "Введите корректную сумму", Toast.LENGTH_SHORT).show()
            return
        }

        val recipientId = if (selectedRecipientIndex == 0) null else selectedRecipientIndex

        val success = viewModel.transferMoney(selectedPlayerId, recipientId, amount)

        if (success) {
            val recipientName = if (recipientId == null) "банк" else
                viewModel.getPlayerNames().getOrNull(recipientId - 1) ?: "неизвестно"

            Toast.makeText(context, "Переведено $amount золотых в $recipientName",
                Toast.LENGTH_SHORT).show()

            etRemoveAmount.text.clear()
            layoutRemoveMoney.visibility = View.GONE
        } else {
            Toast.makeText(context, "Недостаточно средств", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupButtonListeners() {
        btnAddMoney.setOnClickListener {
            showAddMoneyForm()
        }

        btnRemoveMoney.setOnClickListener {
            showRemoveMoneyForm()
        }

        btnSaveAdd.setOnClickListener {
            saveAddMoney()
        }

        btnSaveTransfer.setOnClickListener {
            saveTransfer()
        }

    }


}