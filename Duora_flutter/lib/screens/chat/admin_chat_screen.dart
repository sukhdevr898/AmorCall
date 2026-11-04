import 'package:flutter/material.dart';

class AdminChatScreen extends StatelessWidget {
  const AdminChatScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF3EDFD),
      appBar: AppBar(
        backgroundColor: const Color(0xFF28165A),
        elevation: 1,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_rounded, color: Colors.white, size: 20),
          onPressed: () => Navigator.pop(context),
        ),
        title: Row(
          children: [
            Container(
              width: 36,
              height: 36,
              decoration: const BoxDecoration(
                color: Color(0xFF5B3DF5),
                shape: BoxShape.circle,
              ),
              child: const Icon(Icons.campaign_rounded, color: Colors.white, size: 20),
            ),
            const SizedBox(width: 10),
            const Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Text(
                      'Duora Admin',
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                        color: Colors.white,
                      ),
                    ),
                    SizedBox(width: 4),
                    Icon(Icons.verified_rounded, color: Color(0xFF38BDF8), size: 16),
                  ],
                ),
                Text(
                  'Official Announcements',
                  style: TextStyle(
                    fontSize: 11,
                    color: Color(0xFFB057F7),
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
      body: ListView(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
        children: [
          _buildAdminNoticeCard(
            title: '📢 Duora v3.1 is now Live!',
            date: 'Today • 6:45 PM',
            content:
                'Welcome to the new Duora update! We have revamped the entire communications suite with HD Voice & Video calls, Picture-in-Picture support, and background call persistence. Enjoy your private space for two! 💜',
          ),
          const SizedBox(height: 14),
          _buildAdminNoticeCard(
            title: '🔒 End-to-End Encryption Verified',
            date: 'Yesterday • 10:00 AM',
            content:
                'All your private messages, memories, and calls are secured with direct device-to-device encryption. Only you and your partner can access your shared memories.',
          ),
        ],
      ),
    );
  }

  Widget _buildAdminNoticeCard({
    required String title,
    required String date,
    required String content,
  }) {
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(22),
        border: Border.all(color: const Color(0xFFE4DCF9)),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF5B3DF5).withOpacity(0.06),
            blurRadius: 12,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: const TextStyle(
              fontSize: 15,
              fontWeight: FontWeight.bold,
              color: Color(0xFF211452),
            ),
          ),
          const SizedBox(height: 4),
          Text(
            date,
            style: const TextStyle(
              fontSize: 11,
              color: Color(0xFF7C6E96),
            ),
          ),
          const SizedBox(height: 10),
          Text(
            content,
            style: const TextStyle(
              fontSize: 13,
              height: 1.4,
              color: Color(0xFF4A3E68),
            ),
          ),
        ],
      ),
    );
  }
}
