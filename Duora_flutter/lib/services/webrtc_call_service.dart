import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:flutter_webrtc/flutter_webrtc.dart';
import '../core/config/env_config.dart';
import 'signaling_service.dart';

class WebRtcCallService extends ChangeNotifier {
  final SignalingService signalingService;

  RTCPeerConnection? _peerConnection;
  MediaStream? _localStream;
  MediaStream? _remoteStream;

  final RTCVideoRenderer localRenderer = RTCVideoRenderer();
  final RTCVideoRenderer remoteRenderer = RTCVideoRenderer();

  bool _isMuted = false;
  bool _isSpeakerOn = true;
  bool _isVideoEnabled = true;
  bool _isFrontCamera = true;
  bool _isInitialized = false;

  bool get isMuted => _isMuted;
  bool get isSpeakerOn => _isSpeakerOn;
  bool get isVideoEnabled => _isVideoEnabled;
  bool get isFrontCamera => _isFrontCamera;

  StreamSubscription? _signalingSub;

  WebRtcCallService({required this.signalingService});

  Future<void> initializeRenderers() async {
    if (_isInitialized) return;
    await localRenderer.initialize();
    await remoteRenderer.initialize();
    _isInitialized = true;

    _signalingSub = signalingService.messageStream.listen(_onSignalingMessage);
  }

  Future<void> startLocalMedia({required bool isVideo}) async {
    _isVideoEnabled = isVideo;

    final mediaConstraints = <String, dynamic>{
      'audio': true,
      'video': isVideo
          ? {
              'mandatory': {
                'minWidth': '640',
                'minHeight': '480',
                'minFrameRate': '30',
              },
              'facingMode': 'user',
              'optional': [],
            }
          : false,
    };

    try {
      _localStream = await navigator.mediaDevices.getUserMedia(mediaConstraints);
      localRenderer.srcObject = _localStream;
      notifyListeners();
    } catch (e) {
      debugPrint('Error getting user media: $e');
    }
  }

  Future<void> createPeerConnectionAndOffer({required String targetCode}) async {
    await _setupPeerConnection(targetCode);

    final offer = await _peerConnection!.createOffer({
      'offerToReceiveAudio': 1,
      'offerToReceiveVideo': _isVideoEnabled ? 1 : 0,
    });

    await _peerConnection!.setLocalDescription(offer);

    signalingService.sendWebRtcSignal({
      'type': 'webrtc_offer',
      'to': targetCode,
      'sdp': offer.sdp,
    });
  }

  Future<void> _setupPeerConnection(String targetCode) async {
    final configuration = <String, dynamic>{
      'iceServers': [
        {'urls': EnvConfig.stunServer1},
        {'urls': EnvConfig.stunServer2},
      ],
      'sdpSemantics': 'unified-plan',
    };

    _peerConnection = await createPeerConnection(configuration);

    if (_localStream != null) {
      for (final track in _localStream!.getTracks()) {
        _peerConnection!.addTrack(track, _localStream!);
      }
    }

    _peerConnection!.onTrack = (RTCTrackEvent event) {
      if (event.streams.isNotEmpty) {
        _remoteStream = event.streams[0];
        remoteRenderer.srcObject = _remoteStream;
        notifyListeners();
      }
    };

    _peerConnection!.onIceCandidate = (RTCIceCandidate candidate) {
      signalingService.sendWebRtcSignal({
        'type': 'ice_candidate',
        'to': targetCode,
        'candidate': {
          'sdpMLineIndex': candidate.sdpMLineIndex,
          'sdpMid': candidate.sdpMid,
          'candidate': candidate.candidate,
        },
      });
    };
  }

  Future<void> _onSignalingMessage(Map<String, dynamic> data) async {
    final type = data['type'] as String?;

    if (type == 'webrtc_offer') {
      final sdp = data['sdp'] as String;
      final from = data['from'] as String? ?? '';
      await _setupPeerConnection(from);
      await _peerConnection!.setRemoteDescription(RTCSessionDescription(sdp, 'offer'));

      final answer = await _peerConnection!.createAnswer();
      await _peerConnection!.setLocalDescription(answer);

      signalingService.sendWebRtcSignal({
        'type': 'webrtc_answer',
        'to': from,
        'sdp': answer.sdp,
      });
    } else if (type == 'webrtc_answer') {
      final sdp = data['sdp'] as String;
      await _peerConnection!.setRemoteDescription(RTCSessionDescription(sdp, 'answer'));
    } else if (type == 'ice_candidate') {
      final cand = data['candidate'] as Map<String, dynamic>?;
      if (cand != null && _peerConnection != null) {
        final candidate = RTCIceCandidate(
          cand['candidate'],
          cand['sdpMid'],
          cand['sdpMLineIndex'],
        );
        await _peerConnection!.addCandidate(candidate);
      }
    }
  }

  // --- Controls ---

  void toggleMute() {
    if (_localStream == null) return;
    _isMuted = !_isMuted;
    for (final track in _localStream!.getAudioTracks()) {
      track.enabled = !_isMuted;
    }
    notifyListeners();
  }

  void toggleSpeaker() {
    _isSpeakerOn = !_isSpeakerOn;
    if (_localStream != null) {
      for (final track in _localStream!.getAudioTracks()) {
        track.enableSpeakerphone(_isSpeakerOn);
      }
    }
    notifyListeners();
  }

  void toggleVideo() {
    if (_localStream == null) return;
    _isVideoEnabled = !_isVideoEnabled;
    for (final track in _localStream!.getVideoTracks()) {
      track.enabled = _isVideoEnabled;
    }
    notifyListeners();
  }

  void switchCamera() {
    if (_localStream == null) return;
    _isFrontCamera = !_isFrontCamera;
    final videoTrack = _localStream!.getVideoTracks().firstOrNull;
    if (videoTrack != null) {
      Helper.switchCamera(videoTrack);
    }
    notifyListeners();
  }

  Future<void> cleanUp() async {
    _signalingSub?.cancel();
    _localStream?.getTracks().forEach((track) => track.stop());
    await _localStream?.dispose();
    _localStream = null;

    await _peerConnection?.close();
    _peerConnection = null;

    localRenderer.srcObject = null;
    remoteRenderer.srcObject = null;
    notifyListeners();
  }

  @override
  void dispose() {
    cleanUp();
    localRenderer.dispose();
    remoteRenderer.dispose();
    super.dispose();
  }
}
