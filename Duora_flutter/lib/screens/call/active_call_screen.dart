import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_webrtc/flutter_webrtc.dart';
import '../../core/theme/duora_theme.dart';
import '../../services/signaling_service.dart';
import '../../services/webrtc_call_service.dart';

class ActiveCallScreen extends StatefulWidget {
  final String partnerName;
  final String partnerCode;
  final bool isVideo;
  final bool isIncoming;
  final SignalingService signalingService;

  const ActiveCallScreen({
    super.key,
    required this.partnerName,
    required this.partnerCode,
    required this.isVideo,
    this.isIncoming = false,
    required this.signalingService,
  });

  @override
  State<ActiveCallScreen> createState() => _ActiveCallScreenState();
}

class _ActiveCallScreenState extends State<ActiveCallScreen> {
  late WebRtcCallService _callService;
  Timer? _durationTimer;
  int _secondsElapsed = 0;

  @override
  void initState() {
    super.initState();
    _callService = WebRtcCallService(signalingService: widget.signalingService);
    _initCall();
  }

  Future<void> _initCall() async {
    await _callService.initializeRenderers();
    await _callService.startLocalMedia(isVideo: widget.isVideo);

    if (!widget.isIncoming) {
      widget.signalingService.startCall(
        targetCode: widget.partnerCode,
        isVideo: widget.isVideo,
      );
      await _callService.createPeerConnectionAndOffer(targetCode: widget.partnerCode);
    }

    _startTimer();
  }

  void _startTimer() {
    _durationTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (mounted) {
        setState(() => _secondsElapsed++);
      }
    });
  }

  String _formatDuration(int seconds) {
    final m = (seconds ~/ 60).toString().padLeft(2, '0');
    final s = (seconds % 60).toString().padLeft(2, '0');
    return '$m:$s';
  }

  void _endCall() {
    _durationTimer?.cancel();
    widget.signalingService.endCall(targetCode: widget.partnerCode);
    _callService.cleanUp();
    if (mounted) Navigator.pop(context);
  }

  @override
  void dispose() {
    _durationTimer?.cancel();
    _callService.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      body: Stack(
        children: [
          // Background: Video or Voice UI
          if (widget.isVideo) ...[
            // Remote Video
            Positioned.fill(
              child: _callService.remoteRenderer.srcObject != null
                  ? RTCVideoView(
                      _callService.remoteRenderer,
                      objectFit: RTCVideoViewObjectFit.RTCVideoViewObjectFitCover,
                    )
                  : Container(
                      color: DuoraColors.background,
                      child: const Center(
                        child: Text(
                          'Waiting for video stream...',
                          style: TextStyle(color: DuoraColors.textSecondary),
                        ),
                      ),
                    ),
            ),
            // Floating Local Video PiP
            Positioned(
              top: 50,
              right: 20,
              child: Container(
                width: 110,
                height: 160,
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: DuoraColors.primaryPink, width: 2),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.5),
                      blurRadius: 10,
                    ),
                  ],
                ),
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(14),
                  child: RTCVideoView(
                    _callService.localRenderer,
                    mirror: _callService.isFrontCamera,
                    objectFit: RTCVideoViewObjectFit.RTCVideoViewObjectFitCover,
                  ),
                ),
              ),
            ),
          ] else ...[
            // Voice Call Visualizer UI
            Container(
              decoration: const BoxDecoration(
                gradient: DuoraColors.luxuryBackgroundGradient,
              ),
              child: Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Container(
                      width: 140,
                      height: 140,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        gradient: DuoraColors.primaryGradient,
                        boxShadow: [
                          BoxShadow(
                            color: DuoraColors.primaryPink.withOpacity(0.4),
                            blurRadius: 36,
                            spreadRadius: 6,
                          ),
                        ],
                      ),
                      child: const Center(
                        child: Icon(Icons.favorite, color: Colors.white, size: 70),
                      ),
                    ),
                    const SizedBox(height: 28),
                    Text(
                      widget.partnerName,
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 26,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      widget.signalingService.callStatus == CallStatus.outgoingRinging
                          ? 'Ringing...'
                          : 'Voice Hotline • Encrypted',
                      style: const TextStyle(
                        color: DuoraColors.textSecondary,
                        fontSize: 14,
                      ),
                    ),
                    const SizedBox(height: 16),
                    Text(
                      _formatDuration(_secondsElapsed),
                      style: const TextStyle(
                        color: DuoraColors.primaryPink,
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                        letterSpacing: 2,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],

          // Top Header Bar
          Positioned(
            top: 50,
            left: 20,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
              decoration: BoxDecoration(
                color: Colors.black.withOpacity(0.5),
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: Colors.white.withOpacity(0.15)),
              ),
              child: Row(
                children: [
                  const Icon(Icons.lock_rounded, color: DuoraColors.onlineGreen, size: 14),
                  const SizedBox(width: 8),
                  Text(
                    widget.partnerName,
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 13,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  if (widget.isVideo) ...[
                    const SizedBox(width: 8),
                    Text(
                      _formatDuration(_secondsElapsed),
                      style: const TextStyle(color: Colors.white70, fontSize: 12),
                    ),
                  ],
                ],
              ),
            ),
          ),

          // Bottom Call Controls
          Positioned(
            bottom: 40,
            left: 20,
            right: 20,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
              decoration: BoxDecoration(
                color: DuoraColors.surface.withOpacity(0.85),
                borderRadius: BorderRadius.circular(32),
                border: Border.all(color: DuoraColors.borderMuted),
              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: [
                  // Mute
                  _CallControlBtn(
                    icon: _callService.isMuted
                        ? Icons.mic_off_rounded
                        : Icons.mic_rounded,
                    isActive: _callService.isMuted,
                    onTap: () => setState(() => _callService.toggleMute()),
                  ),

                  // Speaker
                  _CallControlBtn(
                    icon: _callService.isSpeakerOn
                        ? Icons.volume_up_rounded
                        : Icons.volume_down_rounded,
                    isActive: _callService.isSpeakerOn,
                    onTap: () => setState(() => _callService.toggleSpeaker()),
                  ),

                  if (widget.isVideo) ...[
                    // Toggle Video
                    _CallControlBtn(
                      icon: _callService.isVideoEnabled
                          ? Icons.videocam_rounded
                          : Icons.videocam_off_rounded,
                      isActive: !_callService.isVideoEnabled,
                      onTap: () => setState(() => _callService.toggleVideo()),
                    ),
                    // Flip Camera
                    _CallControlBtn(
                      icon: Icons.flip_camera_ios_rounded,
                      isActive: false,
                      onTap: () => _callService.switchCamera(),
                    ),
                  ],

                  // End Call (Red Button)
                  GestureDetector(
                    onTap: _endCall,
                    child: Container(
                      width: 56,
                      height: 56,
                      decoration: const BoxDecoration(
                        color: DuoraColors.error,
                        shape: BoxShape.circle,
                        boxShadow: [
                          BoxShadow(
                            color: DuoraColors.error,
                            blurRadius: 14,
                            spreadRadius: 1,
                          ),
                        ],
                      ),
                      child: const Center(
                        child: Icon(Icons.call_end_rounded, color: Colors.white, size: 26),
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

class _CallControlBtn extends StatelessWidget {
  final IconData icon;
  final bool isActive;
  final VoidCallback onTap;

  const _CallControlBtn({
    required this.icon,
    required this.isActive,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 46,
        height: 46,
        decoration: BoxDecoration(
          color: isActive ? DuoraColors.primaryPink : Colors.white.withOpacity(0.12),
          shape: BoxShape.circle,
        ),
        child: Center(
          child: Icon(
            icon,
            color: Colors.white,
            size: 22,
          ),
        ),
      ),
    );
  }
}
