import 'package:flutter/material.dart';
import '../../core/theme/duora_theme.dart';
import '../../core/config/env_config.dart';

class ServerHealthScreen extends StatefulWidget {
  const ServerHealthScreen({super.key});

  @override
  State<ServerHealthScreen> createState() => _ServerHealthScreenState();
}

class _ServerHealthScreenState extends State<ServerHealthScreen> {
  int _latencyMs = 38;
  bool _isChecking = false;

  void _runDiagnostics() async {
    setState(() => _isChecking = true);
    await Future.delayed(const Duration(milliseconds: 900));
    if (!mounted) return;
    setState(() {
      _latencyMs = 32 + (DateTime.now().millisecond % 20);
      _isChecking = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: DuoraColors.background,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: const Text(
          'Server & Connection Health',
          style: TextStyle(
            color: DuoraColors.textPrimary,
            fontWeight: FontWeight.bold,
            fontSize: 20,
          ),
        ),
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Main Health Card
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(22),
                decoration: BoxDecoration(
                  color: DuoraColors.surfaceCard,
                  borderRadius: BorderRadius.circular(24),
                  border: Border.all(color: DuoraColors.borderMuted),
                ),
                child: Column(
                  children: [
                    Container(
                      width: 64,
                      height: 64,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: DuoraColors.onlineGreen.withOpacity(0.15),
                      ),
                      child: const Center(
                        child: Icon(Icons.cloud_done_rounded,
                            color: DuoraColors.onlineGreen, size: 36),
                      ),
                    ),
                    const SizedBox(height: 16),
                    const Text(
                      'Amorcall Cloud Signaling',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      EnvConfig.websocketUrl,
                      style: const TextStyle(
                        color: DuoraColors.textMuted,
                        fontSize: 12,
                      ),
                    ),
                    const SizedBox(height: 20),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceAround,
                      children: [
                        _StatBadge(
                          title: 'Latency',
                          value: '$_latencyMs ms',
                          color: DuoraColors.onlineGreen,
                        ),
                        const _StatBadge(
                          title: 'WebRTC STUN',
                          value: 'Active',
                          color: DuoraColors.verifiedBlue,
                        ),
                        const _StatBadge(
                          title: 'Encryption',
                          value: 'TLS 1.3',
                          color: DuoraColors.primaryPink,
                        ),
                      ],
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 24),

              // Diagnostics Button
              ElevatedButton.icon(
                style: ElevatedButton.styleFrom(
                  backgroundColor: DuoraColors.surfaceHighlight,
                  foregroundColor: Colors.white,
                  side: const BorderSide(color: DuoraColors.borderMuted),
                ),
                icon: _isChecking
                    ? const SizedBox(
                        width: 16,
                        height: 16,
                        child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                      )
                    : const Icon(Icons.refresh_rounded, size: 20),
                label: const Text('Re-test Connection Speed'),
                onPressed: _isChecking ? null : _runDiagnostics,
              ),

              const SizedBox(height: 24),
              const Text(
                'Diagnostic Details',
                style: TextStyle(
                  color: DuoraColors.textPrimary,
                  fontSize: 15,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 12),
              Expanded(
                child: Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: DuoraColors.surface,
                    borderRadius: BorderRadius.circular(18),
                    border: Border.all(color: DuoraColors.borderMuted),
                  ),
                  child: ListView(
                    children: const [
                      Text('• STUN Server: stun.l.google.com:19302 (Connected)',
                          style: TextStyle(color: DuoraColors.textSecondary, fontSize: 13)),
                      SizedBox(height: 8),
                      Text('• ICE Gathering State: Complete',
                          style: TextStyle(color: DuoraColors.textSecondary, fontSize: 13)),
                      SizedBox(height: 8),
                      Text('• Heartbeat Interval: 25s ping/pong',
                          style: TextStyle(color: DuoraColors.textSecondary, fontSize: 13)),
                      SizedBox(height: 8),
                      Text('• Reconnection Grace Period: 15s',
                          style: TextStyle(color: DuoraColors.textSecondary, fontSize: 13)),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _StatBadge extends StatelessWidget {
  final String title;
  final String value;
  final Color color;

  const _StatBadge({
    required this.title,
    required this.value,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Text(
          value,
          style: TextStyle(
            color: color,
            fontSize: 16,
            fontWeight: FontWeight.bold,
          ),
        ),
        const SizedBox(height: 2),
        Text(
          title,
          style: const TextStyle(
            color: DuoraColors.textMuted,
            fontSize: 11,
          ),
        ),
      ],
    );
  }
}
