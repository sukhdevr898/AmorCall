import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import '../../core/theme/duora_theme.dart';
import '../../services/auth_service.dart';
import '../../services/signaling_service.dart';
import '../call/active_call_screen.dart';

enum MessageType { text, voice, image }

class ChatMessageModel {
  final String id;
  String text;
  final bool isMe;
  final DateTime timestamp;
  final MessageType type;
  final String? mediaUrl;
  final String? voiceDuration;
  final String? replyToText;
  final String? replyToAuthor;
  bool isRead;
  bool isEdited;

  ChatMessageModel({
    required this.id,
    required this.text,
    required this.isMe,
    required this.timestamp,
    this.type = MessageType.text,
    this.mediaUrl,
    this.voiceDuration,
    this.replyToText,
    this.replyToAuthor,
    this.isRead = true,
    this.isEdited = false,
  });
}

class PartnerChatScreen extends StatefulWidget {
  final String partnerName;
  final String partnerCode;
  final String partnerAvatar;

  const PartnerChatScreen({
    super.key,
    this.partnerName = 'Unisha ♡',
    this.partnerCode = 'LOVE01',
    this.partnerAvatar = 'assets/images/avatar_partner_demo.png',
  });

  @override
  State<PartnerChatScreen> createState() => _PartnerChatScreenState();
}

class _PartnerChatScreenState extends State<PartnerChatScreen> {
  final List<ChatMessageModel> _messages = [];
  final _textController = TextEditingController();
  final _scrollController = ScrollController();

  ChatMessageModel? _replyingTo;
  ChatMessageModel? _editingMessage;
  bool _isPlayingVoice = false;

  @override
  void initState() {
    super.initState();
    _loadInitialMessages();
  }

  void _loadInitialMessages() {
    final now = DateTime.now();
    _messages.addAll([
      ChatMessageModel(
        id: '1',
        text: 'Hii ♡',
        isMe: false,
        timestamp: DateTime(now.year, now.month, now.day, 20, 10),
      ),
      ChatMessageModel(
        id: '2',
        text: 'Kya kar rahe ho? 😊',
        isMe: false,
        timestamp: DateTime(now.year, now.month, now.day, 20, 11),
      ),
      ChatMessageModel(
        id: '3',
        text: 'Kuch kaam kar raha hu,\nTum batao? ♡',
        isMe: true,
        timestamp: DateTime(now.year, now.month, now.day, 20, 13),
        isRead: true,
      ),
      ChatMessageModel(
        id: '4',
        text: 'Voice note',
        isMe: false,
        type: MessageType.voice,
        voiceDuration: '0:28',
        timestamp: DateTime(now.year, now.month, now.day, 20, 15),
      ),
      ChatMessageModel(
        id: '5',
        text: 'Sunset view from terrace 🌅',
        isMe: true,
        type: MessageType.image,
        mediaUrl: 'assets/images/moment_couple.png',
        timestamp: DateTime(now.year, now.month, now.day, 20, 19),
        isRead: true,
      ),
      ChatMessageModel(
        id: '6',
        text: 'Bas tumse baat ♡',
        isMe: false,
        replyToText: 'Kya kar rahe ho? 😊',
        replyToAuthor: 'Unisha ♡',
        timestamp: DateTime(now.year, now.month, now.day, 20, 21),
      ),
    ]);
  }

  void _sendMessage() {
    final text = _textController.text.trim();
    if (text.isEmpty) return;

    if (_editingMessage != null) {
      // Edit existing message
      setState(() {
        _editingMessage!.text = text;
        _editingMessage!.isEdited = true;
        _editingMessage = null;
        _textController.clear();
      });
      return;
    }

    final newMsg = ChatMessageModel(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      text: text,
      isMe: true,
      timestamp: DateTime.now(),
      replyToText: _replyingTo?.text,
      replyToAuthor: _replyingTo != null
          ? (_replyingTo!.isMe ? 'You' : widget.partnerName)
          : null,
      isRead: true,
    );

    setState(() {
      _messages.add(newMsg);
      _replyingTo = null;
      _textController.clear();
    });

    _scrollToBottom();
  }

  void _sendVoiceNote() {
    final newMsg = ChatMessageModel(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      text: 'Voice message',
      isMe: true,
      type: MessageType.voice,
      voiceDuration: '0:14',
      timestamp: DateTime.now(),
      isRead: true,
    );

    setState(() {
      _messages.add(newMsg);
    });

    _scrollToBottom();
  }

  void _sendImageMoment() {
    final newMsg = ChatMessageModel(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      text: 'Romantic memory captured ✨',
      isMe: true,
      type: MessageType.image,
      mediaUrl: 'assets/images/moment_heart_lake.png',
      timestamp: DateTime.now(),
      isRead: true,
    );

    setState(() {
      _messages.add(newMsg);
    });

    _scrollToBottom();
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }

  void _showMessageActionMenu(BuildContext context, ChatMessageModel message) {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      builder: (ctx) => Container(
        margin: const EdgeInsets.all(16),
        padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 16),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(24),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.15),
              blurRadius: 20,
              offset: const Offset(0, 4),
            ),
          ],
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            // Reply
            ListTile(
              leading: const Icon(Icons.reply_rounded, color: Color(0xFF5B3DF5)),
              title: const Text('Reply',
                  style: TextStyle(fontWeight: FontWeight.bold, color: Color(0xFF211452))),
              onTap: () {
                Navigator.pop(ctx);
                setState(() => _replyingTo = message);
              },
            ),
            // Edit (only for outgoing message)
            if (message.isMe && message.type == MessageType.text)
              ListTile(
                leading: const Icon(Icons.edit_outlined, color: Color(0xFF5B3DF5)),
                title: const Text('Edit',
                    style: TextStyle(fontWeight: FontWeight.bold, color: Color(0xFF211452))),
                onTap: () {
                  Navigator.pop(ctx);
                  setState(() {
                    _editingMessage = message;
                    _textController.text = message.text;
                  });
                },
              ),
            // Copy
            ListTile(
              leading: const Icon(Icons.copy_rounded, color: Color(0xFF5B3DF5)),
              title: const Text('Copy',
                  style: TextStyle(fontWeight: FontWeight.bold, color: Color(0xFF211452))),
              onTap: () {
                Navigator.pop(ctx);
                Clipboard.setData(ClipboardData(text: message.text));
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    backgroundColor: Color(0xFF211452),
                    content: Text('Message copied to clipboard!'),
                    duration: Duration(seconds: 1),
                  ),
                );
              },
            ),
            // Delete
            ListTile(
              leading: const Icon(Icons.delete_outline_rounded, color: Colors.redAccent),
              title: const Text('Delete',
                  style: TextStyle(fontWeight: FontWeight.bold, color: Colors.redAccent)),
              onTap: () {
                Navigator.pop(ctx);
                _showDeleteDialog(message);
              },
            ),
          ],
        ),
      ),
    );
  }

  void _showDeleteDialog(ChatMessageModel message) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: Colors.white,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        title: const Text('Delete Message',
            style: TextStyle(fontWeight: FontWeight.bold, color: Color(0xFF211452))),
        content: const Text(
          'Do you want to delete this message?',
          style: TextStyle(color: Color(0xFF5E5478)),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel', style: TextStyle(color: Colors.grey)),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.redAccent,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            ),
            onPressed: () {
              Navigator.pop(ctx);
              setState(() {
                _messages.removeWhere((m) => m.id == message.id);
              });
            },
            child: const Text('Delete for Everyone', style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );
  }

  void _showAttachmentSheet() {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      builder: (ctx) => Container(
        margin: const EdgeInsets.all(16),
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(28),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.12),
              blurRadius: 20,
              offset: const Offset(0, 4),
            ),
          ],
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text(
              'Share with Partner 💕',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: Color(0xFF211452),
              ),
            ),
            const SizedBox(height: 20),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: [
                _AttachmentActionItem(
                  icon: Icons.photo_library_rounded,
                  label: 'Photo Moment',
                  color: const Color(0xFFFF2D8D),
                  onTap: () {
                    Navigator.pop(ctx);
                    _sendImageMoment();
                  },
                ),
                _AttachmentActionItem(
                  icon: Icons.mic_rounded,
                  label: 'Voice Note',
                  color: const Color(0xFF5B3DF5),
                  onTap: () {
                    Navigator.pop(ctx);
                    _sendVoiceNote();
                  },
                ),
                _AttachmentActionItem(
                  icon: Icons.favorite_rounded,
                  label: 'Love Poke',
                  color: const Color(0xFFFF6584),
                  onTap: () {
                    Navigator.pop(ctx);
                    _textController.text = 'Sending you a warm hug! 🤗❤️';
                    _sendMessage();
                  },
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final sig = Provider.of<SignalingService>(context);

    return Scaffold(
      backgroundColor: const Color(0xFFF3EDFD),
      appBar: AppBar(
        backgroundColor: const Color(0xFF28165A),
        elevation: 1,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_rounded, color: Colors.white, size: 20),
          onPressed: () => Navigator.pop(context),
        ),
        titleSpacing: 0,
        title: Row(
          children: [
            // Partner Avatar with green online dot
            Stack(
              clipBehavior: Clip.none,
              children: [
                Container(
                  width: 38,
                  height: 38,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    border: Border.all(color: Colors.white, width: 1.5),
                  ),
                  child: ClipOval(
                    child: Image.asset(
                      widget.partnerAvatar,
                      fit: BoxFit.cover,
                      errorBuilder: (_, __, ___) => Container(
                        color: const Color(0xFFFF2D8D),
                        child: const Icon(Icons.person, color: Colors.white, size: 22),
                      ),
                    ),
                  ),
                ),
                Positioned(
                  bottom: 0,
                  right: 0,
                  child: Container(
                    width: 10,
                    height: 10,
                    decoration: BoxDecoration(
                      color: const Color(0xFF10B981),
                      shape: BoxShape.circle,
                      border: Border.all(color: Colors.white, width: 1.5),
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(width: 10),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    widget.partnerName,
                    style: const TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                      color: Colors.white,
                    ),
                  ),
                  const Text(
                    '🟢 Online',
                    style: TextStyle(
                      fontSize: 11,
                      color: Color(0xFFB057F7),
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
        actions: [
          // Audio Call Button
          IconButton(
            icon: const Icon(Icons.phone_rounded, color: Colors.white, size: 22),
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (_) => ActiveCallScreen(
                    partnerName: widget.partnerName,
                    partnerCode: widget.partnerCode,
                    isVideo: false,
                    signalingService: sig,
                  ),
                ),
              );
            },
          ),
          // Video Call Button
          IconButton(
            icon: const Icon(Icons.videocam_rounded, color: Colors.white, size: 24),
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (_) => ActiveCallScreen(
                    partnerName: widget.partnerName,
                    partnerCode: widget.partnerCode,
                    isVideo: true,
                    signalingService: sig,
                  ),
                ),
              );
            },
          ),
          // Three dot menu
          PopupMenuButton<String>(
            icon: const Icon(Icons.more_vert_rounded, color: Colors.white),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
            onSelected: (val) {
              if (val == 'clear') {
                setState(() => _messages.clear());
              }
            },
            itemBuilder: (ctx) => [
              const PopupMenuItem(value: 'search', child: Text('Search Messages')),
              const PopupMenuItem(value: 'media', child: Text('Media & Links')),
              const PopupMenuItem(value: 'disappear', child: Text('Disappearing Messages')),
              const PopupMenuItem(value: 'clear', child: Text('Clear History')),
            ],
          ),
        ],
      ),
      body: Column(
        children: [
          // Messages List View
          Expanded(
            child: ListView.builder(
              controller: _scrollController,
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
              itemCount: _messages.length + 1, // +1 for "Today" pill header
              itemBuilder: (context, index) {
                if (index == 0) {
                  return Center(
                    child: Container(
                      margin: const EdgeInsets.symmetric(vertical: 8),
                      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 4),
                      decoration: BoxDecoration(
                        color: const Color(0xFFDCD2F6),
                        borderRadius: BorderRadius.circular(14),
                      ),
                      child: const Text(
                        'Today',
                        style: TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.w600,
                          color: Color(0xFF5B3DF5),
                        ),
                      ),
                    ),
                  );
                }

                final msg = _messages[index - 1];
                return GestureDetector(
                  onLongPress: () => _showMessageActionMenu(context, msg),
                  child: _MessageBubbleWidget(
                    message: msg,
                    isPlayingVoice: _isPlayingVoice,
                    onToggleVoice: () {
                      setState(() => _isPlayingVoice = !_isPlayingVoice);
                    },
                  ),
                );
              },
            ),
          ),

          // Reply / Edit Banner above Composer
          if (_replyingTo != null || _editingMessage != null)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              color: Colors.white,
              child: Row(
                children: [
                  Container(
                    width: 4,
                    height: 36,
                    decoration: BoxDecoration(
                      color: const Color(0xFFFF2D8D),
                      borderRadius: BorderRadius.circular(2),
                    ),
                  ),
                  const SizedBox(width: 10),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          _editingMessage != null
                              ? 'Editing Message'
                              : 'Replying to ${_replyingTo!.isMe ? "yourself" : widget.partnerName}',
                          style: const TextStyle(
                            fontSize: 12,
                            fontWeight: FontWeight.bold,
                            color: Color(0xFFFF2D8D),
                          ),
                        ),
                        Text(
                          _editingMessage != null
                              ? _editingMessage!.text
                              : _replyingTo!.text,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: const TextStyle(fontSize: 12, color: Color(0xFF5E5478)),
                        ),
                      ],
                    ),
                  ),
                  IconButton(
                    icon: const Icon(Icons.close_rounded, size: 18, color: Colors.grey),
                    onPressed: () {
                      setState(() {
                        _replyingTo = null;
                        if (_editingMessage != null) {
                          _editingMessage = null;
                          _textController.clear();
                        }
                      });
                    },
                  ),
                ],
              ),
            ),

          // Chat Composer Bar
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
            decoration: BoxDecoration(
              color: Colors.white,
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.04),
                  blurRadius: 10,
                  offset: const Offset(0, -2),
                ),
              ],
            ),
            child: SafeArea(
              top: false,
              child: Row(
                children: [
                  // Attachment "+" Button
                  GestureDetector(
                    onTap: _showAttachmentSheet,
                    child: Container(
                      width: 40,
                      height: 40,
                      decoration: const BoxDecoration(
                        color: Color(0xFFF1EDFC),
                        shape: BoxShape.circle,
                      ),
                      child: const Center(
                        child: Icon(Icons.add_rounded, color: Color(0xFF5B3DF5), size: 24),
                      ),
                    ),
                  ),

                  const SizedBox(width: 8),

                  // Message Text Input Field
                  Expanded(
                    child: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 14),
                      decoration: BoxDecoration(
                        color: const Color(0xFFF7F4FF),
                        borderRadius: BorderRadius.circular(24),
                        border: Border.all(color: const Color(0xFFE4DCF9)),
                      ),
                      child: Row(
                        children: [
                          Expanded(
                            child: TextField(
                              controller: _textController,
                              style: const TextStyle(fontSize: 14, color: Color(0xFF211452)),
                              decoration: const InputDecoration(
                                hintText: 'Message...',
                                hintStyle: TextStyle(fontSize: 13.5, color: Colors.grey),
                                border: InputBorder.none,
                                isDense: true,
                                contentPadding: EdgeInsets.symmetric(vertical: 12),
                              ),
                              onSubmitted: (_) => _sendMessage(),
                            ),
                          ),
                          // Emoji Icon
                          GestureDetector(
                            onTap: () {
                              _textController.text += ' ♡ ';
                            },
                            child: const Icon(Icons.sentiment_satisfied_alt_rounded,
                                color: Color(0xFF7C6E96), size: 22),
                          ),
                        ],
                      ),
                    ),
                  ),

                  const SizedBox(width: 8),

                  // Mic (Record) or Send Button
                  GestureDetector(
                    onTap: _sendMessage,
                    child: Container(
                      width: 44,
                      height: 44,
                      decoration: const BoxDecoration(
                        gradient: LinearGradient(
                          colors: [Color(0xFF7C4DFF), Color(0xFF5B3DF5)],
                        ),
                        shape: BoxShape.circle,
                      ),
                      child: const Center(
                        child: Icon(Icons.send_rounded, color: Colors.white, size: 20),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _MessageBubbleWidget extends StatelessWidget {
  final ChatMessageModel message;
  final bool isPlayingVoice;
  final VoidCallback onToggleVoice;

  const _MessageBubbleWidget({
    required this.message,
    required this.isPlayingVoice,
    required this.onToggleVoice,
  });

  @override
  Widget build(BuildContext context) {
    final timeFormat = DateFormat('h:mm a');
    final isMe = message.isMe;

    return Align(
      alignment: isMe ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        margin: const EdgeInsets.symmetric(vertical: 4),
        constraints: BoxConstraints(
          maxWidth: MediaQuery.of(context).size.width * 0.76,
        ),
        decoration: BoxDecoration(
          gradient: isMe
              ? const LinearGradient(
                  colors: [Color(0xFF7C4DFF), Color(0xFF5B3DF5)],
                )
              : null,
          color: isMe ? null : Colors.white,
          borderRadius: BorderRadius.only(
            topLeft: const Radius.circular(18),
            topRight: const Radius.circular(18),
            bottomLeft: Radius.circular(isMe ? 18 : 4),
            bottomRight: Radius.circular(isMe ? 4 : 18),
          ),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.04),
              blurRadius: 8,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Reply Preview Snippet
            if (message.replyToText != null) ...[
              Container(
                padding: const EdgeInsets.all(6),
                margin: const EdgeInsets.only(bottom: 6),
                decoration: BoxDecoration(
                  color: isMe ? Colors.white.withOpacity(0.18) : const Color(0xFFECE7FA),
                  borderRadius: BorderRadius.circular(8),
                  border: Border(
                    left: BorderSide(
                      color: isMe ? Colors.white : const Color(0xFFFF2D8D),
                      width: 3,
                    ),
                  ),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      message.replyToAuthor ?? 'Partner',
                      style: TextStyle(
                        fontSize: 10,
                        fontWeight: FontWeight.bold,
                        color: isMe ? Colors.white : const Color(0xFFFF2D8D),
                      ),
                    ),
                    Text(
                      message.replyToText!,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: TextStyle(
                        fontSize: 11,
                        color: isMe ? Colors.white70 : const Color(0xFF4A3E68),
                      ),
                    ),
                  ],
                ),
              ),
            ],

            // Message Body according to type
            if (message.type == MessageType.image) ...[
              ClipRRect(
                borderRadius: BorderRadius.circular(12),
                child: Image.asset(
                  message.mediaUrl ?? 'assets/images/moment_couple.png',
                  height: 140,
                  width: double.infinity,
                  fit: BoxFit.cover,
                  errorBuilder: (_, __, ___) => Container(
                    height: 140,
                    color: const Color(0xFFDCD2F6),
                    child: const Icon(Icons.broken_image_rounded),
                  ),
                ),
              ),
              const SizedBox(height: 6),
            ] else if (message.type == MessageType.voice) ...[
              Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  GestureDetector(
                    onTap: onToggleVoice,
                    child: Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: isMe ? Colors.white.withOpacity(0.2) : const Color(0xFF5B3DF5),
                        shape: BoxShape.circle,
                      ),
                      child: Icon(
                        isPlayingVoice ? Icons.pause_rounded : Icons.play_arrow_rounded,
                        color: Colors.white,
                        size: 20,
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  // Audio Waveform graphic representation
                  Row(
                    children: List.generate(
                      14,
                      (i) => Container(
                        margin: const EdgeInsets.symmetric(horizontal: 1.5),
                        width: 2.5,
                        height: 6.0 + (i % 4) * 6.0,
                        decoration: BoxDecoration(
                          color: isMe ? Colors.white : const Color(0xFF5B3DF5),
                          borderRadius: BorderRadius.circular(2),
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Text(
                    message.voiceDuration ?? '0:28',
                    style: TextStyle(
                      fontSize: 11,
                      color: isMe ? Colors.white70 : const Color(0xFF5E5478),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 4),
            ],

            if (message.type == MessageType.text)
              Text(
                message.text,
                style: TextStyle(
                  fontSize: 14,
                  height: 1.35,
                  color: isMe ? Colors.white : const Color(0xFF211452),
                ),
              ),

            // Timestamp, Edited, and Double Tick Read Indicator
            const SizedBox(height: 4),
            Row(
              mainAxisSize: MainAxisSize.min,
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                if (message.isEdited) ...[
                  Text(
                    '(edited) ',
                    style: TextStyle(
                      fontSize: 9.5,
                      color: isMe ? Colors.white60 : Colors.grey.shade500,
                    ),
                  ),
                ],
                Text(
                  timeFormat.format(message.timestamp),
                  style: TextStyle(
                    fontSize: 10,
                    color: isMe ? Colors.white70 : Colors.grey.shade500,
                  ),
                ),
                if (isMe) ...[
                  const SizedBox(width: 4),
                  const Icon(
                    Icons.done_all_rounded,
                    size: 13,
                    color: Colors.white,
                  ),
                ],
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _AttachmentActionItem extends StatelessWidget {
  final IconData icon;
  final String label;
  final Color color;
  final VoidCallback onTap;

  const _AttachmentActionItem({
    required this.icon,
    required this.label,
    required this.color,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Column(
        children: [
          Container(
            width: 56,
            height: 56,
            decoration: BoxDecoration(
              color: color.withOpacity(0.12),
              shape: BoxShape.circle,
            ),
            child: Center(
              child: Icon(icon, color: color, size: 28),
            ),
          ),
          const SizedBox(height: 8),
          Text(
            label,
            style: const TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w600,
              color: Color(0xFF211452),
            ),
          ),
        ],
      ),
    );
  }
}
