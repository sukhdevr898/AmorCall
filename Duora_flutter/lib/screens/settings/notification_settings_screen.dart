import 'package:flutter/material.dart';
import '../../core/theme/duora_theme.dart';

class NotificationSettingsScreen extends StatefulWidget {
  const NotificationSettingsScreen({super.key});

  @override
  State<NotificationSettingsScreen> createState() => _NotificationSettingsScreenState();
}

class _NotificationSettingsScreenState extends State<NotificationSettingsScreen> {
  bool _messageAlerts = true;
  bool _callAlerts = true;
  bool _inAppBanner = true;
  String _vibrationPattern = 'Heartbeat (Romantic)';
  String _ringtone = 'Romantic Bells';

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: DuoraColors.background,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: const Text(
          'Notification Settings',
          style: TextStyle(
            color: DuoraColors.textPrimary,
            fontWeight: FontWeight.bold,
            fontSize: 20,
          ),
        ),
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
          children: [
            Container(
              decoration: BoxDecoration(
                color: DuoraColors.surfaceCard,
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: DuoraColors.borderMuted),
              ),
              child: Column(
                children: [
                  SwitchListTile(
                    activeColor: DuoraColors.primaryPink,
                    title: const Text('Message Notifications',
                        style: TextStyle(color: Colors.white, fontSize: 15)),
                    subtitle: const Text('Receive alerts when partner sends sweet texts',
                        style: TextStyle(color: DuoraColors.textMuted, fontSize: 12)),
                    value: _messageAlerts,
                    onChanged: (v) => setState(() => _messageAlerts = v),
                  ),
                  const Divider(color: DuoraColors.divider, height: 1),
                  SwitchListTile(
                    activeColor: DuoraColors.primaryPink,
                    title: const Text('Call & Hotline Alerts',
                        style: TextStyle(color: Colors.white, fontSize: 15)),
                    subtitle: const Text('Heads-up banner for incoming voice/video calls',
                        style: TextStyle(color: DuoraColors.textMuted, fontSize: 12)),
                    value: _callAlerts,
                    onChanged: (v) => setState(() => _callAlerts = v),
                  ),
                  const Divider(color: DuoraColors.divider, height: 1),
                  SwitchListTile(
                    activeColor: DuoraColors.primaryPink,
                    title: const Text('In-App Notification Banners',
                        style: TextStyle(color: Colors.white, fontSize: 15)),
                    subtitle: const Text('Show floating heart reactions while inside app',
                        style: TextStyle(color: DuoraColors.textMuted, fontSize: 12)),
                    value: _inAppBanner,
                    onChanged: (v) => setState(() => _inAppBanner = v),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),
            const Text(
              'Ringtone & Vibration',
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
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: DuoraColors.borderMuted),
              ),
              child: Column(
                children: [
                  ListTile(
                    leading: const Icon(Icons.music_note_rounded, color: DuoraColors.primaryPink),
                    title: const Text('Hotline Ringtone',
                        style: TextStyle(color: Colors.white, fontSize: 14)),
                    subtitle: Text(_ringtone,
                        style: const TextStyle(color: DuoraColors.textSecondary, fontSize: 12)),
                    trailing: const Icon(Icons.chevron_right_rounded, color: DuoraColors.textMuted),
                    onTap: () {
                      setState(() {
                        _ringtone = _ringtone == 'Romantic Bells' ? 'Harp Melody' : 'Romantic Bells';
                      });
                    },
                  ),
                  const Divider(color: DuoraColors.divider, height: 1),
                  ListTile(
                    leading: const Icon(Icons.vibration_rounded, color: DuoraColors.primaryPink),
                    title: const Text('Vibration Pattern',
                        style: TextStyle(color: Colors.white, fontSize: 14)),
                    subtitle: Text(_vibrationPattern,
                        style: const TextStyle(color: DuoraColors.textSecondary, fontSize: 12)),
                    trailing: const Icon(Icons.chevron_right_rounded, color: DuoraColors.textMuted),
                    onTap: () {
                      setState(() {
                        _vibrationPattern = _vibrationPattern == 'Heartbeat (Romantic)'
                            ? 'Gentle Pulse'
                            : 'Heartbeat (Romantic)';
                      });
                    },
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
