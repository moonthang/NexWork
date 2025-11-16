// ... existing code ...
class ChatActivity : AppCompatActivity() {
    private lateinit var chatFragment: ChatFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        // ... existing code ...
    }
}
class ChatViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats = _chats
    fun getChats(): Flow<List<Chat>> {
        // ... existing code ...
    }
}
class ChatRepository(private val firebaseDatabase: FirebaseDatabase) : Repository() {
    override fun getUsers(): Flow<List<User>> {
        // ... existing code ...
    }
}