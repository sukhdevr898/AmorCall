import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../services/auth_service.dart';
import '../../services/signaling_service.dart';
import '../navigation/duora_bottom_bar.dart';
import '../call/active_call_screen.dart';
import '../pairing/partner_pairing_screen.dart';
import '../memories/memories_screen.dart';
import '../settings/server_health_screen.dart';

class HomeTab extends StatelessWidget {
  final ValueChanged<DuoraTab> onNavigateTab;

  const HomeTab({super.key, required this.onNavigateTab});

  @override
  Widget build(BuildContext context) {
    final authService = Provider.of<AuthService>(context);
    final user = authService.currentUser;

    return Scaffold(
      backgroundColor: const Color(0xFFDCD2F6),
      body: Stack(
        children: [
          // 1. Subtle twilight purple glow background
          Positioned.fill(
            child: Container(
              decoration: const BoxDecoration(
                gradient: LinearGradient(
                  colors: [
                    Color(0xFFE8DEFF),
                    Color(0xFFD5C4FA),
                    Color(0xFFC7B1F7),
                  ],
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                ),
              ),
            ),
          ),

          // 2. Main Content
          SafeArea(
            bottom: false,
            child: SingleChildScrollView(
              padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 8),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // A. Header: Horizontal Duora Logo + Bell & Avatar
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      // Duora Horizontal Logo
                      Image.asset(
                        'assets/images/duora_header_logo.png',
                        width: 150,
                        height: 42,
                        fit: BoxFit.contain,
                        alignment: Alignment.centerLeft,
                        errorBuilder: (_, __, ___) => const Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Icon(Icons.favorite, color: Color(0xFF5B3DF5), size: 26),
                            SizedBox(width: 8),
                            Text(
                              'Duora',
                              style: TextStyle(
                                fontSize: 24,
                                fontWeight: FontWeight.w900,
                                color: Color(0xFF211452),
                              ),
                            ),
                          ],
                        ),
                      ),

                      // Actions: Bell & Avatar
                      Row(
                        children: [
                          // Bell icon with orange/red notification dot
                          Stack(
                            clipBehavior: Clip.none,
                            children: [
                              Container(
                                width: 38,
                                height: 38,
                                decoration: BoxDecoration(
                                  color: Colors.white,
                                  shape: BoxShape.circle,
                                  boxShadow: [
                                    BoxShadow(
                                      color: const Color(0xFF5B3DF5).withOpacity(0.12),
                                      blurRadius: 8,
                                    ),
                                  ],
                                ),
                                child: const Icon(
                                  Icons.notifications_rounded,
                                  color: Color(0xFF5B3DF5),
                                  size: 20,
                                ),
                              ),
                              Positioned(
                                top: 0,
                                right: 0,
                                child: Container(
                                  width: 9,
                                  height: 9,
                                  decoration: const BoxDecoration(
                                    color: Color(0xFFFF5252),
                                    shape: BoxShape.circle,
                                  ),
                                ),
                              ),
                            ],
                          ),

                          const SizedBox(width: 10),

                          // Profile Avatar with green online dot
                          Stack(
                            clipBehavior: Clip.none,
                            children: [
                              Container(
                                width: 40,
                                height: 40,
                                decoration: BoxDecoration(
                                  shape: BoxShape.circle,
                                  border: Border.all(color: Colors.white, width: 2),
                                  boxShadow: [
                                    BoxShadow(
                                      color: Colors.black.withOpacity(0.1),
                                      blurRadius: 6,
                                    ),
                                  ],
                                ),
                                child: ClipOval(
                                  child: Image.asset(
                                    'assets/images/avatar_partner_demo.png',
                                    fit: BoxFit.cover,
                                    errorBuilder: (_, __, ___) => Container(
                                      color: const Color(0xFF5B3DF5),
                                      child: const Icon(Icons.person,
                                          color: Colors.white, size: 22),
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
                        ],
                      ),
                    ],
                  ),

                  const SizedBox(height: 16),

                  // B. Partner Card (Main Hero Highlight)
                  Container(
                    width: double.infinity,
                    height: 165,
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(26),
                      gradient: const LinearGradient(
                        colors: [
                          Color(0xFF7C4DFF),
                          Color(0xFFB057F7),
                          Color(0xFFFF75A0),
                        ],
                        begin: Alignment.topLeft,
                        end: Alignment.bottomRight,
                      ),
                      boxShadow: [
                        BoxShadow(
                          color: const Color(0xFF7C4DFF).withOpacity(0.35),
                          blurRadius: 20,
                          offset: const Offset(0, 8),
                        ),
                      ],
                    ),
                    child: Stack(
                      children: [
                        // Background twilight glow illustration
                        Positioned.fill(
                          child: ClipRRect(
                            borderRadius: BorderRadius.circular(26),
                            child: Image.asset(
                              'assets/images/duora_bg.png',
                              fit: BoxFit.cover,
                              errorBuilder: (_, __, ___) => const SizedBox(),
                            ),
                          ),
                        ),

                        // Soft dark gradient overlay for high contrast
                        Positioned.fill(
                          child: Container(
                            decoration: BoxDecoration(
                              borderRadius: BorderRadius.circular(26),
                              gradient: LinearGradient(
                                colors: [
                                  const Color(0xFF211452).withOpacity(0.75),
                                  const Color(0xFF5B3DF5).withOpacity(0.4),
                                ],
                                begin: Alignment.centerLeft,
                                end: Alignment.centerRight,
                              ),
                            ),
                          ),
                        ),

                        // Glowing dual hearts illustration decoration in top right
                        Positioned(
                          right: 18,
                          top: 14,
                          child: Icon(
                            Icons.favorite_rounded,
                            size: 44,
                            color: const Color(0xFFFFB6D5).withOpacity(0.45),
                          ),
                        ),

                        // Card Content
                        Padding(
                          padding: const EdgeInsets.all(18),
                          child: Row(
                            children: [
                              // Partner DP
                              Stack(
                                children: [
                                  Container(
                                    width: 60,
                                    height: 60,
                                    decoration: BoxDecoration(
                                      shape: BoxShape.circle,
                                      border: Border.all(
                                          color: Colors.white, width: 2.5),
                                      boxShadow: [
                                        BoxShadow(
                                          color: Colors.black.withOpacity(0.2),
                                          blurRadius: 8,
                                        ),
                                      ],
                                    ),
                                    child: ClipOval(
                                      child: Image.asset(
                                        'assets/images/avatar_partner_demo.png',
                                        fit: BoxFit.cover,
                                        errorBuilder: (_, __, ___) => Container(
                                          color: const Color(0xFFFF2D8D),
                                          child: const Icon(Icons.person,
                                              color: Colors.white, size: 32),
                                        ),
                                      ),
                                    ),
                                  ),
                                  Positioned(
                                    bottom: 0,
                                    right: 2,
                                    child: Container(
                                      width: 14,
                                      height: 14,
                                      decoration: BoxDecoration(
                                        color: const Color(0xFF10B981),
                                        shape: BoxShape.circle,
                                        border: Border.all(
                                            color: Colors.white, width: 2),
                                      ),
                                    ),
                                  ),
                                ],
                              ),

                              const SizedBox(width: 14),

                              // Partner Info
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  mainAxisAlignment: MainAxisAlignment.center,
                                  children: [
                                    Row(
                                      children: [
                                        Text(
                                          user?.partnerName ?? 'My Partner',
                                          style: const TextStyle(
                                            fontSize: 18,
                                            fontWeight: FontWeight.w800,
                                            color: Colors.white,
                                          ),
                                        ),
                                        const SizedBox(width: 6),
                                        const Icon(
                                          Icons.verified_rounded,
                                          color: Color(0xFFB057F7),
                                          size: 18,
                                        ),
                                      ],
                                    ),
                                    const SizedBox(height: 4),
                                    Row(
                                      children: [
                                        Container(
                                          width: 7,
                                          height: 7,
                                          decoration: const BoxDecoration(
                                            color: Color(0xFF10B981),
                                            shape: BoxShape.circle,
                                          ),
                                        ),
                                        const SizedBox(width: 6),
                                        const Text(
                                          'Online now',
                                          style: TextStyle(
                                            fontSize: 12,
                                            color: Color(0xFFE8DEFF),
                                            fontWeight: FontWeight.w500,
                                          ),
                                        ),
                                      ],
                                    ),
                                  ],
                                ),
                              ),

                              // Quick Call Action Buttons
                              Row(
                                mainAxisSize: MainAxisSize.min,
                                children: [
                                  // Voice Call Circle Button
                                  _QuickCallCircleBtn(
                                    icon: Icons.phone_rounded,
                                    onTap: () {
                                      final sig = Provider.of<SignalingService>(
                                          context,
                                          listen: false);
                                      Navigator.push(
                                        context,
                                        MaterialPageRoute(
                                          builder: (_) => ActiveCallScreen(
                                            partnerName: user?.partnerName ??
                                                'My Partner',
                                            partnerCode:
                                                user?.partnerPairCode ?? 'LOVE01',
                                            isVideo: false,
                                            signalingService: sig,
                                          ),
                                        ),
                                      );
                                    },
                                  ),
                                  const SizedBox(width: 10),
                                  // Video Call Circle Button
                                  _QuickCallCircleBtn(
                                    icon: Icons.videocam_rounded,
                                    onTap: () {
                                      final sig = Provider.of<SignalingService>(
                                          context,
                                          listen: false);
                                      Navigator.push(
                                        context,
                                        MaterialPageRoute(
                                          builder: (_) => ActiveCallScreen(
                                            partnerName: user?.partnerName ??
                                                'My Partner',
                                            partnerCode:
                                                user?.partnerPairCode ?? 'LOVE01',
                                            isVideo: true,
                                            signalingService: sig,
                                          ),
                                        ),
                                      );
                                    },
                                  ),
                                ],
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),

                  const SizedBox(height: 18),

                  // C. Quick Actions Grid (2 rows x 3 columns)
                  Row(
                    children: [
                      Expanded(
                        child: _ActionTile(
                          title: 'Chat',
                          icon: Icons.chat_bubble_rounded,
                          badgeCount: 3,
                          onTap: () => onNavigateTab(DuoraTab.chat),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: _ActionTile(
                          title: 'Notes',
                          icon: Icons.edit_calendar_rounded,
                          onTap: () => onNavigateTab(DuoraTab.notes),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: _ActionTile(
                          title: 'Call Logs',
                          icon: Icons.phone_callback_rounded,
                          onTap: () => onNavigateTab(DuoraTab.call),
                        ),
                      ),
                    ],
                  ),

                  const SizedBox(height: 12),

                  Row(
                    children: [
                      Expanded(
                        child: _ActionTile(
                          title: 'Media',
                          icon: Icons.photo_library_rounded,
                          onTap: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                  builder: (_) => const MemoriesScreen()),
                            );
                          },
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: _ActionTile(
                          title: 'Shared',
                          icon: Icons.link_rounded,
                          onTap: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                  builder: (_) =>
                                      const PartnerPairingScreen()),
                            );
                          },
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: _ActionTile(
                          title: 'Our Space',
                          icon: Icons.favorite_rounded,
                          onTap: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                  builder: (_) => const ServerHealthScreen()),
                            );
                          },
                        ),
                      ),
                    ],
                  ),

                  const SizedBox(height: 18),

                  // D. Connection Status Card
                  Container(
                    width: double.infinity,
                    padding: const EdgeInsets.symmetric(
                        horizontal: 18, vertical: 14),
                    decoration: BoxDecoration(
                      gradient: const LinearGradient(
                        colors: [
                          Color(0xFF904BE8),
                          Color(0xFF5B3DF5),
                        ],
                        begin: Alignment.centerLeft,
                        end: Alignment.centerRight,
                      ),
                      borderRadius: BorderRadius.circular(22),
                      boxShadow: [
                        BoxShadow(
                          color: const Color(0xFF5B3DF5).withOpacity(0.3),
                          blurRadius: 16,
                          offset: const Offset(0, 4),
                        ),
                      ],
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: const [
                            Row(
                              children: [
                                Text(
                                  'Connected',
                                  style: TextStyle(
                                    fontSize: 16,
                                    fontWeight: FontWeight.w800,
                                    color: Colors.white,
                                  ),
                                ),
                                SizedBox(width: 6),
                                Icon(Icons.link_rounded,
                                    color: Colors.white, size: 16),
                              ],
                            ),
                            SizedBox(height: 2),
                            Text(
                              'Your bond is active 🤍',
                              style: TextStyle(
                                fontSize: 12,
                                color: Color(0xFFE8DEFF),
                              ),
                            ),
                          ],
                        ),

                        // Center glowing heart icon
                        const Icon(
                          Icons.favorite_rounded,
                          color: Color(0xFFFFB6D5),
                          size: 26,
                        ),

                        // Since date pill
                        Container(
                          padding: const EdgeInsets.symmetric(
                              horizontal: 12, vertical: 6),
                          decoration: BoxDecoration(
                            color: Colors.white.withOpacity(0.2),
                            borderRadius: BorderRadius.circular(16),
                          ),
                          child: const Text(
                            'Since 12 Oct 2026',
                            style: TextStyle(
                              fontSize: 11,
                              fontWeight: FontWeight.bold,
                              color: Colors.white,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),

                  const SizedBox(height: 100), // Space for bottom bar
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _QuickCallCircleBtn extends StatelessWidget {
  final IconData icon;
  final VoidCallback onTap;

  const _QuickCallCircleBtn({required this.icon, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 44,
        height: 44,
        decoration: BoxDecoration(
          color: Colors.white,
          shape: BoxShape.circle,
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.12),
              blurRadius: 8,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Center(
          child: Icon(
            icon,
            color: const Color(0xFF5B3DF5),
            size: 22,
          ),
        ),
      ),
    );
  }
}

class _ActionTile extends StatelessWidget {
  final String title;
  final IconData icon;
  final int badgeCount;
  final VoidCallback onTap;

  const _ActionTile({
    required this.title,
    required this.icon,
    this.badgeCount = 0,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 18),
        decoration: BoxDecoration(
          color: const Color(0xFFF7F4FF),
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: Colors.white, width: 1.5),
          boxShadow: [
            BoxShadow(
              color: const Color(0xFF5B3DF5).withOpacity(0.06),
              blurRadius: 10,
              offset: const Offset(0, 4),
            ),
          ],
        ),
        child: Column(
          children: [
            Stack(
              clipBehavior: Clip.none,
              children: [
                Icon(
                  icon,
                  size: 26,
                  color: const Color(0xFF5B3DF5),
                ),
                if (badgeCount > 0)
                  Positioned(
                    top: -4,
                    right: -8,
                    child: Container(
                      padding: const EdgeInsets.all(4),
                      decoration: const BoxDecoration(
                        color: Color(0xFFFF2D8D),
                        shape: BoxShape.circle,
                      ),
                      child: Text(
                        badgeCount.toString(),
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 9,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                  ),
              ],
            ),
            const SizedBox(height: 8),
            Text(
              title,
              style: const TextStyle(
                fontSize: 12.5,
                fontWeight: FontWeight.w700,
                color: Color(0xFF211452),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
