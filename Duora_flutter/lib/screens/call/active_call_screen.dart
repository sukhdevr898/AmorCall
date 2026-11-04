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
    this.partnerName = 'Unisha ♡',
    this.partnerCode = 'LOVE01',
    required this.isVideo,
    this.isIncoming = false,
    required this.signalingService,
  });

  @override
  State<ActiveCallScreen> createState() => _ActiveCallScreenState();
}

class _ActiveCallScreenState extends State<ActiveCallScreen>
    with SingleTickerProviderStateMixin {
  late WebRtcCallService _callService;
  Timer? _durationTimer;
  int _secondsElapsed = 0;

  bool _isBluetoothOn = false;
  bool _isKeypadOpen = false;
  bool _isRecording = false;
  bool _isPiPMode = false;

  late AnimationController _pulseController;
  late Animation<double> _pulseAnimation;

  @override
  void initState() {
    super.initState();
    _callService = WebRtcCallService(signalingService: widget.signalingService);

    _pulseController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 2),
    )..repeat(reverse: true);

    _pulseAnimation = Tween<double>(begin: 1.0, end: 1.15).animate(
      CurvedAnimation(parent: _pulseController, curve: Curves.easeInOut),
    );

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
    _pulseController.dispose();
    _callService.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (_isPiPMode) {
      return _buildPiPOverlay();
    }

    return Scaffold(
      backgroundColor: const Color(0xFF1E103D),
      body: widget.isVideo ? _buildVideoCallUI() : _buildAudioCallUI(),
    );
  }

  // --- AUDIO CALL UI (Matches Screen 4) ---
  Widget _buildAudioCallUI() {
    return Stack(
      children: [
        // 1. Deep purple romantic gradient background
        Positioned.fill(
          child: Container(
            decoration: const BoxDecoration(
              gradient: RadialGradient(
                center: Alignment(0, -0.2),
                radius: 1.2,
                colors: [
                  Color(0xFF452285),
                  Color(0xFF261353),
                  Color(0xFF150A30),
                ],
              ),
            ),
          ),
        ),

        // 2. Main Content
        SafeArea(
          child: Column(
            children: [
              // Top Bar with PiP / Fullscreen Toggle
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    IconButton(
                      icon: const Icon(Icons.keyboard_arrow_down_rounded,
                          color: Colors.white, size: 30),
                      onPressed: () => setState(() => _isPiPMode = true),
                    ),
                    IconButton(
                      icon: const Icon(Icons.fullscreen_rounded,
                          color: Colors.white, size: 26),
                      onPressed: () => setState(() => _isPiPMode = true),
                    ),
                  ],
                ),
              ),

              const Spacer(flex: 2),

              // Glowing Animated Concentric Avatar Rings
              AnimatedBuilder(
                animation: _pulseAnimation,
                builder: (context, child) {
                  return Container(
                    width: 170 * _pulseAnimation.value,
                    height: 170 * _pulseAnimation.value,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      border: Border.all(
                        color: const Color(0xFFB057F7).withOpacity(0.3),
                        width: 2,
                      ),
                      boxShadow: [
                        BoxShadow(
                          color: const Color(0xFF5B3DF5).withOpacity(0.25),
                          blurRadius: 36,
                          spreadRadius: 8,
                        ),
                      ],
                    ),
                    child: Center(
                      child: Container(
                        width: 140,
                        height: 140,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          border: Border.all(color: Colors.white, width: 3),
                        ),
                        child: ClipOval(
                          child: Image.asset(
                            'assets/images/avatar_partner_demo.png',
                            fit: BoxFit.cover,
                            errorBuilder: (_, __, ___) => Container(
                              color: const Color(0xFFFF2D8D),
                              child: const Icon(Icons.person, color: Colors.white, size: 70),
                            ),
                          ),
                        ),
                      ),
                    ),
                  );
                },
              ),

              const SizedBox(height: 24),

              // Partner Name
              Text(
                widget.partnerName,
                style: const TextStyle(
                  fontSize: 26,
                  fontWeight: FontWeight.w900,
                  color: Colors.white,
                  letterSpacing: 0.5,
                ),
              ),

              const SizedBox(height: 8),

              // Signal Icon + Duration Timer
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.signal_cellular_alt_rounded,
                      color: Color(0xFF10B981), size: 16),
                  const SizedBox(width: 6),
                  Text(
                    _formatDuration(_secondsElapsed),
                    style: const TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w600,
                      color: Color(0xFFE8DEFF),
                    ),
                  ),
                ],
              ),

              const Spacer(flex: 3),

              // 6-Button Call Matrix
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 36),
                child: Column(
                  children: [
                    // Row 1: Mute, Speaker, Bluetooth
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceAround,
                      children: [
                        _CallMatrixBtn(
                          icon: _callService.isMuted
                              ? Icons.mic_off_rounded
                              : Icons.mic_rounded,
                          label: 'Mute',
                          isActive: _callService.isMuted,
                          onTap: () => setState(() => _callService.toggleMute()),
                        ),
                        _CallMatrixBtn(
                          icon: Icons.volume_up_rounded,
                          label: 'Speaker',
                          isActive: _callService.isSpeakerOn,
                          onTap: () => setState(() => _callService.toggleSpeaker()),
                        ),
                        _CallMatrixBtn(
                          icon: Icons.bluetooth_rounded,
                          label: 'Bluetooth',
                          isActive: _isBluetoothOn,
                          onTap: () => setState(() => _isBluetoothOn = !_isBluetoothOn),
                        ),
                      ],
                    ),

                    const SizedBox(height: 20),

                    // Row 2: Add Call, Keypad, Record
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceAround,
                      children: [
                        _CallMatrixBtn(
                          icon: Icons.add_rounded,
                          label: 'Add Call',
                          onTap: () {
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(content: Text('Couple private line only')),
                            );
                          },
                        ),
                        _CallMatrixBtn(
                          icon: Icons.dialpad_rounded,
                          label: 'Keypad',
                          isActive: _isKeypadOpen,
                          onTap: () => setState(() => _isKeypadOpen = !_isKeypadOpen),
                        ),
                        _CallMatrixBtn(
                          icon: Icons.fiber_manual_record_rounded,
                          label: 'Record',
                          isActive: _isRecording,
                          onTap: () {
                            setState(() => _isRecording = !_isRecording);
                            ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(
                                content: Text(_isRecording
                                    ? 'Call recording started'
                                    : 'Call recording saved to vault'),
                              ),
                            );
                          },
                        ),
                      ],
                    ),
                  ],
                ),
              ),

              const Spacer(flex: 2),

              // End Call Red Button
              GestureDetector(
                onTap: _endCall,
                child: Container(
                  width: 68,
                  height: 68,
                  decoration: const BoxDecoration(
                    color: Color(0xFFFF334B),
                    shape: BoxShape.circle,
                    boxShadow: [
                      BoxShadow(
                        color: Color(0x66FF334B),
                        blurRadius: 20,
                        spreadRadius: 4,
                        offset: Offset(0, 4),
                      ),
                    ],
                  ),
                  child: const Center(
                    child: Icon(Icons.call_end_rounded, color: Colors.white, size: 34),
                  ),
                ),
              ),

              const SizedBox(height: 40),
            ],
          ),
        ),
      ],
    );
  }

  // --- VIDEO CALL UI (Matches Screen 5) ---
  Widget _buildVideoCallUI() {
    return Stack(
      children: [
        // 1. Fullscreen Remote Video
        Positioned.fill(
          child: _callService.remoteRenderer.srcObject != null
              ? RTCVideoView(
                  _callService.remoteRenderer,
                  objectFit: RTCVideoViewObjectFit.RTCVideoViewObjectFitCover,
                )
              : Image.asset(
                  'assets/images/avatar_partner_demo.png',
                  fit: BoxFit.cover,
                  errorBuilder: (_, __, ___) => Container(
                    color: const Color(0xFF1E103D),
                    child: const Center(
                      child: Text(
                        'Connecting HD video stream...',
                        style: TextStyle(color: Color(0xFFE8DEFF)),
                      ),
                    ),
                  ),
                ),
        ),

        // Gradient overlay for top and bottom controls
        Positioned.fill(
          child: Container(
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: [
                  Colors.black.withOpacity(0.55),
                  Colors.transparent,
                  Colors.black.withOpacity(0.65),
                ],
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
              ),
            ),
          ),
        ),

        // 2. Top Bar: Call Timer & Local PiP Camera
        SafeArea(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Duration Pill
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
                  decoration: BoxDecoration(
                    color: Colors.black.withOpacity(0.4),
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Container(
                        width: 8,
                        height: 8,
                        decoration: const BoxDecoration(
                          color: Color(0xFFFF2D8D),
                          shape: BoxShape.circle,
                        ),
                      ),
                      const SizedBox(width: 8),
                      Text(
                        _formatDuration(_secondsElapsed),
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 13,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ],
                  ),
                ),

                // Floating Local Camera Preview (PiP Window)
                Container(
                  width: 100,
                  height: 145,
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(18),
                    border: Border.all(color: Colors.white, width: 2),
                    boxShadow: [
                      BoxShadow(
                        color: Colors.black.withOpacity(0.4),
                        blurRadius: 12,
                      ),
                    ],
                  ),
                  child: ClipRRect(
                    borderRadius: BorderRadius.circular(16),
                    child: _callService.localRenderer.srcObject != null
                        ? RTCVideoView(
                            _callService.localRenderer,
                            mirror: _callService.isFrontCamera,
                            objectFit: RTCVideoViewObjectFit.RTCVideoViewObjectFitCover,
                          )
                        : Container(
                            color: const Color(0xFF5B3DF5),
                            child: const Center(
                              child: Icon(Icons.person, color: Colors.white, size: 40),
                            ),
                          ),
                  ),
                ),
              ],
            ),
          ),
        ),

        // 3. Bottom Video Controls Bar
        Positioned(
          bottom: 36,
          left: 20,
          right: 20,
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
            decoration: BoxDecoration(
              color: const Color(0xFF211452).withOpacity(0.88),
              borderRadius: BorderRadius.circular(36),
              border: Border.all(color: Colors.white.withOpacity(0.18)),
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                // Switch Camera
                _VideoControlBtn(
                  icon: Icons.flip_camera_ios_rounded,
                  onTap: () => _callService.switchCamera(),
                ),
                // Mute
                _VideoControlBtn(
                  icon: _callService.isMuted
                      ? Icons.mic_off_rounded
                      : Icons.mic_rounded,
                  isActive: _callService.isMuted,
                  onTap: () => setState(() => _callService.toggleMute()),
                ),
                // Video On/Off
                _VideoControlBtn(
                  icon: _callService.isVideoEnabled
                      ? Icons.videocam_rounded
                      : Icons.videocam_off_rounded,
                  isActive: !_callService.isVideoEnabled,
                  onTap: () => setState(() => _callService.toggleVideo()),
                ),
                // End Call Red Button
                GestureDetector(
                  onTap: _endCall,
                  child: Container(
                    width: 52,
                    height: 52,
                    decoration: const BoxDecoration(
                      color: Color(0xFFFF334B),
                      shape: BoxShape.circle,
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
    );
  }

  // --- PiP MINIMIZED OVERLAY (Picture-in-Picture) ---
  Widget _buildPiPOverlay() {
    return Scaffold(
      backgroundColor: Colors.black45,
      body: Center(
        child: Container(
          width: 140,
          height: 190,
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(22),
            border: Border.all(color: const Color(0xFFB057F7), width: 2),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(0.6),
                blurRadius: 24,
              ),
            ],
          ),
          child: ClipRRect(
            borderRadius: BorderRadius.circular(20),
            child: Stack(
              children: [
                Positioned.fill(
                  child: Image.asset(
                    'assets/images/avatar_partner_demo.png',
                    fit: BoxFit.cover,
                  ),
                ),
                Positioned(
                  top: 8,
                  right: 8,
                  child: GestureDetector(
                    onTap: () => setState(() => _isPiPMode = false),
                    child: Container(
                      padding: const EdgeInsets.all(4),
                      decoration: const BoxDecoration(
                        color: Colors.black54,
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(Icons.fullscreen_rounded, color: Colors.white, size: 18),
                    ),
                  ),
                ),
                Positioned(
                  bottom: 8,
                  left: 0,
                  right: 0,
                  child: Center(
                    child: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                      decoration: BoxDecoration(
                        color: Colors.black54,
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: Text(
                        _formatDuration(_secondsElapsed),
                        style: const TextStyle(color: Colors.white, fontSize: 10),
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _CallMatrixBtn extends StatelessWidget {
  final IconData icon;
  final String label;
  final bool isActive;
  final VoidCallback onTap;

  const _CallMatrixBtn({
    required this.icon,
    required this.label,
    this.isActive = false,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Column(
        children: [
          Container(
            width: 58,
            height: 58,
            decoration: BoxDecoration(
              color: isActive
                  ? const Color(0xFF5B3DF5)
                  : Colors.white.withOpacity(0.12),
              shape: BoxShape.circle,
              border: Border.all(
                color: isActive ? Colors.white : Colors.white.withOpacity(0.18),
                width: 1.5,
              ),
            ),
            child: Center(
              child: Icon(icon, color: Colors.white, size: 24),
            ),
          ),
          const SizedBox(height: 6),
          Text(
            label,
            style: const TextStyle(
              fontSize: 11.5,
              fontWeight: FontWeight.w500,
              color: Color(0xFFE8DEFF),
            ),
          ),
        ],
      ),
    );
  }
}

class _VideoControlBtn extends StatelessWidget {
  final IconData icon;
  final bool isActive;
  final VoidCallback onTap;

  const _VideoControlBtn({
    required this.icon,
    this.isActive = false,
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
          color: isActive
              ? const Color(0xFF5B3DF5)
              : Colors.white.withOpacity(0.15),
          shape: BoxShape.circle,
        ),
        child: Center(
          child: Icon(icon, color: Colors.white, size: 22),
        ),
      ),
    );
  }
}
