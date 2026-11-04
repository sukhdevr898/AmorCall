import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'package:firebase_core/firebase_core.dart';
import 'core/config/env_config.dart';
import 'core/theme/duora_theme.dart';
import 'services/auth_service.dart';
import 'services/signaling_service.dart';
import 'screens/splash/splash_screen.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Load environment variables from .env
  await EnvConfig.init();

  // Try initializing Firebase safely (won't crash if offline or keys pending)
  try {
    await Firebase.initializeApp();
  } catch (e) {
    debugPrint('Firebase safe initialization note: $e');
  }

  // Edge-to-edge transparent system overlays
  SystemChrome.setSystemUIOverlayStyle(
    const SystemUiOverlayStyle(
      statusBarColor: Colors.transparent,
      statusBarIconBrightness: Brightness.light,
      systemNavigationBarColor: Colors.transparent,
      systemNavigationBarIconBrightness: Brightness.light,
    ),
  );

  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthService()),
        ChangeNotifierProvider(create: (_) => SignalingService()),
      ],
      child: const DuoraApp(),
    ),
  );
}

class DuoraApp extends StatelessWidget {
  const DuoraApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Duora',
      debugShowCheckedModeBanner: false,
      theme: DuoraTheme.darkTheme,
      home: const SplashScreen(),
    );
  }
}
