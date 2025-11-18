package com.example.nexwork.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nexwork.R
import com.example.nexwork.data.model.Chat
import com.example.nexwork.data.model.User
import com.example.nexwork.data.repository.AuthRepository
import com.example.nexwork.data.repository.ChatRepository
import com.example.nexwork.databinding.FragmentChatListBinding
import kotlinx.coroutines.launch
import java.text.Normalizer

data class ChatWithUser(
    val chat: Chat,
    val otherUser: User
)

class UserSearchFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ChatViewModel
    private lateinit var chatListAdapter: ChatListAdapter
    private lateinit var userSearchAdapter: UserSearchAdapter
    private val authRepository = AuthRepository()
    private var allUsers: List<User> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
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
        btnFilter.visibility = View.VISIBLE
        btnOptions.visibility = View.GONE

        txtTitle.text = getString(R.string.inbox_title)

        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val chatRepository = ChatRepository()
        val viewModelFactory = ChatViewModelFactory(chatRepository, authRepository)
        viewModel = ViewModelProvider(this, viewModelFactory)[ChatViewModel::class.java]

        setupRecyclerViews()
        setupSearchView()
        observeViewModel()

        // Carga todos los usuarios para la búsqueda local
        viewModel.fetchAllUsers()
    }

    private fun setupRecyclerViews() {
        // Adaptador para la lista de chats recientes (mejorado)
        chatListAdapter = ChatListAdapter(emptyList()) { chatWithUser ->
            navigateToChat(chatWithUser.chat.chatId, "${chatWithUser.otherUser.firstName} ${chatWithUser.otherUser.lastName}")
        }

        // Adaptador para los resultados de búsqueda
        userSearchAdapter = UserSearchAdapter(emptyList()) { user ->
            val currentUserId = authRepository.getCurrentUserId()
            if (currentUserId != null) {
                lifecycleScope.launch {
                    val chatId = getChatId(currentUserId, user.userId)
                    val participantIds = listOf(currentUserId, user.userId)
                    viewModel.createChatIfNotExists(chatId, participantIds)
                    navigateToChat(chatId, "${user.firstName} ${user.lastName}")
                }
            }
        }

        binding.rvChats.apply {
            adapter = chatListAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViewModel() {
        // Observador para los chats recientes
        viewModel.chatsWithUsers.observe(viewLifecycleOwner) { chatsWithUsers ->
            chatListAdapter.updateData(chatsWithUsers)
        }

        // Observador para la lista completa de usuarios
        viewModel.allUsers.observe(viewLifecycleOwner) { users ->
            allUsers = users
        }

        // Se pide la lista de chats al iniciar
        viewModel.getChats()
    }

    // Configuración del SearchView
    private fun setupSearchView() {
        binding.userSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    binding.rvChats.adapter = chatListAdapter
                } else {
                    binding.rvChats.adapter = userSearchAdapter
                    val filteredUsers = filterUsers(newText)
                    userSearchAdapter.updateData(filteredUsers)
                }
                return true
            }
        })
    }

    // Filtra los usuarios según el texto de búsqueda
    private fun filterUsers(query: String): List<User> {
        val normalizedQuery = query.normalized()
        return allUsers.filter { user ->
            val fullName = "${user.firstName} ${user.lastName}".normalized()
            fullName.contains(normalizedQuery, ignoreCase = true)
        }
    }

    // Extensión para normalizar texto (quitar tildes y transformar mayusculas a minúsculas)
    private fun String.normalized(): String {
        return Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .lowercase()
    }

    // Navega al fragment de chat
    private fun navigateToChat(chatId: String, name: String) {
        val fragment = ChatFragment.newInstance(chatId, name)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    // Crea un ID único para el chat basado en los IDs de los participantes
    private fun getChatId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "$userId1-$userId2" else "$userId2-$userId1"
    }

    // Limpia los recursos
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
