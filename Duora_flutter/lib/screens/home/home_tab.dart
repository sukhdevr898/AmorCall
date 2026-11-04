import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/duora_theme.dart';
import '../../services/auth_service.dart';
import '../../services/signaling_service.dart';
import '../navigation/duora_bottom_bar.dart';
import '../pairing/partner_pairing_screen.dart';
import '../call/active_call_screen.dart';
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
      backgroundColor: const Color(0xFFF9F7FB),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 12),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // 1. Top Bar: Duora Logo + Notification Bell & Partner Avatar
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  // Duora Logo (Always Together)
                  SizedBox(
                    height: 42,
                    child: Image.asset(
                      'assets/images/logo_full_duora.png',
                      fit: BoxFit.contain,
                      errorBuilder: (_, __, ___) => const Row(
                        children: [
                          Text(
                            'Du',
                            style: TextStyle(
                              fontSize: 24,
                              fontWeight: FontWeight.w900,
                              color: Color(0xFF1F1135),
                            ),
                          ),
                          Icon(Icons.favorite, color: Color(0xFFFF2D8D), size: 20),
                          Text(
                            'ra',
                            style: TextStyle(
                              fontSize: 24,
                              fontWeight: FontWeight.w900,
                              color: Color(0xFF1F1135),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),

                  // Actions: Notification Bell & Partner Avatar
                  Row(
                    children: [
                      // Notification Bell with unread dot
                      Stack(
                        children: [
                          Container(
                            width: 38,
                            height: 38,
                            decoration: BoxDecoration(
                              color: Colors.white,
                              shape: BoxShape.circle,
                              border: Border.all(color: Colors.grey.shade200),
                              boxShadow: [
                                BoxShadow(
                                  color: Colors.black.withOpacity(0.04),
                                  blurRadius: 6,
                                ),
                              ],
                            ),
                            child: const Icon(Icons.notifications_none_rounded,
                                color: Color(0xFF1F1135), size: 20),
                          ),
                          Positioned(
                            top: 2,
                            right: 2,
                            child: Container(
                              width: 9,
                              height: 9,
                              decoration: const BoxDecoration(
                                color: Color(0xFFFF2D8D),
                                shape: BoxShape.circle,
                              ),
                            ),
                          ),
                        ],
                      ),

                      const SizedBox(width: 10),

                      // Partner Avatar with green online dot
                      Stack(
                        children: [
                          Container(
                            width: 40,
                            height: 40,
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
                                  child: const Icon(Icons.person,
                                      color: Colors.white, size: 22),
                                ),
                              ),
                            ),
                          ),
                          Positioned(
                            bottom: 1,
                            right: 1,
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

              // 2. Sunset Couple Hero Card
              Container(
                width: double.infinity,
                height: 165,
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(24),
                  boxShadow: [
                    BoxShadow(
                      color: const Color(0xFFFF2D8D).withOpacity(0.2),
                      blurRadius: 18,
                      offset: const Offset(0, 6),
                    ),
                  ],
                ),
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(24),
                  child: Stack(
                    children: [
                      // Sunset couple background image
                      Positioned.fill(
                        child: Image.asset(
                          'assets/images/img_home_hero_sunset_clean.png',
                          fit: BoxFit.cover,
                          errorBuilder: (_, __, ___) => Image.asset(
                            'assets/images/img_home_hero_sunset.png',
                            fit: BoxFit.cover,
                            errorBuilder: (_, __, ___) => Container(
                              decoration: const BoxDecoration(
                                gradient: LinearGradient(
                                  colors: [Color(0xFF831843), Color(0xFFBE185D)],
                                ),
                              ),
                            ),
                          ),
                        ),
                      ),

                      // Gradient overlay for text readability
                      Positioned.fill(
                        child: Container(
                          decoration: BoxDecoration(
                            gradient: LinearGradient(
                              colors: [
                                Colors.black.withOpacity(0.35),
                                Colors.black.withOpacity(0.15),
                              ],
                              begin: Alignment.centerLeft,
                              end: Alignment.centerRight,
                            ),
                          ),
                        ),
                      ),

                      // Text inside Hero Card
                      Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 22),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: const [
                            Text(
                              'Good Morning',
                              style: TextStyle(
                                color: Colors.white,
                                fontSize: 13,
                                fontWeight: FontWeight.w500,
                                shadows: [Shadow(color: Colors.black45, blurRadius: 4)],
                              ),
                            ),
                            SizedBox(height: 4),
                            Text(
                              'My Love 💕',
                              style: TextStyle(
                                color: Colors.white,
                                fontSize: 24,
                                fontWeight: FontWeight.w800,
                                shadows: [Shadow(color: Colors.black45, blurRadius: 6)],
                              ),
                            ),
                            SizedBox(height: 8),
                            Text(
                              'Distance means nothing\nwhen we are connected ❤️',
                              style: TextStyle(
                                color: Colors.white,
                                fontSize: 12,
                                height: 1.3,
                                fontWeight: FontWeight.w400,
                                shadows: [Shadow(color: Colors.black45, blurRadius: 4)],
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),

              const SizedBox(height: 20),

              // 3. 6 Core Feature Grid (Chat, Call, Notes, Call Logs, Partner, Server)
              Row(
                children: [
                  Expanded(
                    child: _HomeFeatureItem(
                      title: 'Chat',
                      badgeCount: 3,
                      iconWidget: Container(
                        width: 32,
                        height: 32,
                        decoration: const BoxDecoration(
                          color: Color(0xFFFF2D8D),
                          shape: BoxShape.circle,
                        ),
                        child: const Icon(Icons.chat_bubble_rounded,
                            color: Colors.white, size: 16),
                      ),
                      onTap: () => onNavigateTab(DuoraTab.chat),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: _HomeFeatureItem(
                      title: 'Call',
                      iconWidget: Container(
                        width: 32,
                        height: 32,
                        decoration: BoxDecoration(
                          color: const Color(0xFF10B981).withOpacity(0.15),
                          shape: BoxShape.circle,
                        ),
                        child: const Icon(Icons.phone_rounded,
                            color: Color(0xFF10B981), size: 18),
                      ),
                      onTap: () {
                        final sig = Provider.of<SignalingService>(context, listen: false);
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (_) => ActiveCallScreen(
                              partnerName: user?.partnerName ?? 'My Partner',
                              partnerCode: user?.partnerPairCode ?? 'PARTNER',
                              isVideo: false,
                              signalingService: sig,
                            ),
                          ),
                        );
                      },
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: _HomeFeatureItem(
                      title: 'Notes',
                      iconWidget: Container(
                        width: 32,
                        height: 32,
                        decoration: BoxDecoration(
                          color: const Color(0xFFF59E0B).withOpacity(0.15),
                          shape: BoxShape.circle,
                        ),
                        child: const Icon(Icons.description_rounded,
                            color: Color(0xFFF59E0B), size: 18),
                      ),
                      onTap: () => onNavigateTab(DuoraTab.notes),
                    ),
                  ),
                ],
              ),

              const SizedBox(height: 12),

              Row(
                children: [
                  Expanded(
                    child: _HomeFeatureItem(
                      title: 'Call Logs',
                      iconWidget: Container(
                        width: 32,
                        height: 32,
                        decoration: BoxDecoration(
                          color: const Color(0xFF8B5CF6).withOpacity(0.15),
                          shape: BoxShape.circle,
                        ),
                        child: const Icon(Icons.access_time_rounded,
                            color: Color(0xFF8B5CF6), size: 18),
                      ),
                      onTap: () => onNavigateTab(DuoraTab.call),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: _HomeFeatureItem(
                      title: 'Partner',
                      iconWidget: Container(
                        width: 32,
                        height: 32,
                        decoration: BoxDecoration(
                          color: const Color(0xFFFF2D8D).withOpacity(0.15),
                          shape: BoxShape.circle,
                        ),
                        child: const Icon(Icons.favorite_rounded,
                            color: Color(0xFFFF2D8D), size: 18),
                      ),
                      onTap: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                              builder: (_) => const PartnerPairingScreen()),
                        );
                      },
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: _HomeFeatureItem(
                      title: 'Server',
                      iconWidget: Container(
                        width: 32,
                        height: 32,
                        decoration: BoxDecoration(
                          color: const Color(0xFF38BDF8).withOpacity(0.15),
                          shape: BoxShape.circle,
                        ),
                        child: const Icon(Icons.layers_rounded,
                            color: Color(0xFF38BDF8), size: 18),
                      ),
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

              const SizedBox(height: 24),

              // 4. "Our Moments" Section
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    'Our Moments',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w800,
                      color: Color(0xFF1A1429),
                    ),
                  ),
                  GestureDetector(
                    onTap: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                            builder: (_) => const MemoriesScreen()),
                      );
                    },
                    child: const Text(
                      'See All >',
                      style: TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.bold,
                        color: Color(0xFFFF2D8D),
                      ),
                    ),
                  ),
                ],
              ),

              const SizedBox(height: 12),

              // Horizontal Moments Gallery List
              SizedBox(
                height: 90,
                child: ListView(
                  scrollDirection: Axis.horizontal,
                  children: [
                    _MomentPhotoCard(assetPath: 'assets/images/moment_couple.png'),
                    const SizedBox(width: 12),
                    _MomentPhotoCard(assetPath: 'assets/images/moment_heart_lake.png'),
                    const SizedBox(width: 12),
                    _MomentPhotoCard(assetPath: 'assets/images/moment_pink_garden.png'),
                    const SizedBox(width: 12),
                    // "+" Add moment card
                    GestureDetector(
                      onTap: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                              builder: (_) => const MemoriesScreen()),
                        );
                      },
                      child: Container(
                        width: 75,
                        height: 90,
                        decoration: BoxDecoration(
                          color: Colors.white,
                          borderRadius: BorderRadius.circular(16),
                          border: Border.all(color: Colors.grey.shade300, width: 1.2),
                        ),
                        child: const Center(
                          child: Icon(Icons.add_rounded,
                              color: Color(0xFF1F1135), size: 28),
                        ),
                      ),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 24),
            ],
          ),
        ),
      ),
    );
  }
}

class _HomeFeatureItem extends StatelessWidget {
  final String title;
  final Widget iconWidget;
  final int badgeCount;
  final VoidCallback onTap;

  const _HomeFeatureItem({
    required this.title,
    required this.iconWidget,
    this.badgeCount = 0,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 16),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: Colors.grey.shade200, width: 1),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.03),
              blurRadius: 10,
              offset: const Offset(0, 3),
            ),
          ],
        ),
        child: Column(
          children: [
            Stack(
              clipBehavior: Clip.none,
              children: [
                iconWidget,
                if (badgeCount > 0)
                  Positioned(
                    top: -4,
                    right: -6,
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
                fontSize: 12,
                fontWeight: FontWeight.w600,
                color: Color(0xFF1F1135),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _MomentPhotoCard extends StatelessWidget {
  final String assetPath;
  const _MomentPhotoCard({required this.assetPath});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 75,
      height: 90,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.08),
            blurRadius: 6,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(16),
        child: Image.asset(
          assetPath,
          fit: BoxFit.cover,
          errorBuilder: (_, __, ___) => Container(
            color: const Color(0xFFFFB6C1),
            child: const Icon(Icons.favorite, color: Colors.white),
          ),
        ),
      ),
    );
  }
}
