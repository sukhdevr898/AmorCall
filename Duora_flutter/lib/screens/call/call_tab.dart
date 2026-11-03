import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import '../../core/theme/duora_theme.dart';
import '../../services/auth_service.dart';
import '../../services/signaling_service.dart';
import 'active_call_screen.dart';

enum CallType { voice, video }
enum CallDirection { incoming, outgoing, missed }

class CallRecord {
  final String id;
  final String partnerName;
  final CallType type;
  final CallDirection direction;
  final DateTime timestamp;
  final String duration;

  CallRecord({
    required this.id,
    required this.partnerName,
    required this.type,
    required this.direction,
    required this.timestamp,
    required this.duration,
  });
}

class CallTab extends StatefulWidget {
  const CallTab({super.key});

  @override
  State<CallTab> createState() => _CallTabState();
}

class _CallTabState extends State<CallTab> {
  final List<CallRecord> _callLogs = [
    CallRecord(
      id: '1',
      partnerName: 'My Partner',
      type: CallType.video,
      direction: CallDirection.incoming,
      timestamp: DateTime.now().subtract(const Duration(hours: 3)),
      duration: '42m 18s',
    ),
    CallRecord(
      id: '2',
      partnerName: 'My Partner',
      type: CallType.voice,
      direction: CallDirection.outgoing,
      timestamp: DateTime.now().subtract(const Duration(hours: 8)),
      duration: '18m 04s',
    ),
    CallRecord(
      id: '3',
      partnerName: 'My Partner',
      type: CallType.voice,
      direction: CallDirection.missed,
      timestamp: DateTime.now().subtract(const Duration(days: 1)),
      duration: 'Missed',
    ),
  ];

  String _filter = 'All';

  @override
  Widget build(BuildContext context) {
    final dateFormat = DateFormat('MMM dd, hh:mm a');

    final filteredLogs = _callLogs.where((log) {
      if (_filter == 'Missed') return log.direction == CallDirection.missed;
      if (_filter == 'Video') return log.type == CallType.video;
      return true;
    }).toList();

    return Scaffold(
      backgroundColor: DuoraColors.background,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: const Text(
          'Hotline & Calls',
          style: TextStyle(
            color: DuoraColors.textPrimary,
            fontWeight: FontWeight.bold,
            fontSize: 20,
          ),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.delete_sweep_outlined, color: DuoraColors.textSecondary),
            onPressed: () {
              setState(() => _callLogs.clear());
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                  backgroundColor: DuoraColors.surfaceCard,
                  content: Text('Call logs cleared'),
                ),
              );
            },
          ),
        ],
      ),
      body: SafeArea(
        child: Column(
          children: [
            // Quick Call Actions Banner
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
              child: Row(
                children: [
                  Expanded(
                    child: ElevatedButton.icon(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: const Color(0xFF10B981),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(16),
                        ),
                      ),
                      icon: const Icon(Icons.phone_rounded, color: Colors.white),
                      label: const Text('Voice Call'),
                      onPressed: () => _startCall(context, isVideo: false),
                    ),
                  ),
                  const SizedBox(width: 14),
                  Expanded(
                    child: ElevatedButton.icon(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: DuoraColors.primaryPink,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(16),
                        ),
                      ),
                      icon: const Icon(Icons.videocam_rounded, color: Colors.white),
                      label: const Text('Video Call'),
                      onPressed: () => _startCall(context, isVideo: true),
                    ),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 12),

            // Filter Chips
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20),
              child: Row(
                children: ['All', 'Missed', 'Video'].map((filter) {
                  final isSelected = _filter == filter;
                  return Padding(
                    padding: const EdgeInsets.only(right: 8),
                    child: ChoiceChip(
                      label: Text(filter),
                      selected: isSelected,
                      selectedColor: DuoraColors.primaryPink,
                      backgroundColor: DuoraColors.surfaceCard,
                      labelStyle: TextStyle(
                        color: isSelected ? Colors.white : DuoraColors.textSecondary,
                        fontWeight: FontWeight.w600,
                        fontSize: 12,
                      ),
                      side: const BorderSide(color: DuoraColors.borderMuted),
                      onSelected: (_) => setState(() => _filter = filter),
                    ),
                  );
                }).toList(),
              ),
            ),

            const SizedBox(height: 12),

            // Call Logs List
            Expanded(
              child: filteredLogs.isEmpty
                  ? const Center(
                      child: Text(
                        'No call history found',
                        style: TextStyle(color: DuoraColors.textMuted),
                      ),
                    )
                  : ListView.builder(
                      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
                      itemCount: filteredLogs.length,
                      itemBuilder: (context, index) {
                        final log = filteredLogs[index];
                        return Container(
                          margin: const EdgeInsets.symmetric(vertical: 6),
                          padding: const EdgeInsets.all(14),
                          decoration: BoxDecoration(
                            color: DuoraColors.surfaceCard,
                            borderRadius: BorderRadius.circular(16),
                            border: Border.all(color: DuoraColors.borderMuted),
                          ),
                          child: Row(
                            children: [
                              Container(
                                padding: const EdgeInsets.all(10),
                                decoration: BoxDecoration(
                                  color: log.direction == CallDirection.missed
                                      ? DuoraColors.error.withOpacity(0.15)
                                      : DuoraColors.primaryPink.withOpacity(0.15),
                                  borderRadius: BorderRadius.circular(12),
                                ),
                                child: Icon(
                                  log.type == CallType.video
                                      ? Icons.videocam_rounded
                                      : Icons.call_rounded,
                                  color: log.direction == CallDirection.missed
                                      ? DuoraColors.error
                                      : DuoraColors.primaryPink,
                                  size: 20,
                                ),
                              ),
                              const SizedBox(width: 14),
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(
                                      log.partnerName,
                                      style: const TextStyle(
                                        color: DuoraColors.textPrimary,
                                        fontWeight: FontWeight.bold,
                                        fontSize: 14,
                                      ),
                                    ),
                                    const SizedBox(height: 2),
                                    Row(
                                      children: [
                                        Icon(
                                          log.direction == CallDirection.incoming
                                              ? Icons.call_received_rounded
                                              : log.direction == CallDirection.outgoing
                                                  ? Icons.call_made_rounded
                                                  : Icons.call_missed_rounded,
                                          size: 13,
                                          color: log.direction == CallDirection.missed
                                              ? DuoraColors.error
                                              : DuoraColors.onlineGreen,
                                        ),
                                        const SizedBox(width: 4),
                                        Text(
                                          dateFormat.format(log.timestamp),
                                          style: const TextStyle(
                                            color: DuoraColors.textMuted,
                                            fontSize: 11,
                                          ),
                                        ),
                                      ],
                                    ),
                                  ],
                                ),
                              ),
                              Text(
                                log.duration,
                                style: TextStyle(
                                  color: log.direction == CallDirection.missed
                                      ? DuoraColors.error
                                      : DuoraColors.textSecondary,
                                  fontSize: 12,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                            ],
                          ),
                        );
                      },
                    ),
            ),
          ],
        ),
      ),
    );
  }

  void _startCall(BuildContext context, {required bool isVideo}) {
    final authService = Provider.of<AuthService>(context, listen: false);
    final sig = Provider.of<SignalingService>(context, listen: false);
    final user = authService.currentUser;

    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => ActiveCallScreen(
          partnerName: user?.partnerName ?? 'My Partner',
          partnerCode: user?.partnerPairCode ?? 'PARTNER',
          isVideo: isVideo,
          signalingService: sig,
        ),
      ),
    );
  }
}
