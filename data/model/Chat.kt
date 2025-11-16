class Chat {
    val userId: String
    val userName: String
    var messages: List<Message> = listOf()
}
class Message {
    val text: String
    val senderId: String
    val receiverId: String
}