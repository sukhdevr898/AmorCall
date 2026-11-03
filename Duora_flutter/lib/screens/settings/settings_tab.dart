import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/duora_theme.dart';
import '../../services/auth_service.dart';
import '../auth/auth_screen.dart';
import 'avatar_selection_screen.dart';
import 'notification_settings_screen.dart';
import 'server_health_screen.dart';

class SettingsTab extends StatefulWidget {
  const SettingsTab({super.key});

  @override
  State<SettingsTab> createState() => _SettingsTabState();
}

class _SettingsTabState extends State<SettingsTab> {
  String _selectedDisguise = 'Default (Duora)';
  bool _appLockEnabled = false;
  bool _blurNotifications = true;

  @override
  Widget build(BuildContext context) {
    final authService = Provider.of<AuthService>(context);
    final user = authService.currentUser;

    return Scaffold(
      backgroundColor: DuoraColors.background,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: const Text(
          'Settings & Privacy',
          style: TextStyle(
            color: DuoraColors.textPrimary,
            fontWeight: FontWeight.bold,
            fontSize: 20,
          ),
        ),
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
          children: [
            // User Profile Card (Clickable to change Avatar)
            GestureDetector(
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (_) => const AvatarSelectionScreen()),
                );
              },
              child: Container(
                padding: const EdgeInsets.all(18),
                decoration: BoxDecoration(
                  color: DuoraColors.surfaceCard,
                  borderRadius: BorderRadius.circular(22),
                  border: Border.all(color: DuoraColors.borderMuted),
                ),
                child: Row(
                  children: [
                    Stack(
                      children: [
                        Container(
                          width: 58,
                          height: 58,
                          decoration: const BoxDecoration(
                            shape: BoxShape.circle,
                            gradient: DuoraColors.primaryGradient,
                          ),
                          child: Center(
                            child: Text(
                              (user?.fullName.isNotEmpty == true ? user!.fullName[0] : 'U')
                                  .toUpperCase(),
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 24,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                        ),
                        Positioned(
                          bottom: 0,
                          right: 0,
                          child: Container(
                            padding: const EdgeInsets.all(3),
                            decoration: const BoxDecoration(
                              shape: BoxShape.circle,
                              color: DuoraColors.primaryPink,
                            ),
                            child: const Icon(Icons.edit, size: 12, color: Colors.white),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              Text(
                                user?.fullName ?? 'Duora User',
                                style: const TextStyle(
                                  color: DuoraColors.textPrimary,
                                  fontSize: 17,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                              if (user?.hasBlueTick == true) ...[
                                const SizedBox(width: 4),
                                const Icon(Icons.verified_rounded,
                                    color: DuoraColors.verifiedBlue, size: 18),
                              ],
                            ],
                          ),
                          Text(
                            user?.email ?? 'user@duora.app',
                            style: const TextStyle(
                              color: DuoraColors.textMuted,
                              fontSize: 13,
                            ),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            'Pair Code: ${user?.myPairCode ?? "A9F2X1"}',
                            style: const TextStyle(
                              color: DuoraColors.primaryPink,
                              fontSize: 12,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ],
                      ),
                    ),
                    const Icon(Icons.chevron_right_rounded, color: DuoraColors.textMuted),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 24),

            // Preferences & Notifications
            const Text(
              'Preferences',
              style: TextStyle(
                color: DuoraColors.textPrimary,
                fontSize: 15,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 10),
            Container(
              decoration: BoxDecoration(
                color: DuoraColors.surfaceCard,
                borderRadius: BorderRadius.circular(18),
                border: Border.all(color: DuoraColors.borderMuted),
              ),
              child: ListTile(
                leading: const Icon(Icons.notifications_active_outlined,
                    color: DuoraColors.primaryPink),
                title: const Text('Notifications & Ringtones',
                    style: TextStyle(color: Colors.white, fontSize: 14)),
                subtitle: const Text('Ringtones, alerts, and vibration patterns',
                    style: TextStyle(color: DuoraColors.textSecondary, fontSize: 12)),
                trailing: const Icon(Icons.chevron_right_rounded,
                    color: DuoraColors.textMuted),
                onTap: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                        builder: (_) => const NotificationSettingsScreen()),
                  );
                },
              ),
            ),

            const SizedBox(height: 24),

            // Privacy & Disguise Section
            const Text(
              'Privacy Camouflage',
              style: TextStyle(
                color: DuoraColors.textPrimary,
                fontSize: 15,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 10),
            Container(
              decoration: BoxDecoration(
                color: DuoraColors.surfaceCard,
                borderRadius: BorderRadius.circular(18),
                border: Border.all(color: DuoraColors.borderMuted),
              ),
              child: Column(
                children: [
                  ListTile(
                    leading: const Icon(Icons.visibility_off_outlined,
                        color: DuoraColors.primaryPink),
                    title: const Text('App Icon Disguise',
                        style: TextStyle(color: Colors.white, fontSize: 14)),
                    subtitle: Text(_selectedDisguise,
                        style: const TextStyle(
                            color: DuoraColors.textSecondary, fontSize: 12)),
                    trailing: const Icon(Icons.chevron_right_rounded,
                        color: DuoraColors.textMuted),
                    onTap: _showDisguisePicker,
                  ),
                  const Divider(color: DuoraColors.divider, height: 1),
                  SwitchListTile(
                    activeColor: DuoraColors.primaryPink,
                    secondary: const Icon(Icons.lock_outline_rounded,
                        color: DuoraColors.primaryPink),
                    title: const Text('App Lock (PIN)',
                        style: TextStyle(color: Colors.white, fontSize: 14)),
                    value: _appLockEnabled,
                    onChanged: (val) => setState(() => _appLockEnabled = val),
                  ),
                  const Divider(color: DuoraColors.divider, height: 1),
                  SwitchListTile(
                    activeColor: DuoraColors.primaryPink,
                    secondary: const Icon(Icons.blur_on_rounded,
                        color: DuoraColors.primaryPink),
                    title: const Text('Blur Notification Preview',
                        style: TextStyle(color: Colors.white, fontSize: 14)),
                    value: _blurNotifications,
                    onChanged: (val) => setState(() => _blurNotifications = val),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 24),

            // Connection & Network Section
            const Text(
              'Signaling & Backend',
              style: TextStyle(
                color: DuoraColors.textPrimary,
                fontSize: 15,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 10),
            GestureDetector(
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (_) => const ServerHealthScreen()),
                );
              },
              child: Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: DuoraColors.surfaceCard,
                  borderRadius: BorderRadius.circular(18),
                  border: Border.all(color: DuoraColors.borderMuted),
                ),
                child: Row(
                  children: [
                    Container(
                      width: 10,
                      height: 10,
                      decoration: const BoxDecoration(
                        shape: BoxShape.circle,
                        color: DuoraColors.onlineGreen,
                      ),
                    ),
                    const SizedBox(width: 12),
                    const Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Amorcall Cloud Server',
                            style: TextStyle(
                              color: Colors.white,
                              fontSize: 14,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                          Text(
                            'wss://amorcall.onrender.com (Tap for speed test)',
                            style: TextStyle(
                              color: DuoraColors.textMuted,
                              fontSize: 11,
                            ),
                          ),
                        ],
                      ),
                    ),
                    const Icon(Icons.chevron_right_rounded,
                        color: DuoraColors.textMuted, size: 20),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 28),

            // Sign Out Button
            ElevatedButton(
              style: ElevatedButton.styleFrom(
                backgroundColor: DuoraColors.surfaceCard,
                foregroundColor: DuoraColors.error,
                side: const BorderSide(color: DuoraColors.error),
                elevation: 0,
              ),
              onPressed: () async {
                await authService.signOut();
                if (!mounted) return;
                Navigator.of(context).pushAndRemoveUntil(
                  MaterialPageRoute(builder: (_) => const AuthScreen()),
                  (route) => false,
                );
              },
              child: const Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.logout_rounded, size: 18),
                  SizedBox(width: 8),
                  Text('Sign Out of Duora'),
                ],
              ),
            ),
            const SizedBox(height: 30),
          ],
        ),
      ),
    );
  }

  void _showDisguisePicker() {
    final disguises = [
      'Default (Duora)',
      'Calculator 🔢',
      'Notes 📝',
      'Clock ⏰',
      'Weather ☀️',
    ];

    showModalBottomSheet(
      context: context,
      backgroundColor: DuoraColors.surfaceCard,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (ctx) => Padding(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Select Launcher Disguise',
              style: TextStyle(
                color: Colors.white,
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 14),
            ...disguises.map(
              (name) => ListTile(
                title: Text(name, style: const TextStyle(color: Colors.white)),
                trailing: _selectedDisguise == name
                    ? const Icon(Icons.check_rounded, color: DuoraColors.primaryPink)
                    : null,
                onTap: () {
                  setState(() => _selectedDisguise = name);
                  Navigator.pop(ctx);
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
