// ... existing code ...
class ChatFragment : Fragment() {
    private lateinit var chatAdapter: ChatAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // ... existing code ...
    }
}
class ChatAdapter(private val chatList: List<Chat>) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // ... existing code ...
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // ... existing code ...
    }
    override fun getItemCount(): Int {
        return chatList.size
    }
}
class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
