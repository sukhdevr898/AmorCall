import 'dart:async';
import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:web_socket_channel/web_socket_channel.dart';
import '../core/config/env_config.dart';

enum CallStatus { idle, outgoingRinging, incomingRinging, connecting, connected, ended }

class SignalingService extends ChangeNotifier {
  WebSocketChannel? _channel;
  Timer? _reconnectTimer;
  Timer? _pingTimer;

  bool _isConnected = false;
  String _myCode = '';
  String _partnerCode = '';
  String _myFullName = '';

  CallStatus _callStatus = CallStatus.idle;
  String? _currentCallId;
  String? _incomingCallerName;
  String? _incomingCallerCode;
  String? _currentCallType; // 'VOICE' or 'VIDEO'

  // Stream controllers for WebRTC & App Events
  final _messageStreamController = StreamController<Map<String, dynamic>>.broadcast();
  Stream<Map<String, dynamic>> get messageStream => _messageStreamController.stream;

  bool get isConnected => _isConnected;
  CallStatus get callStatus => _callStatus;
  String? get currentCallId => _currentCallId;
  String? get incomingCallerName => _incomingCallerName;
  String? get incomingCallerCode => _incomingCallerCode;
  String? get currentCallType => _currentCallType;

  void init({
    required String myCode,
    required String partnerCode,
    required String myFullName,
  }) {
    _myCode = myCode;
    _partnerCode = partnerCode;
    _myFullName = myFullName;
    connect();
  }

  void connect() {
    if (_isConnected) return;

    try {
      final wsUrl = Uri.parse(EnvConfig.websocketUrl);
      _channel = WebSocketChannel.connect(wsUrl);

      _channel!.stream.listen(
        (data) {
          if (!_isConnected) {
            _isConnected = true;
            notifyListeners();
            _sendRegistration();
            _startHeartbeat();
          }
          _handleIncomingMessage(data.toString());
        },
        onError: (err) {
          debugPrint('Signaling WebSocket error: $err');
          _handleDisconnect();
        },
        onDone: () {
          debugPrint('Signaling WebSocket closed');
          _handleDisconnect();
        },
      );
    } catch (e) {
      debugPrint('Error connecting signaling socket: $e');
      _handleDisconnect();
    }
  }

  void _sendRegistration() {
    _send({
      'type': 'register',
      'pairCode': _myCode,
      'fullName': _myFullName,
      'userName': _myFullName,
      'partnerPairCode': _partnerCode,
    });
  }

  void _startHeartbeat() {
    _pingTimer?.cancel();
    _pingTimer = Timer.periodic(const Duration(seconds: 25), (timer) {
      if (_isConnected) {
        _send({'type': 'ping'});
      }
    });
  }

  void _handleIncomingMessage(String rawJson) {
    try {
      final data = jsonDecode(rawJson) as Map<String, dynamic>;
      final type = data['type'] as String?;

      switch (type) {
        case 'call_request':
          _incomingCallerCode = data['from'];
          _incomingCallerName = data['callerName'] ?? 'My Partner';
          _currentCallType = data['callType'] ?? 'VOICE';
          _currentCallId = data['callId'] ?? DateTime.now().millisecondsSinceEpoch.toString();
          _callStatus = CallStatus.incomingRinging;
          notifyListeners();
          break;

        case 'call_ring':
          _callStatus = CallStatus.outgoingRinging;
          notifyListeners();
          break;

        case 'call_accept':
          _callStatus = CallStatus.connected;
          notifyListeners();
          break;

        case 'call_decline':
        case 'call_end':
          _resetCallState();
          break;

        case 'pong':
          // Heartbeat ack
          break;

        default:
          _messageStreamController.add(data);
          break;
      }
    } catch (e) {
      debugPrint('Failed to parse signaling message: $e');
    }
  }

  // --- Calling Methods ---

  void startCall({required String targetCode, required bool isVideo}) {
    _currentCallId = DateTime.now().millisecondsSinceEpoch.toString();
    _currentCallType = isVideo ? 'VIDEO' : 'VOICE';
    _callStatus = CallStatus.outgoingRinging;
    notifyListeners();

    _send({
      'type': 'call_request',
      'from': _myCode,
      'target': targetCode,
      'callerName': _myFullName,
      'callType': _currentCallType,
      'callId': _currentCallId,
    });
  }

  void acceptCall() {
    if (_incomingCallerCode == null || _currentCallId == null) return;
    _callStatus = CallStatus.connected;
    notifyListeners();

    _send({
      'type': 'call_accept',
      'to': _incomingCallerCode,
      'callId': _currentCallId,
    });
  }

  void declineCall() {
    if (_incomingCallerCode != null && _currentCallId != null) {
      _send({
        'type': 'call_decline',
        'to': _incomingCallerCode,
        'callId': _currentCallId,
      });
    }
    _resetCallState();
  }

  void endCall({required String targetCode}) {
    _send({
      'type': 'call_end',
      'to': targetCode,
      'callId': _currentCallId ?? '',
    });
    _resetCallState();
  }

  // WebRTC Signal Forwarding
  void sendWebRtcSignal(Map<String, dynamic> payload) {
    _send(payload);
  }

  void _resetCallState() {
    _callStatus = CallStatus.idle;
    _currentCallId = null;
    _incomingCallerCode = null;
    _incomingCallerName = null;
    _currentCallType = null;
    notifyListeners();
  }

  void _send(Map<String, dynamic> payload) {
    if (_channel != null && _isConnected) {
      try {
        _channel!.sink.add(jsonEncode(payload));
      } catch (e) {
        debugPrint('Signaling send error: $e');
      }
    }
  }

  void _handleDisconnect() {
    _isConnected = false;
    _pingTimer?.cancel();
    notifyListeners();

    _reconnectTimer?.cancel();
    _reconnectTimer = Timer(const Duration(seconds: 4), () {
      connect();
    });
  }

  @override
  void dispose() {
    _reconnectTimer?.cancel();
    _pingTimer?.cancel();
    _channel?.sink.close();
    _messageStreamController.close();
    super.dispose();
  }
}
