import 'package:flutter/material.dart';
import 'partner_chat_screen.dart';
import 'admin_chat_screen.dart';

class ChatTab extends StatefulWidget {
  const ChatTab({super.key});

  @override
  State<ChatTab> createState() => _ChatTabState();
}

class _ChatTabState extends State<ChatTab> {
  int _selectedFilter = 0; // 0: All, 1: Partner, 2: Admin
  final _searchController = TextEditingController();

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFDCD2F6),
      floatingActionButton: Padding(
        padding: const EdgeInsets.only(bottom: 74), // Avoid bottom bar
        child: FloatingActionButton(
          backgroundColor: const Color(0xFF5B3DF5),
          foregroundColor: Colors.white,
          shape: const CircleBorder(),
          elevation: 4,
          onPressed: () {
            Navigator.push(
              context,
              MaterialPageRoute(builder: (_) => const PartnerChatScreen()),
            );
          },
          child: const Icon(Icons.edit_rounded, size: 24),
        ),
      ),
      body: SafeArea(
        bottom: false,
        child: Column(
          children: [
            // 1. Top Header Bar: "Chat" title + Search & Profile Avatar
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    'Chat',
                    style: TextStyle(
                      fontSize: 26,
                      fontWeight: FontWeight.w900,
                      color: Color(0xFF211452),
                    ),
                  ),
                  Container(
                    width: 38,
                    height: 38,
                    decoration: BoxDecoration(
                      color: Colors.white,
                      shape: BoxShape.circle,
                      boxShadow: [
                        BoxShadow(
                          color: const Color(0xFF5B3DF5).withOpacity(0.1),
                          blurRadius: 8,
                        ),
                      ],
                    ),
                    child: const Icon(Icons.person_outline_rounded,
                        color: Color(0xFF5B3DF5), size: 22),
                  ),
                ],
              ),
            ),

            // 2. Search Bar
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 6),
              child: Container(
                height: 46,
                padding: const EdgeInsets.symmetric(horizontal: 14),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(23),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.04),
                      blurRadius: 8,
                      offset: const Offset(0, 2),
                    ),
                  ],
                ),
                child: Row(
                  children: [
                    const Icon(Icons.search_rounded, color: Color(0xFF7C6E96), size: 20),
                    const SizedBox(width: 10),
                    Expanded(
                      child: TextField(
                        controller: _searchController,
                        style: const TextStyle(fontSize: 13.5, color: Color(0xFF211452)),
                        decoration: const InputDecoration(
                          hintText: 'Search chats...',
                          hintStyle: TextStyle(fontSize: 13, color: Colors.grey),
                          border: InputBorder.none,
                          isDense: true,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 10),

            // 3. Filter Chips: All, Partner ♡, Admin 🛡️
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20),
              child: Row(
                children: [
                  _FilterChip(
                    label: 'All',
                    isSelected: _selectedFilter == 0,
                    onTap: () => setState(() => _selectedFilter = 0),
                  ),
                  const SizedBox(width: 8),
                  _FilterChip(
                    label: 'Partner ♡',
                    isSelected: _selectedFilter == 1,
                    onTap: () => setState(() => _selectedFilter = 1),
                  ),
                  const SizedBox(width: 8),
                  _FilterChip(
                    label: 'Admin 🛡️',
                    isSelected: _selectedFilter == 2,
                    onTap: () => setState(() => _selectedFilter = 2),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 14),

            // 4. Conversation Items List
            Expanded(
              child: ListView(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                children: [
                  // Partner Chat: Unisha ♡
                  if (_selectedFilter == 0 || _selectedFilter == 1)
                    _ConversationTile(
                      avatarWidget: Stack(
                        clipBehavior: Clip.none,
                        children: [
                          Container(
                            width: 50,
                            height: 50,
                            decoration: BoxDecoration(
                              shape: BoxShape.circle,
                              border: Border.all(color: Colors.white, width: 2),
                              boxShadow: [
                                BoxShadow(
                                  color: Colors.black.withOpacity(0.08),
                                  blurRadius: 6,
                                ),
                              ],
                            ),
                            child: ClipOval(
                              child: Image.asset(
                                'assets/images/avatar_partner_demo.png',
                                fit: BoxFit.cover,
                                errorBuilder: (_, __, ___) => Container(
                                  color: const Color(0xFFFF2D8D),
                                  child: const Icon(Icons.person, color: Colors.white),
                                ),
                              ),
                            ),
                          ),
                          Positioned(
                            bottom: 0,
                            right: 0,
                            child: Container(
                              width: 12,
                              height: 12,
                              decoration: BoxDecoration(
                                color: const Color(0xFF10B981),
                                shape: BoxShape.circle,
                                border: Border.all(color: Colors.white, width: 2),
                              ),
                            ),
                          ),
                        ],
                      ),
                      title: 'Unisha ♡',
                      isVerified: true,
                      lastMessage: 'You: Good night ♡',
                      time: '10:24 PM',
                      badgeCount: 2,
                      onTap: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(builder: (_) => const PartnerChatScreen()),
                        );
                      },
                    ),

                  const SizedBox(height: 10),

                  // Duora Admin
                  if (_selectedFilter == 0 || _selectedFilter == 2)
                    _ConversationTile(
                      avatarWidget: Container(
                        width: 50,
                        height: 50,
                        decoration: const BoxDecoration(
                          color: Color(0xFF5B3DF5),
                          shape: BoxShape.circle,
                        ),
                        child: const Center(
                          child: Icon(Icons.campaign_rounded, color: Colors.white, size: 26),
                        ),
                      ),
                      title: 'Duora Admin',
                      isVerified: true,
                      lastMessage: '📢 New update v3.1 is live!',
                      time: '6:45 PM',
                      badgeCount: 0,
                      onTap: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(builder: (_) => const AdminChatScreen()),
                        );
                      },
                    ),

                  const SizedBox(height: 10),

                  // Saved Messages
                  if (_selectedFilter == 0) ...[
                    _ConversationTile(
                      avatarWidget: Container(
                        width: 50,
                        height: 50,
                        decoration: BoxDecoration(
                          color: const Color(0xFF7C4DFF).withOpacity(0.15),
                          shape: BoxShape.circle,
                        ),
                        child: const Center(
                          child: Icon(Icons.bookmark_rounded,
                              color: Color(0xFF5B3DF5), size: 24),
                        ),
                      ),
                      title: 'Saved Messages',
                      isVerified: false,
                      lastMessage: 'Your personal space',
                      time: '',
                      badgeCount: 0,
                      onTap: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (_) => const PartnerChatScreen(
                              partnerName: 'Saved Messages',
                              partnerAvatar: 'assets/images/logo_heart.png',
                            ),
                          ),
                        );
                      },
                    ),

                    const SizedBox(height: 10),

                    // Archived Chats
                    _ConversationTile(
                      avatarWidget: Container(
                        width: 50,
                        height: 50,
                        decoration: BoxDecoration(
                          color: Colors.grey.shade200,
                          shape: BoxShape.circle,
                        ),
                        child: const Center(
                          child: Icon(Icons.archive_outlined,
                              color: Color(0xFF7C6E96), size: 24),
                        ),
                      ),
                      title: 'Archived Chats',
                      isVerified: false,
                      lastMessage: '2 chats',
                      time: '',
                      badgeCount: 0,
                      onTap: () {},
                    ),
                  ],

                  const SizedBox(height: 90), // Spacing for bottom bar
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _FilterChip extends StatelessWidget {
  final String label;
  final bool isSelected;
  final VoidCallback onTap;

  const _FilterChip({
    required this.label,
    required this.isSelected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 180),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 7),
        decoration: BoxDecoration(
          color: isSelected ? const Color(0xFF5B3DF5) : Colors.white,
          borderRadius: BorderRadius.circular(18),
          boxShadow: isSelected
              ? [
                  BoxShadow(
                    color: const Color(0xFF5B3DF5).withOpacity(0.3),
                    blurRadius: 8,
                    offset: const Offset(0, 2),
                  ),
                ]
              : null,
        ),
        child: Text(
          label,
          style: TextStyle(
            fontSize: 12.5,
            fontWeight: FontWeight.bold,
            color: isSelected ? Colors.white : const Color(0xFF4A3E68),
          ),
        ),
      ),
    );
  }
}

class _ConversationTile extends StatelessWidget {
  final Widget avatarWidget;
  final String title;
  final bool isVerified;
  final String lastMessage;
  final String time;
  final int badgeCount;
  final VoidCallback onTap;

  const _ConversationTile({
    required this.avatarWidget,
    required this.title,
    this.isVerified = false,
    required this.lastMessage,
    required this.time,
    this.badgeCount = 0,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(20),
          boxShadow: [
            BoxShadow(
              color: const Color(0xFF5B3DF5).withOpacity(0.04),
              blurRadius: 8,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Row(
          children: [
            avatarWidget,
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Text(
                        title,
                        style: const TextStyle(
                          fontSize: 15,
                          fontWeight: FontWeight.w800,
                          color: Color(0xFF211452),
                        ),
                      ),
                      if (isVerified) ...[
                        const SizedBox(width: 4),
                        const Icon(
                          Icons.verified_rounded,
                          color: Color(0xFF38BDF8),
                          size: 16,
                        ),
                      ],
                      const Spacer(),
                      if (time.isNotEmpty)
                        Text(
                          time,
                          style: TextStyle(
                            fontSize: 11,
                            color: Colors.grey.shade500,
                          ),
                        ),
                    ],
                  ),
                  const SizedBox(height: 4),
                  Row(
                    children: [
                      Expanded(
                        child: Text(
                          lastMessage,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: TextStyle(
                            fontSize: 12.5,
                            color: Colors.grey.shade600,
                          ),
                        ),
                      ),
                      if (badgeCount > 0)
                        Container(
                          padding: const EdgeInsets.all(5),
                          decoration: const BoxDecoration(
                            color: Color(0xFFFF2D8D),
                            shape: BoxShape.circle,
                          ),
                          child: Text(
                            badgeCount.toString(),
                            style: const TextStyle(
                              color: Colors.white,
                              fontSize: 10,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ),
                    ],
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
