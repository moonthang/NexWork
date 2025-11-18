package com.example.nexwork.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nexwork.R
import com.example.nexwork.data.repository.AuthRepository
import com.example.nexwork.data.repository.ChatRepository
import com.example.nexwork.databinding.FragmentChatBinding

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChatViewModel
    private lateinit var messageAdapter: MessageAdapter
    private var chatId: String? = null
    private var name: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            chatId = it.getString(ARG_CHAT_ID)
            name = it.getString(ARG_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val basic_header = view.findViewById<View>(R.id.header)
        val btnNotification = basic_header.findViewById<ImageView>(R.id.btnNotification)
        val btnSearch = basic_header.findViewById<ImageView>(R.id.btnSearch)
        val btnFilter = basic_header.findViewById<ImageView>(R.id.btnFilter)
        val btnOptions = basic_header.findViewById<ImageView>(R.id.btnOptions)
        val txtTitle = basic_header.findViewById<TextView>(R.id.txtTitle)
        val btnBack = basic_header.findViewById<ImageView>(R.id.btnBack)

        btnNotification.visibility = View.GONE
        btnSearch.visibility = View.GONE
        btnFilter.visibility = View.GONE
        btnOptions.visibility = View.VISIBLE

        txtTitle.text = name

        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val chatRepository = ChatRepository()
        val authRepository = AuthRepository()
        val viewModelFactory = ChatViewModelFactory(chatRepository, authRepository)
        viewModel = ViewModelProvider(this, viewModelFactory)[ChatViewModel::class.java]

        setupRecyclerView()

        // Observa los cambios en la lista de mensajes y actualiza el adaptador
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            messageAdapter.updateData(messages)
            binding.rvMessages.scrollToPosition(messages.size - 1)
        }

        chatId?.let {
            viewModel.getMessages(it)
        }

        // Configura el botón de envío
        binding.btnSend.setOnClickListener {
            val messageText = binding.etMessage.text.toString()
            if (messageText.isNotBlank()) {
                chatId?.let {
                    viewModel.sendMessage(it, messageText)
                    binding.etMessage.text.clear()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(emptyList())
        binding.rvMessages.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    // Limpia la referencia al binding
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // Constantes para los argumentos del fragmento
        private const val ARG_CHAT_ID = "chat_id"
        private const val ARG_NAME = "name"

        // Funcion para crear una nueva instancia del fragmento
        @JvmStatic
        fun newInstance(chatId: String, name: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CHAT_ID, chatId)
                    putString(ARG_NAME, name)
                }
            }
    }
}