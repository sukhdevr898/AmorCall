import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import '../../core/theme/duora_theme.dart';
import '../../services/auth_service.dart';
import '../../services/signaling_service.dart';
import '../navigation/duora_bottom_bar.dart';
import '../pairing/partner_pairing_screen.dart';
import '../call/active_call_screen.dart';
import '../memories/memories_screen.dart';

class HomeTab extends StatelessWidget {
  final ValueChanged<DuoraTab> onNavigateTab;

  const HomeTab({super.key, required this.onNavigateTab});

  @override
  Widget build(BuildContext context) {
    final authService = Provider.of<AuthService>(context);
    final user = authService.currentUser;

    final daysTogether = user != null
        ? DateTime.now().difference(user.relationshipStartDate).inDays
        : 520;

    return Scaffold(
      backgroundColor: DuoraColors.background,
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Top Header
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Row(
                    children: [
                      Container(
                        width: 44,
                        height: 44,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          gradient: DuoraColors.primaryGradient,
                          boxShadow: [
                            BoxShadow(
                              color: DuoraColors.primaryPink.withOpacity(0.3),
                              blurRadius: 10,
                            ),
                          ],
                        ),
                        child: Center(
                          child: Text(
                            (user?.fullName.isNotEmpty == true
                                    ? user!.fullName[0]
                                    : 'Y')
                                .toUpperCase(),
                            style: const TextStyle(
                              color: Colors.white,
                              fontWeight: FontWeight.bold,
                              fontSize: 18,
                            ),
                          ),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              Text(
                                user?.partnerName ?? 'My Partner',
                                style: const TextStyle(
                                  color: DuoraColors.textPrimary,
                                  fontSize: 16,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                              if (user?.hasBlueTick == true) ...[
                                const SizedBox(width: 4),
                                const Icon(Icons.verified_rounded,
                                    color: DuoraColors.verifiedBlue, size: 16),
                              ],
                            ],
                          ),
                          Row(
                            children: [
                              Container(
                                width: 8,
                                height: 8,
                                decoration: const BoxDecoration(
                                  shape: BoxShape.circle,
                                  color: DuoraColors.onlineGreen,
                                ),
                              ),
                              const SizedBox(width: 6),
                              const Text(
                                'Connected & Online',
                                style: TextStyle(
                                  color: DuoraColors.textSecondary,
                                  fontSize: 12,
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ],
                  ),
                  Container(
                    decoration: BoxDecoration(
                      color: DuoraColors.surface,
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(color: DuoraColors.borderMuted),
                    ),
                    child: IconButton(
                      icon: const Icon(Icons.notifications_outlined,
                          color: DuoraColors.textPrimary, size: 22),
                      onPressed: () {
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(
                            backgroundColor: DuoraColors.surfaceCard,
                            content: Text('No new notifications'),
                          ),
                        );
                      },
                    ),
                  ),
                ],
              ),

              const SizedBox(height: 20),

              // Romantic Sunset Hero Banner with Days Counter
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(22),
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(24),
                  gradient: const LinearGradient(
                    colors: [
                      Color(0xFF831843),
                      Color(0xFFBE185D),
                      Color(0xFF4C0519),
                    ],
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
                  boxShadow: [
                    BoxShadow(
                      color: const Color(0xFFBE185D).withOpacity(0.35),
                      blurRadius: 24,
                      offset: const Offset(0, 8),
                    ),
                  ],
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Container(
                          padding: const EdgeInsets.symmetric(
                              horizontal: 10, vertical: 4),
                          decoration: BoxDecoration(
                            color: Colors.black.withOpacity(0.25),
                            borderRadius: BorderRadius.circular(20),
                          ),
                          child: const Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Icon(Icons.favorite_rounded,
                                  color: DuoraColors.softPink, size: 14),
                              SizedBox(width: 6),
                              Text(
                                'Together in Love',
                                style: TextStyle(
                                  color: Colors.white,
                                  fontSize: 12,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                            ],
                          ),
                        ),
                        const Icon(Icons.auto_awesome,
                            color: Colors.amberAccent, size: 20),
                      ],
                    ),
                    const SizedBox(height: 18),
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.baseline,
                      textBaseline: TextBaseline.alphabetic,
                      children: [
                        Text(
                          '$daysTogether',
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 48,
                            fontWeight: FontWeight.w800,
                            letterSpacing: -1,
                          ),
                        ),
                        const SizedBox(width: 8),
                        const Text(
                          'DAYS',
                          style: TextStyle(
                            color: Colors.white70,
                            fontSize: 18,
                            fontWeight: FontWeight.bold,
                            letterSpacing: 2,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 6),
                    Text(
                      user?.statusMsg ?? 'Thinking of you always ❤️',
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 14,
                        fontStyle: FontStyle.italic,
                      ),
                    ),
                    const SizedBox(height: 18),
                    // Quick Love Reaction Buttons
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceAround,
                      children: [
                        _QuickReactionBtn(
                          emoji: '❤️',
                          label: 'Heart',
                          onTap: () => _showReactionToast(context, 'Heart sent to partner! ❤️'),
                        ),
                        _QuickReactionBtn(
                          emoji: '😘',
                          label: 'Kiss',
                          onTap: () => _showReactionToast(context, 'Kiss sent! 😘'),
                        ),
                        _QuickReactionBtn(
                          emoji: '🤗',
                          label: 'Hug',
                          onTap: () => _showReactionToast(context, 'Warm hug sent! 🤗'),
                        ),
                        _QuickReactionBtn(
                          emoji: '👉',
                          label: 'Poke',
                          onTap: () => _showReactionToast(context, 'Poked your partner! 👉'),
                        ),
                      ],
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 22),

              // Pair Code / Status Card
              GestureDetector(
                onTap: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(builder: (_) => const PartnerPairingScreen()),
                  );
                },
                child: Container(
                  width: double.infinity,
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
                  decoration: BoxDecoration(
                    color: DuoraColors.surfaceCard,
                    borderRadius: BorderRadius.circular(18),
                    border: Border.all(color: DuoraColors.border),
                  ),
                  child: Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(10),
                        decoration: BoxDecoration(
                          color: DuoraColors.primaryPink.withOpacity(0.15),
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: const Icon(Icons.link_rounded,
                            color: DuoraColors.primaryPink, size: 24),
                      ),
                      const SizedBox(width: 14),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text(
                              'My Pair Code (Tap to Pair)',
                              style: TextStyle(
                                color: DuoraColors.textSecondary,
                                fontSize: 12,
                              ),
                            ),
                            Text(
                              user?.myPairCode.isNotEmpty == true
                                  ? user!.myPairCode
                                  : 'A9F2X1',
                              style: const TextStyle(
                                color: DuoraColors.textPrimary,
                                fontSize: 18,
                                fontWeight: FontWeight.bold,
                                letterSpacing: 2,
                              ),
                            ),
                          ],
                        ),
                      ),
                      IconButton(
                        icon: const Icon(Icons.qr_code_rounded,
                            color: DuoraColors.primaryPink, size: 22),
                        onPressed: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(builder: (_) => const PartnerPairingScreen()),
                          );
                        },
                      ),
                    ],
                  ),
                ),
              ),

              const SizedBox(height: 24),

              // Core Feature Grid
              const Text(
                'Private Space',
                style: TextStyle(
                  color: DuoraColors.textPrimary,
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 14),
              GridView.count(
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                crossAxisCount: 2,
                crossAxisSpacing: 14,
                mainAxisSpacing: 14,
                childAspectRatio: 1.25,
                children: [
                  _FeatureCard(
                    title: 'Voice Hotline',
                    subtitle: 'Private encrypted call',
                    icon: Icons.phone_in_talk_rounded,
                    accentColor: const Color(0xFF10B981),
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
                  _FeatureCard(
                    title: 'Video Hotline',
                    subtitle: 'Real-time face-to-face',
                    icon: Icons.videocam_rounded,
                    accentColor: DuoraColors.primaryPink,
                    onTap: () {
                      final sig = Provider.of<SignalingService>(context, listen: false);
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) => ActiveCallScreen(
                            partnerName: user?.partnerName ?? 'My Partner',
                            partnerCode: user?.partnerPairCode ?? 'PARTNER',
                            isVideo: true,
                            signalingService: sig,
                          ),
                        ),
                      );
                    },
                  ),
                  _FeatureCard(
                    title: 'Love Chat',
                    subtitle: 'Disappearing messages',
                    icon: Icons.chat_rounded,
                    accentColor: const Color(0xFF38BDF8),
                    onTap: () => onNavigateTab(DuoraTab.chat),
                  ),
                  _FeatureCard(
                    title: 'Couple Notes',
                    subtitle: 'Memories & promises',
                    icon: Icons.favorite_border_rounded,
                    accentColor: const Color(0xFFA855F7),
                    onTap: () => onNavigateTab(DuoraTab.notes),
                  ),
                  _FeatureCard(
                    title: 'Our Moments',
                    subtitle: 'Couple photo vault',
                    icon: Icons.photo_library_rounded,
                    accentColor: Colors.amberAccent,
                    onTap: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(builder: (_) => const MemoriesScreen()),
                      );
                    },
                  ),
                ],
              ),
              const SizedBox(height: 24),
            ],
          ),
        ),
      ),
    );
  }

  void _showReactionToast(BuildContext context, String msg) {
    ScaffoldMessenger.of(context).hideCurrentSnackBar();
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        backgroundColor: DuoraColors.surfaceCard,
        duration: const Duration(seconds: 1),
        content: Text(msg, style: const TextStyle(color: Colors.white)),
      ),
    );
  }
}

class _QuickReactionBtn extends StatelessWidget {
  final String emoji;
  final String label;
  final VoidCallback onTap;

  const _QuickReactionBtn({
    required this.emoji,
    required this.label,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
        decoration: BoxDecoration(
          color: Colors.black.withOpacity(0.25),
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: Colors.white.withOpacity(0.15)),
        ),
        child: Column(
          children: [
            Text(emoji, style: const TextStyle(fontSize: 20)),
            const SizedBox(height: 2),
            Text(
              label,
              style: const TextStyle(
                color: Colors.white,
                fontSize: 10,
                fontWeight: FontWeight.w600,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _FeatureCard extends StatelessWidget {
  final String title;
  final String subtitle;
  final IconData icon;
  final Color accentColor;
  final VoidCallback onTap;

  const _FeatureCard({
    required this.title,
    required this.subtitle,
    required this.icon,
    required this.accentColor,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: DuoraColors.surfaceCard,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: DuoraColors.borderMuted),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Container(
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                color: accentColor.withOpacity(0.15),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Icon(icon, color: accentColor, size: 22),
            ),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    color: DuoraColors.textPrimary,
                    fontSize: 14,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  subtitle,
                  style: const TextStyle(
                    color: DuoraColors.textMuted,
                    fontSize: 11,
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
