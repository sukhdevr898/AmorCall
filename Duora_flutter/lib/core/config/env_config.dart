import 'package:flutter_dotenv/flutter_dotenv.dart';

class EnvConfig {
  static Future<void> init() async {
    try {
      await dotenv.load(fileName: ".env");
    } catch (_) {
      // Fallback if .env asset is absent in test or minimal environments
    }
  }

  static String get serverUrl =>
      dotenv.env['SERVER_URL'] ?? 'https://amorcall.onrender.com';

  static String get websocketUrl =>
      dotenv.env['WEBSOCKET_URL'] ?? 'wss://amorcall.onrender.com';

  static String get firebaseApiKey => dotenv.env['FIREBASE_API_KEY'] ?? '';
  static String get firebaseProjectId =>
      dotenv.env['FIREBASE_PROJECT_ID'] ?? 'duora-app';

  static String get stunServer1 =>
      dotenv.env['STUN_SERVER_1'] ?? 'stun:stun.l.google.com:19302';
  static String get stunServer2 =>
      dotenv.env['STUN_SERVER_2'] ?? 'stun:stun1.l.google.com:19302';
}
